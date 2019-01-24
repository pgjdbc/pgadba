package org.postgresql.adba;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.postgresql.adba.testutil.CollectorUtils.singleCollector;
import static org.postgresql.adba.testutil.FutureUtil.get10;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;
import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Session;
import jdk.incubator.sql2.Session.Lifecycle;
import jdk.incubator.sql2.Submission;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.postgresql.adba.testutil.CollectorUtils;
import org.postgresql.adba.testutil.ConnectUtil;
import org.postgresql.adba.testutil.DatabaseHolder;
import org.postgresql.adba.testutil.SimpleRowSubscriber;
import org.testcontainers.containers.PostgreSQLContainer;

public class PgSessionTest {
  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();
  public static PostgreSQLContainer postgres11 = DatabaseHolder.getNew11();

  private static DataSource ds;
  private static DataSource ds11;

  @BeforeAll
  public static void setUp() {
    ds = ConnectUtil.openDb(postgres);
    ds11 = ConnectUtil.openDb(postgres11);

    ConnectUtil.createTable(ds, "tab",
        "id int", "name varchar(100)", "answer int");
  }

  @AfterAll
  public static void tearDown() {
    ds.close();
  }

  @Test
  public void sessionWithTcpKeepAlive() throws InterruptedException, ExecutionException, TimeoutException {
    try (Session session = ds.builder().property(PgSessionProperty.TCP_KEEP_ALIVE, true).build().attach()) {
      CompletionStage<String> idF = session.<String>rowOperation("select '1'::text as t")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("1", get10(idF));
    }
  }

  @Test
  public void sessionBuilderSetPropertyAfterBuild() {
    Session.Builder builder = ds.builder();
    Session session = builder.build().attach();

    assertThrows(IllegalStateException.class, () -> builder.property(PgSessionProperty.TCP_KEEP_ALIVE, true));

    session.close();
  }

  @Test
  public void sessionBuilderBuildTwice() {
    Session.Builder builder = ds.builder();
    Session session = builder.build().attach();

    assertThrows(IllegalStateException.class, () -> builder.build());

    session.close();
  }

  @Test
  public void sessionBuilderBuildAfterDsClose() {
    DataSource localDs = ConnectUtil.openDb(postgres);
    Session.Builder builder = localDs.builder();
    localDs.close();

    assertThrows(IllegalStateException.class, () -> builder.build());
  }

