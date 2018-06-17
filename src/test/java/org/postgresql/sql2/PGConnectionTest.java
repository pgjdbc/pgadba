package org.postgresql.sql2;

import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.Submission;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.sql2.testUtil.CollectorUtils;
import org.postgresql.sql2.testUtil.ConnectUtil;
import org.postgresql.sql2.testUtil.DatabaseHolder;
import org.postgresql.sql2.testUtil.SimpleRowProcessor;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
  public void rowProcessorOperation() throws InterruptedException, ExecutionException, TimeoutException {
    final Integer[] result = {null};
    try (Connection conn = ds.getConnection()) {
      //First do a normal query so that the connection has time to get established
      conn.rowProcessorOperation("select 321 as t")
          .rowProcessor(new Flow.Processor<Result.Row, Integer>() {
            Flow.Subscription publisherSubscription;

            final ExecutorService executor = Executors.newFixedThreadPool(4);
            Flow.Subscription subscription;
            ConcurrentLinkedQueue<Flow.Subscriber<? super Integer>> subscribers = new ConcurrentLinkedQueue<>();

            @Override
            public void subscribe(Flow.Subscriber<? super Integer> subscriber) {
              subscribers.add(subscriber);
              subscriber.onSubscribe(new Flow.Subscription() {
                @Override
                public void request(long n) {

                }

                @Override
                public void cancel() {

                }
              });
            }

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
              this.subscription = subscription;
              subscription.request(1);
            }

            @Override
            public void onNext(Result.Row item) {
              result[0] = item.get("t", Integer.class);
              subscription.request(1);

              for(Flow.Subscriber<? super Integer> s : subscribers) {
                s.onNext(result[0]);
              }
            }

            @Override
            public void onComplete() {
              for(Flow.Subscriber<? super Integer> s : subscribers) {
                s.onComplete();
              }
            }

            @Override
            public void onError(Throwable t) {
              for(Flow.Subscriber<? super Integer> s : subscribers) {
                s.onError(t);
              }
            }
          })
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

      assertEquals(Integer.valueOf(321), result[0]);
    }
  }

  @Test
  public void rowProcessorOperationReturnedValue() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      //First do a normal query so that the connection has time to get established
      Integer result = conn.<Integer>rowProcessorOperation("select 321 as t")
          .rowProcessor(new SimpleRowProcessor())
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
            assertEquals(Integer.valueOf(1), r.get("x", Integer.class));
            assertEquals(Integer.valueOf(2), r.get("y", Integer.class));
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
          .apply((r) -> r.get("x", Integer.class) + r.get("y", Integer.class))
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

      conn.operation("DROP FUNCTION outParameterTestReturnedValue").submit()
          .getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

      assertEquals(Integer.valueOf(3), result);
    }
  }
}