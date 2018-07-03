package org.postgresql.sql2;

import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Submission;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.sql2.testUtil.CollectorUtils;
import org.postgresql.sql2.testUtil.ConnectUtil;
import org.postgresql.sql2.testUtil.DatabaseHolder;
import org.postgresql.sql2.testUtil.SimpleRowSubscriber;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class PGConnectionTest {
  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

  private static DataSource ds;

  @BeforeClass
  public static void setUp() {
    ds = ConnectUtil.openDB(postgres);

    ConnectUtil.createTable(ds, "tab",
        "id int", "name varchar(100)", "answer int");
  }

  @AfterClass
  public static void tearDown() {
    ds.close();
  }

  @Test
  public void selectAfterDeactivateActivate() throws InterruptedException, ExecutionException, TimeoutException {

    String sql = "select 1 as t";
    try (Connection conn = ds.getConnection()) {
      conn.deactivate();
      conn.activate();
      Integer result = conn.<Integer>rowOperation(sql)
          .collect(CollectorUtils.singleCollector(Integer.class))
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
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
      conn.<Integer>rowOperation(sql)
          .collect(Collector.of(
              () -> new Integer[] {0},
              (a, r) -> {
                throw new Error("exception thrown in accumulator");
              },
              (l, r) -> null,
              a -> ((Integer[])a)[0]
          ))
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    } catch (ExecutionException e) {
      assertEquals("exception thrown in accumulator", e.getCause().getMessage());
    }
  }

  @Test
  public void selectWithBrokenCollectorFinisher() throws InterruptedException, ExecutionException, TimeoutException {

    String sql = "select 1 as t";
    try (Connection conn = ds.getConnection()) {
      conn.<Integer>rowOperation(sql)
          .collect(Collector.of(
              () -> new Integer[] {0},
              (a, r) -> a[0] += r.at("t").get(Integer.class),
              (l, r) -> null,
              a -> {
                throw new Error("exception thrown in finisher");
              }
          ))
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
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

      Integer result = conn.<Integer>rowOperation("select 1 as t")
          .collect(CollectorUtils.singleCollector(Integer.class))
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
      assertEquals(Integer.valueOf(1), result);
    }
  }

  @Test
  public void validateCompleteGoodConnection() throws TimeoutException, ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      Submission<Void> sub = conn.validationOperation(Connection.Validation.COMPLETE).submit();

      sub.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }
  }

  @Test
  public void validateLocalGoodConnection() throws TimeoutException, ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      //First do a normal query so that the connection has time to get established
      conn.rowOperation("select 1").submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

      //now validate it
      Submission<Void> sub = conn.validationOperation(Connection.Validation.LOCAL).submit();

      sub.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }
  }

  @Test
  public void localOperationSimple() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      //First do a normal query so that the connection has time to get established
      Integer result = conn.<Integer>localOperation().onExecution(() -> {
        return 100;
      }).submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

      assertEquals(Integer.valueOf(100), result);
    }
  }

  @Test
  public void localOperationExceptional() {
    try (Connection conn = ds.getConnection()) {
      //First do a normal query so that the connection has time to get established
      conn.<Integer>localOperation().onExecution(() -> {
        throw new Exception("thrown from a local operation");
      }).submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

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
      Integer result = conn.<Integer>rowPublisherOperation("select 321 as t")
          .subscribe(new SimpleRowSubscriber(result1), result1)
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

      assertEquals(Integer.valueOf(321), result);
    }
  }

  @Test
  public void outParameterTest() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      conn.operation("CREATE OR REPLACE FUNCTION get_test(OUT x integer, OUT y integer)\n" +
          "AS $$\n" +
          "BEGIN\n" +
          "   x := 1;\n" +
          "   y := 2;\n" +
          "END;\n" +
          "$$  LANGUAGE plpgsql").submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

      conn.outOperation("select * from get_test() as result")
          .outParameter("$1", AdbaType.INTEGER)
          .outParameter("$2", AdbaType.INTEGER)
          .apply((r) -> {
            assertEquals(Integer.valueOf(1), r.at("x").get(Integer.class));
            assertEquals(Integer.valueOf(2), r.at("y").get(Integer.class));
            return null;
          }).submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

      conn.operation("DROP FUNCTION get_test").submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }
  }

  @Test
  public void outParameterTestReturnedValue() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      conn.operation("CREATE OR REPLACE FUNCTION outParameterTestReturnedValue(OUT x integer, OUT y integer)\n" +
          "AS $$\n" +
          "BEGIN\n" +
          "   x := 1;\n" +
          "   y := 2;\n" +
          "END;\n" +
          "$$  LANGUAGE plpgsql").submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

      Integer result = conn.<Integer>outOperation("select * from outParameterTestReturnedValue() as result")
          .outParameter("$1", AdbaType.INTEGER)
          .outParameter("$2", AdbaType.INTEGER)
          .apply((r) -> r.at("x").get(Integer.class) + r.at("y").get(Integer.class))
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

      conn.operation("DROP FUNCTION outParameterTestReturnedValue").submit()
          .getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

      assertEquals(Integer.valueOf(3), result);
    }
  }
}