  @Test
  public void loginScramSha256() throws InterruptedException, ExecutionException, TimeoutException {
    try (Session session = ds11.getSession()) {
      CompletionStage<Integer> idF = session.<Integer>rowOperation("select 1918 as t")
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(1918), get10(idF));
    }
  }

  @Test
  public void setApplicationName() throws InterruptedException, ExecutionException, TimeoutException {
    String name = "my custom application name";
    try (Session session = ds.builder().property(PgSessionProperty.APPLICATION_NAME, name).build().attach()) {
      CompletionStage<String> idF = session.<String>rowOperation("select current_setting('application_name') t")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals(name, get10(idF));
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

      try (Session session = ds.getSession()) {
        nowTime = Instant.now();
        sum2 += ChronoUnit.NANOS.between(lastTime, nowTime);
        lastTime = Instant.now();
        Integer result = get10(session.<Integer>rowOperation(sql)
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
    try (Session session = ds.getSession()) {
      Submission<Void> sub = session.closeOperation().submit();

      get10(sub.getCompletionStage());
    }
  }

  @Test
  public void selectWithBrokenCollectorSupplier() throws InterruptedException, ExecutionException, TimeoutException {

    String sql = "select 1 as t";
    try (Session session = ds.getSession()) {
      session.<Integer>rowOperation(sql)
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
    try (Session session = ds.getSession()) {
      get10(session.<Integer>rowOperation(sql)
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
    try (Session session = ds.getSession()) {
      get10(session.<Integer>rowOperation(sql)
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
    try (Session session = ds.getSession()) {
      Thread.sleep(1000); // wait a bit so that the Session have time to come up
      session.attachOperation();
      fail("you are not allowed to connect twice");
    } catch (IllegalStateException e) {
      assertEquals("only connections in state NEW are allowed to start connecting", e.getMessage());
    }
  }

  @Test
  public void deactivationListener() throws InterruptedException, ExecutionException, TimeoutException {

    try (Session session = ds.getSession()) {
      final List<Lifecycle> events = new ArrayList<>();
      final List<Lifecycle> previousEvents = new ArrayList<>();
      final Session[] eventSession = new Session[1];

      session.registerLifecycleListener(new Session.SessionLifecycleListener() {
        @Override
        public void lifecycleEvent(Session session, Session.Lifecycle previous, Session.Lifecycle current) {
          eventSession[0] = session;
          events.add(current);
          previousEvents.add(previous);
        }
      });
      Submission<Void> close = session.closeOperation().submit();

      CompletionStage<Void> stage = close.getCompletionStage();
      get10(stage);

      assertEquals(session, eventSession[0]);
      assertArrayEquals(new Session.Lifecycle[]{Lifecycle.CLOSING, Lifecycle.CLOSING, Lifecycle.CLOSED},
          events.toArray(new Session.Lifecycle[]{}));
      assertArrayEquals(new Session.Lifecycle[]{Lifecycle.NEW, Lifecycle.CLOSING, Lifecycle.CLOSING},
          previousEvents.toArray(new Session.Lifecycle[]{}));
    }
  }

  @Test
  public void validateCompleteGoodConnection() throws TimeoutException, ExecutionException, InterruptedException {
    try (Session session = ds.getSession()) {
      Submission<Void> sub = session.validationOperation(Session.Validation.COMPLETE).submit();

      get10(sub.getCompletionStage());
    }
  }

  @Test
  public void validateLocalGoodConnection() throws TimeoutException, ExecutionException, InterruptedException {
    try (Session session = ds.getSession()) {
      //First do a normal query so that the Session has time to get established
      get10(session.rowOperation("select 1").submit().getCompletionStage());

      //now validate it
      Submission<Void> sub = session.validationOperation(Session.Validation.LOCAL).submit();

      get10(sub.getCompletionStage());
    }
  }

  @Test
  public void localOperationSimple() throws InterruptedException, ExecutionException, TimeoutException {
    try (Session session = ds.getSession()) {
      //First do a normal query so that the Session has time to get established
      Integer result = get10(session.<Integer>localOperation().onExecution(() -> {
        return 100;
      }).submit().getCompletionStage());

      assertEquals(Integer.valueOf(100), result);
    }
  }

  @Test
  public void localOperationExceptional() {
    try (Session session = ds.getSession()) {
      //First do a normal query so that the Session has time to get established
      get10(session.<Integer>localOperation().onExecution(() -> {
        throw new Exception("thrown from a local operation");
      }).submit().getCompletionStage());

      fail("the future should have completed exceptionally");
    } catch (Exception e) {
      assertEquals("thrown from a local operation", e.getCause().getMessage());
    }
  }

  @Test
  public void rowPublisherOperation() throws InterruptedException, ExecutionException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletableFuture<Integer> result1 = new CompletableFuture<>();
      //First do a normal query so that the Session has time to get established
      Integer result = get10(session.<Integer>rowPublisherOperation("select 321 as t")
          .subscribe(new SimpleRowSubscriber(result1), result1)
          .submit().getCompletionStage());

      assertEquals(Integer.valueOf(321), result);
    }
  }

  @Test
  public void outParameterTest() throws InterruptedException, ExecutionException, TimeoutException {
    try (Session session = ds.getSession()) {
      get10(session.operation("CREATE OR REPLACE FUNCTION get_test(OUT x integer, OUT y integer)\n"
          + "AS $$\n"
          + "BEGIN\n"
          + "   x := 1;\n"
          + "   y := 2;\n"
          + "END;\n"
          + "$$  LANGUAGE plpgsql").submit().getCompletionStage());

      get10(session.outOperation("select * from get_test() as result")
          .outParameter("$1", AdbaType.INTEGER)
          .outParameter("$2", AdbaType.INTEGER)
          .apply((r) -> {
            assertEquals(Integer.valueOf(1), r.at("x").get(Integer.class));
            assertEquals(Integer.valueOf(2), r.at("y").get(Integer.class));
            return null;
          }).submit().getCompletionStage());

      get10(session.operation("DROP FUNCTION get_test()").submit().getCompletionStage());
    }
  }

  @Test
  public void outParameterTestReturnedValue() throws InterruptedException, ExecutionException, TimeoutException {
    try (Session session = ds.getSession()) {
      get10(session.operation("CREATE OR REPLACE FUNCTION outParameterTestReturnedValue(OUT x integer, OUT y integer)\n"
          + "AS $$\n"
          + "BEGIN\n"
          + "   x := 1;\n"
          + "   y := 2;\n"
          + "END;\n"
          + "$$  LANGUAGE plpgsql").submit().getCompletionStage());

      Integer result = get10(session.<Integer>outOperation("select * from outParameterTestReturnedValue() as result")
          .outParameter("$1", AdbaType.INTEGER)
          .outParameter("$2", AdbaType.INTEGER)
          .apply((r) -> r.at("x").get(Integer.class) + r.at("y").get(Integer.class))
          .submit().getCompletionStage());

      get10(session.operation("DROP FUNCTION outParameterTestReturnedValue()").submit()
          .getCompletionStage());

      assertEquals(Integer.valueOf(3), result);
    }
  }
}