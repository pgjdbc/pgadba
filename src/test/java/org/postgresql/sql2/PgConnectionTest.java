package org.postgresql.sql2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.postgresql.sql2.testutil.FutureUtil.get10;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import org.junit.jupiter.api.Disabled;
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
  @Disabled
  public void largeNumberOfConnections() throws InterruptedException, ExecutionException, TimeoutException {
    Instant lastTime = Instant.now();
    Instant nowTime = Instant.now();

    long sum1 = 0;
    long sum2 = 0;
    long sum3 = 0;
    long sum4 = 0;
    long sum5 = 0;

    String sql = "select 1 as t";
    for (int i = 0; i < 100000; i++) {
      nowTime = Instant.now();
      sum1 += ChronoUnit.NANOS.between(lastTime, nowTime);
      lastTime = Instant.now();

      try (Connection conn = ds.getConnection()) {
        nowTime = Instant.now();
        sum2 += ChronoUnit.NANOS.between(lastTime, nowTime);
        lastTime = Instant.now();
        Integer result = get10(conn.<Integer>rowOperation(sql)
            .collect(CollectorUtils.singleCollector(Integer.class))
            .submit().getCompletionStage());
        nowTime = Instant.now();
        sum3 += ChronoUnit.NANOS.between(lastTime, nowTime);
        lastTime = Instant.now();
        assertEquals(Integer.valueOf(1), result);
        nowTime = Instant.now();
        sum4 += ChronoUnit.NANOS.between(lastTime, nowTime);
        lastTime = Instant.now();
      }
      nowTime = Instant.now();
      sum5 += ChronoUnit.NANOS.between(lastTime, nowTime);
      lastTime = Instant.now();

      if (i != 0 && i % 100 == 0) {
        System.out.println("sum1 = " + sum1 / i);
        System.out.println("sum2 = " + sum2 / i);
        System.out.println("sum3 = " + sum3 / i);
        System.out.println("sum4 = " + sum4 / i);
        System.out.println("sum5 = " + sum5 / i);
        System.out.println();
      }
    }
  }

  @Test
  public void validateCloseCompletesNormally() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      Submission<Void> sub = conn.closeOperation().submit();

      get10(sub.getCompletionStage());
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