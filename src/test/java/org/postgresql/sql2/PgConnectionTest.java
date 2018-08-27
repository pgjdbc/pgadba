package org.postgresql.sql2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.postgresql.sql2.testutil.FutureUtil.get10;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;
import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Submission;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.sql2.testutil.CollectorUtils;
import org.postgresql.sql2.testutil.ConnectUtil;
import org.postgresql.sql2.testutil.DatabaseHolder;
import org.postgresql.sql2.testutil.SimpleRowSubscriber;
import org.testcontainers.containers.PostgreSQLContainer;

public class PgConnectionTest {
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
  public void selectAfterDeactivateActivate() throws InterruptedException, ExecutionException, TimeoutException {

    String sql = "select 1 as t";
    try (Connection conn = ds.getConnection()) {
      conn.deactivate();
      conn.activate();
      Integer result = get10(conn.<Integer>rowOperation(sql)
          .collect(CollectorUtils.singleCollector(Integer.class))
          .submit().getCompletionStage());
      assertEquals(Integer.valueOf(1), result);
    }
  }

  @Test
  public void selectWithBrokenCollectorSupplier() throws InterruptedException, ExecutionException, TimeoutException {

    String sql = "select 1 as t";
    try (Connection conn = ds.getConnection()) {
      conn.<Integer>rowOperation(sql)
          .collect(Collector.of(
              () -> {
                throw new Error("exception thrown in supplier");
              },
              (a, r) -> ((Integer[])a)[0] += r.get(Integer.class),
              (l, r) -> null,
              a -> ((Integer[])a)[0]
          ));
    } catch (Error e) {
      assertEquals("exception thrown in supplier", e.getMessage());
    }
    Thread.sleep(10000);
  }

  @Test
  public void selectWithBrokenCollectorAccumulator() throws InterruptedException, ExecutionException, TimeoutException {

    String sql = "select 1 as t";
    try (Connection conn = ds.getConnection()) {
      get10(conn.<Integer>rowOperation(sql)
          .collect(Collector.of(
              () -> new Integer[] {0},
              (a, r) -> {
                throw new Error("exception thrown in accumulator");
              },
              (l, r) -> null,
              a -> ((Integer[])a)[0]
          ))
          .submit().getCompletionStage());
    } catch (ExecutionException e) {
      assertEquals("exception thrown in accumulator", e.getCause().getMessage());
    }
  }

  @Test
  public void selectWithBrokenCollectorFinisher() throws InterruptedException, ExecutionException, TimeoutException {

    String sql = "select 1 as t";
    try (Connection conn = ds.getConnection()) {
      get10(conn.<Integer>rowOperation(sql)
          .collect(Collector.of(
              () -> new Integer[] {0},
              (a, r) -> a[0] += r.at("t").get(Integer.class),
              (l, r) -> null,
              a -> {
                throw new Error("exception thrown in finisher");
              }
          ))
          .submit().getCompletionStage());
      fail("an ExecutionException should have been thrown");
    } catch (ExecutionException e) {
      assertEquals("exception thrown in finisher", e.getCause().getMessage());
    }
  }

