package org.postgresql.adba;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.postgresql.adba.testutil.FutureUtil.get10;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.Result.RowColumn;
import jdk.incubator.sql2.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.adba.testutil.ConnectUtil;
import org.postgresql.adba.testutil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

public class BackPressureTest {
  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

  private static DataSource ds;

  @BeforeAll
  public static void setUp() {
    ds = ConnectUtil.openDb(postgres);
  }

  @AfterAll
  public static void tearDown() {
    ds.close();
  }

  @Test
  public void backPressure() throws InterruptedException, ExecutionException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletableFuture<String> result = new CompletableFuture<>();

      String sql = "SELECT day::date as t \n"
          + "FROM generate_series(timestamp '1910-11-20'"
          + "                   , timestamp '1917-02-05'"
          + "                   , interval  '1 day') AS t(day);";

      // Very basic SubmissionPublisher (and a Flow.Processor), so I can expose the result as a publisher
      RowProcessor rp = new RowProcessor();

      MySubscriber subscriber = new MySubscriber();
      subscriber.setDemand(2270);

      rp.subscribe(subscriber);

      final CompletionStage<String> completableFuture = session.<String>rowPublisherOperation(sql)
          .subscribe(rp, result)
          .submit()
          .getCompletionStage();

      String res = get10(completableFuture);
      assertNull(res);

      subscriber.waitForComplete();
      List<String> items = subscriber.getItems();
      assertEquals(78, items.size());
    }
  }

  public class RowProcessor extends SubmissionPublisher<RowColumn> implements Flow.Processor<Result.RowColumn, Result.RowColumn> {

    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription subscription) {
      this.subscription = subscription;
      this.subscription.request(1);
    }

    @Override
    public void onNext(RowColumn item) {
      submit(item);
      this.subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
      throwable.printStackTrace();
      fail(throwable.getMessage());
    }

    @Override
    public void onComplete() {
      System.err.println("completed");
      close();
    }
  }

  public class MySubscriber implements Subscriber<RowColumn> {

    private static final String LOG_MESSAGE_FORMAT = "Subscriber >> %s%n";

    private long demand = 0;

    private Subscription subscription;

    private long count;

    private List<String> items = new ArrayList<>();

    private CountDownLatch complete = new CountDownLatch(1);

    public void setDemand(long demand) {
      this.demand = demand;
      count = demand;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
      log("Subscribed");
      this.subscription = subscription;

      requestItems(demand);
    }

    private void requestItems(long n) {
      log("Requesting %d new items...", n);
      subscription.request(n);
    }

    @Override
    public void onNext(RowColumn item) {
      if (item != null) {
        items.add(item.at("t").get(LocalDate.class).format(DateTimeFormatter.ISO_DATE));

        synchronized (this) {
          count--;

          if (count == 0) {
            log("Cancelling subscription...");
            subscription.cancel();
          }
        }
      } else {
        log("Null Item!");
      }
    }

    @Override
    public void onComplete() {
      log("onComplete(): There is no remaining item in Processor.");
      complete.countDown();
    }

    @Override
    public void onError(Throwable t) {
      t.printStackTrace();
      fail(t.getMessage());

    }

    private void log(String message, Object... args) {
      String fullMessage = String.format(LOG_MESSAGE_FORMAT, message);

      System.out.printf(fullMessage, args);
    }

    public List<String> getItems() {
      return items;
    }

    public void waitForComplete() throws InterruptedException {
      complete.await(10L, TimeUnit.SECONDS);
    }
  }
}
