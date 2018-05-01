package org.postgresql.sql2;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.SqlException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ErrorStatesTest {
  @ClassRule
  public static PostgreSQLContainer postgres = new PostgreSQLContainer();

  private static DataSource ds;

  @BeforeClass
  public static void setUp() {
    ds = TestUtil.openDB(postgres);

    TestUtil.createTable(ds, "tab",
        "id int", "name varchar(100)", "answer int");
  }

  @AfterClass
  public static void tearDown() {
    ds.close();
    postgres.close();
  }

  @Test
  public void testSqlError() throws InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select select")
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), idF.toCompletableFuture().get());
    } catch (ExecutionException e) {
      SqlException ex = (SqlException)e.getCause();

      assertEquals("Severity: ERROR\n" +
          "Message: syntax error at or near \"select\"", ex.getMessage());
      assertEquals("42601", ex.getSqlState());
      assertEquals("select select", ex.getSqlString());
    }
  }

  @Test
  public void testGetNameThatIsntUsed() throws InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select 100 as t")
          .collect(Collector.of(
              () -> new int[1],
              (a, r) -> a[0] = r.get("notused", Integer.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      idF.toCompletableFuture().get();
      fail("the column 'notused' doesn't exist in the result.row and should result in an IllegalArgumentException");
    } catch (ExecutionException e) {
      IllegalArgumentException ex = (IllegalArgumentException)e.getCause();

      assertEquals("no column with id notused", ex.getMessage());
    }
  }

  @Test
  public void testGetCaseInsensitive1() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select 100 as t")
          .collect(Collector.of(
              () -> new int[1],
              (a, r) -> a[0] = r.get("T", Integer.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void testGetCaseInsensitive2() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select 100 as T")
          .collect(Collector.of(
              () -> new int[1],
              (a, r) -> a[0] = r.get("t", Integer.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), idF.toCompletableFuture().get());
    }
  }
}
