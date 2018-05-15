package org.postgresql.sql2;

import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.Submission;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.postgresql.sql2.testUtil.CollectorUtils;
import org.postgresql.sql2.testUtil.ConnectUtil;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PGConnectionTest {
  @ClassRule
  public static PostgreSQLContainer postgres = new PostgreSQLContainer();

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
    postgres.close();
  }

  @Test
  public void insertWithoutATable() {

    String sql = "insert into tab(id, name, answer) values ($1, $2, $3)";
    Submission sub;
    try (Connection conn = ds.getConnection()) {
      sub = conn.countOperation(sql)
          .set("$1", 1, AdbaType.NUMERIC)
          .set("$2", "Deep Thought", AdbaType.VARCHAR)
          .set("$3", 42, AdbaType.NUMERIC)
          .submit();
    }
    try {
      ((CompletableFuture) sub.getCompletionStage()).join();
      fail("table 'tab' doesn't exist, so an exception should be thrown");
    } catch (CompletionException e) {
    }
  }

  @Test
  public void insertWithATableWithWaitingBetween() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      conn.operation("create table tabForInsert(id int)")
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
      conn.countOperation("insert into tabForInsert(id) values ($1)")
          .set("$1", 1, AdbaType.NUMERIC)
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
      Long count = conn.<Long>rowOperation("select count(*) as t from tabForInsert")
          .collect(CollectorUtils.singleCollector(Long.class))
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
      conn.operation("drop table tabForInsert")
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

      assertEquals(Long.valueOf(1), count);
    }
  }

  @Test
  public void insertWithATableWithoutWaiting() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      conn.operation("create table tabForInsert(id int)")
          .submit();
      conn.countOperation("insert into tabForInsert(id) values ($1)")
          .set("$1", 1, AdbaType.NUMERIC)
          .submit();
      Submission<Long> count = conn.<Long>rowOperation("select count(*) as t from tabForInsert")
          .collect(CollectorUtils.singleCollector(Long.class))
          .submit();
      Submission<Object> drop = conn.operation("drop table tabForInsert")
          .submit();

      assertEquals(Long.valueOf(1), count.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS));
      assertNull(drop.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void multiInsertWithATable() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      conn.countOperation("create table tabForInsert(id int)")
          .submit();
      Submission<List<Integer>> arrayCount = conn.<List<Integer>>arrayCountOperation("insert into tabForInsert(id) values ($1)")
          .set("$1", new Integer[]{1, 2, 3}, AdbaType.NUMERIC)
          .submit();
      Submission<Long> count = conn.<Long>rowOperation("select count(*) as t from tabForInsert")
          .collect(CollectorUtils.singleCollector(Long.class))
          .submit();
      Submission<Object> drop = conn.operation("drop table tabForInsert")
          .submit();

      assertArrayEquals(new Integer[]{1, 1, 1}, arrayCount.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS).toArray());
      assertEquals(Long.valueOf(3), count.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS));
      assertNull(drop.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void multiInsertFutureWithATable() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Integer[]> f = CompletableFuture.supplyAsync(() -> new Integer[]{1, 2, 3});
    try (Connection conn = ds.getConnection()) {
      Submission<Object> noReturn = conn.operation("create table tabForInsert(id int)")
          .submit();
      Submission<List<Integer>> arrayCount = conn.<List<Integer>>arrayCountOperation("insert into tabForInsert(id) values ($1)")
          .set("$1", f, AdbaType.NUMERIC)
          .submit();
      Submission<Long> count = conn.<Long>rowOperation("select count(*) as t from tabForInsert")
          .collect(CollectorUtils.singleCollector(Long.class))
          .submit();
      Submission<Object> drop = conn.operation("drop table tabForInsert")
          .submit();

      assertNull(noReturn.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS));
      assertArrayEquals(new Integer[]{1, 1, 1}, arrayCount.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS).toArray());
      assertEquals(Long.valueOf(3), count.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS));
      assertNull(drop.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void insertAfterClose() {

    String sql = "insert into tab(id, name, answer) values ($1, $2, $3)";
    Submission sub;
    try (Connection conn = ds.getConnection()) {
      conn.closeOperation()
          .submit();
      conn.countOperation(sql)
          .set("$1", 1, AdbaType.NUMERIC)
          .set("$2", "Deep Thought", AdbaType.VARCHAR)
          .set("$3", 42, AdbaType.NUMERIC)
          .submit();
    } catch (IllegalStateException e) {
      assertEquals("connection lifecycle in state: CLOSING and not open for new work", e.getMessage());
    }
  }

  @Test
  public void createTable() throws TimeoutException {

    try (Connection conn = ds.getConnection()) {
      conn.operation("create table table1(i int)")
          .submit();
      CompletionStage<Result.Count> idF = conn.<Result.Count>countOperation("insert into table1(i) values(1)")
          .submit()
          .getCompletionStage();

      assertEquals(1, idF.toCompletableFuture().get(10, TimeUnit.SECONDS).getCount());
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
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
  public void rowProcessorOperation() throws InterruptedException, ExecutionException, TimeoutException {
    final Integer[] result = {null};
    try (Connection conn = ds.getConnection()) {
      //First do a normal query so that the connection has time to get established
      conn.rowProcessorOperation("select 321 as t")
          .rowProcessor(new Flow.Processor<Result.Row, String>() {
            Flow.Subscription publisherSubscription;

            final ExecutorService executor = Executors.newFixedThreadPool(4);
            Flow.Subscription subscription;
            ConcurrentLinkedQueue<String> dataItems;

            @Override
            public void subscribe(Flow.Subscriber<? super String> subscriber) {
            }

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
            }

            @Override
            public void onNext(Result.Row item) {
              result[0] = item.get("t", Integer.class);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable t) {
            }
          })
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

      assertEquals(Integer.valueOf(321), result[0]);
    }
  }
}