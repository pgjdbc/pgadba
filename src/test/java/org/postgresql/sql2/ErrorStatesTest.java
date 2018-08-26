package org.postgresql.sql2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.postgresql.sql2.testutil.FutureUtil.get10;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.SqlException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.sql2.testutil.ConnectUtil;
import org.postgresql.sql2.testutil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

public class ErrorStatesTest {
  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

  private static DataSource ds;

  @BeforeAll
  public static void setUp() {
    ds = ConnectUtil.openDb(postgres);

    ConnectUtil.createTable(ds, "tab",
        "id int", "name varchar(100)", "answer int");
  }

  @AfterAll
  public static void tearDown() {
    ds.close();
  }

  @Test
  public void testSqlError() throws InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select select")
          .submit()
          .getCompletionStage();

      get10(idF);
    } catch (ExecutionException e) {
      SqlException ex = (SqlException)e.getCause();

      assertEquals("syntax error at or near \"select\"", ex.getMessage());
      assertEquals("ERROR", ex.getSqlState());
      assertEquals(42601, ex.getVendorCode());
      assertNull(ex.getSqlString());
    }
  }

  @Test
  public void testRowOperationOnError() throws InterruptedException, TimeoutException {
    final boolean[] onErrorResult = new boolean[] {false};
    try (Connection conn = ds.getConnection()) {
      get10(conn.rowOperation("select select")
          .onError(t -> onErrorResult[0] = true)
          .submit()
          .getCompletionStage());
    } catch (ExecutionException ignore) {
      //ignored
    }
    assertTrue(onErrorResult[0]);
  }

  @Test
  public void testRowOperationOnErrorTwice() throws InterruptedException, TimeoutException, ExecutionException {
    final boolean[] onErrorResult = new boolean[] {false};
    try (Connection conn = ds.getConnection()) {
      get10(conn.rowOperation("select select")
          .onError(t -> onErrorResult[0] = true)
          .onError(t -> onErrorResult[0] = true)
          .submit()
          .getCompletionStage());
      fail("you are not allowed to call onError twice");
    } catch (IllegalStateException e) {
      assertEquals("you are not allowed to call onError multiple times", e.getMessage());
    }
    assertFalse(onErrorResult[0]);
  }

  @Test
  public void testCountOperationOnError() throws InterruptedException, TimeoutException {
    final boolean[] onErrorResult = new boolean[] {false};
    try (Connection conn = ds.getConnection()) {
      get10(conn.rowCountOperation("select select")
          .onError(t -> onErrorResult[0] = true)
          .submit()
          .getCompletionStage());
    } catch (ExecutionException ignore) {
      //ignored
    }
    assertTrue(onErrorResult[0]);
  }

  @Test
  public void testOperationOnError() throws InterruptedException, TimeoutException {
    final boolean[] onErrorResult = new boolean[] {false};
    try (Connection conn = ds.getConnection()) {
      get10(conn.operation("select select")
          .onError(t -> onErrorResult[0] = true)
          .submit()
          .getCompletionStage());
    } catch (ExecutionException ignore) {
      //ignored
    }
    assertTrue(onErrorResult[0]);
  }

  @Test
  public void testRowProcessorOperationOnError() throws InterruptedException, TimeoutException {
    final boolean[] onErrorResult = new boolean[] {false};
    try (Connection conn = ds.getConnection()) {
      get10(conn.rowPublisherOperation("select select")
          .onError(t -> onErrorResult[0] = true)
          .submit()
          .getCompletionStage());
    } catch (ExecutionException ignore) {
      //ignored
    }
    assertTrue(onErrorResult[0]);
  }

  @Test
  public void testGetNameThatIsntUsed() throws InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select 100 as t")
          .collect(Collector.of(
              () -> new int[1],
              (a, r) -> a[0] = r.at("notused").get(Integer.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      get10(idF);
      fail("the column 'notused' doesn't exist in the result.row and should result in an IllegalArgumentException");
    } catch (ExecutionException e) {
      IllegalArgumentException ex = (IllegalArgumentException)e.getCause();

      assertEquals("no column with id notused", ex.getMessage());
    }
  }

  @Test
  public void testGetCaseInsensitive1() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select 100 as t")
          .collect(Collector.of(
              () -> new int[1],
              (a, r) -> a[0] = r.at("T").get(Integer.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), get10(idF));
    }
  }

  @Test
  public void testGetCaseInsensitive2() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select 100 as T")
          .collect(Collector.of(
              () -> new int[1],
              (a, r) -> a[0] = r.at("t").get(Integer.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), get10(idF));
    }
  }
}