  @Test
  public void connectTwice() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      Thread.sleep(1000); // wait a bit so that the connection have time to come up
      conn.connectOperation();
      fail("you are not allowed to connect twice");
    } catch (IllegalStateException e) {
      assertEquals("only connections in state NEW are allowed to start connecting", e.getMessage());
    }
  }

  @Test
  public void deactivationListener() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      final Connection[] eventConn = new Connection[1];
      final Connection.Lifecycle[] eventPrevious = new Connection.Lifecycle[1];
      final Connection.Lifecycle[] eventCurrent = new Connection.Lifecycle[1];

      conn.registerLifecycleListener(new Connection.ConnectionLifecycleListener() {
        @Override
        public void lifecycleEvent(Connection conn, Connection.Lifecycle previous, Connection.Lifecycle current) {
          eventConn[0] = conn;
          eventPrevious[0] = previous;
          eventCurrent[0] = current;
        }
      });
      conn.deactivate();

      assertEquals(conn, eventConn[0]);
      assertEquals(Connection.Lifecycle.NEW, eventPrevious[0]);
      assertEquals(Connection.Lifecycle.NEW_INACTIVE, eventCurrent[0]);

      conn.activate();

      assertEquals(conn, eventConn[0]);
      assertEquals(Connection.Lifecycle.NEW_INACTIVE, eventPrevious[0]);
      assertEquals(Connection.Lifecycle.NEW, eventCurrent[0]);

      Integer result = get10(conn.<Integer>rowOperation("select 1 as t")
          .collect(CollectorUtils.singleCollector(Integer.class))
          .submit().getCompletionStage());
      assertEquals(Integer.valueOf(1), result);
    }
  }

  @Test
  public void validateCompleteGoodConnection() throws TimeoutException, ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      Submission<Void> sub = conn.validationOperation(Connection.Validation.COMPLETE).submit();

      get10(sub.getCompletionStage());
    }
  }

  @Test
  public void validateLocalGoodConnection() throws TimeoutException, ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      //First do a normal query so that the connection has time to get established
      get10(conn.rowOperation("select 1").submit().getCompletionStage());

      //now validate it
      Submission<Void> sub = conn.validationOperation(Connection.Validation.LOCAL).submit();

      get10(sub.getCompletionStage());
    }
  }

  @Test
  public void localOperationSimple() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      //First do a normal query so that the connection has time to get established
      Integer result = get10(conn.<Integer>localOperation().onExecution(() -> {
        return 100;
      }).submit().getCompletionStage());

      assertEquals(Integer.valueOf(100), result);
    }
  }

  @Test
  public void localOperationExceptional() {
    try (Connection conn = ds.getConnection()) {
      //First do a normal query so that the connection has time to get established
      get10(conn.<Integer>localOperation().onExecution(() -> {
        throw new Exception("thrown from a local operation");
      }).submit().getCompletionStage());

      fail("the future should have completed exceptionally");
    } catch (Exception e) {
      assertEquals("thrown from a local operation", e.getCause().getMessage());
    }
  }

  @Test
  public void rowPublisherOperation() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletableFuture<Integer> result1 = new CompletableFuture<>();
      //First do a normal query so that the connection has time to get established
      Integer result = get10(conn.<Integer>rowPublisherOperation("select 321 as t")
          .subscribe(new SimpleRowSubscriber(result1), result1)
          .submit().getCompletionStage());

      assertEquals(Integer.valueOf(321), result);
    }
  }

  @Test
  public void outParameterTest() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      get10(conn.operation("CREATE OR REPLACE FUNCTION get_test(OUT x integer, OUT y integer)\n"
          + "AS $$\n"
          + "BEGIN\n"
          + "   x := 1;\n"
          + "   y := 2;\n"
          + "END;\n"
          + "$$  LANGUAGE plpgsql").submit().getCompletionStage());

      get10(conn.outOperation("select * from get_test() as result")
          .outParameter("$1", AdbaType.INTEGER)
          .outParameter("$2", AdbaType.INTEGER)
          .apply((r) -> {
            assertEquals(Integer.valueOf(1), r.at("x").get(Integer.class));
            assertEquals(Integer.valueOf(2), r.at("y").get(Integer.class));
            return null;
          }).submit().getCompletionStage());

      get10(conn.operation("DROP FUNCTION get_test()").submit().getCompletionStage());
    }
  }

  @Test
  public void outParameterTestReturnedValue() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      get10(conn.operation("CREATE OR REPLACE FUNCTION outParameterTestReturnedValue(OUT x integer, OUT y integer)\n"
          + "AS $$\n"
          + "BEGIN\n"
          + "   x := 1;\n"
          + "   y := 2;\n"
          + "END;\n"
          + "$$  LANGUAGE plpgsql").submit().getCompletionStage());

      Integer result = get10(conn.<Integer>outOperation("select * from outParameterTestReturnedValue() as result")
          .outParameter("$1", AdbaType.INTEGER)
          .outParameter("$2", AdbaType.INTEGER)
          .apply((r) -> r.at("x").get(Integer.class) + r.at("y").get(Integer.class))
          .submit().getCompletionStage());

      get10(conn.operation("DROP FUNCTION outParameterTestReturnedValue()").submit()
          .getCompletionStage());

      assertEquals(Integer.valueOf(3), result);
    }
  }
}