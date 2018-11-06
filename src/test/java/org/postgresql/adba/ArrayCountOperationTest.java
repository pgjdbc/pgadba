package org.postgresql.adba;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.postgresql.adba.testutil.FutureUtil.get10;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;
import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Session;
import jdk.incubator.sql2.Submission;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.adba.testutil.CollectorUtils;
import org.postgresql.adba.testutil.ConnectUtil;
import org.postgresql.adba.testutil.DatabaseHolder;
import org.postgresql.adba.util.PgCount;
import org.testcontainers.containers.PostgreSQLContainer;

public class ArrayCountOperationTest {
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
  public void multiInsertWithATable() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      session.rowCountOperation("create table tabForInsert(id int)")
          .submit();
      Submission<List<Integer>> arrayCount =
          session.<List<Integer>>arrayRowCountOperation("insert into tabForInsert(id) values ($1)")
          .set("$1", new Integer[]{1, 2, 3}, AdbaType.NUMERIC)
          .submit();
      Submission<Long> count = session.<Long>rowOperation("select count(*) as t from tabForInsert")
          .collect(CollectorUtils.singleCollector(Long.class))
          .submit();
      Submission<Object> drop = session.operation("drop table tabForInsert")
          .submit();

      assertArrayEquals(new PgCount[]{new PgCount(1), new PgCount(1), new PgCount(1)},
          get10(arrayCount.getCompletionStage()).toArray());
      assertEquals(Long.valueOf(3), get10(count.getCompletionStage()));
      assertNull(get10(drop.getCompletionStage()));
    }
  }

  @Test
  public void multiInsertFutureWithATable() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Integer[]> f = CompletableFuture.supplyAsync(() -> new Integer[]{1, 2, 3});
    try (Session session = ds.getSession()) {
      Submission<Object> noReturn = session.operation("create table tabForInsert(id int)")
          .submit();
      Submission<List<Integer>> arrayCount =
          session.<List<Integer>>arrayRowCountOperation("insert into tabForInsert(id) values ($1)")
          .set("$1", f, AdbaType.NUMERIC)
          .submit();
      Submission<Long> count = session.<Long>rowOperation("select count(*) as t from tabForInsert")
          .collect(CollectorUtils.singleCollector(Long.class))
          .submit();
      Submission<Object> drop = session.operation("drop table tabForInsert")
          .submit();

      assertNull(get10(noReturn.getCompletionStage()));
      assertArrayEquals(new PgCount[]{new PgCount(1), new PgCount(1), new PgCount(1)},
          get10(arrayCount.getCompletionStage()).toArray());
      assertEquals(Long.valueOf(3), get10(count.getCompletionStage()));
      assertNull(get10(drop.getCompletionStage()));
    }
  }


  @Test
  public void multiInsertWithCustomCollector() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      session.rowCountOperation("create table secondTabForInsert(id int)")
          .submit();
      Submission<List<Long>> arrayCount =
          session.<List<Long>>arrayRowCountOperation("insert into secondTabForInsert(id) values ($1)")
          .set("$1", new Integer[]{1, 2, 3}, AdbaType.NUMERIC)
          .collect(Collector.of(
              () -> new ArrayList<Long>(),
              (a, r) -> a.add(r.getCount()),
              (l, r) -> null,
              a -> a
          ))
          .submit();
      Submission<Long> count = session.<Long>rowOperation("select count(*) as t from secondTabForInsert")
          .collect(CollectorUtils.singleCollector(Long.class))
          .submit();
      Submission<Object> drop = session.operation("drop table secondTabForInsert")
          .submit();

      assertArrayEquals(new Long[]{1L, 1L, 1L},
          get10(arrayCount.getCompletionStage()).toArray());
      assertEquals(Long.valueOf(3), get10(count.getCompletionStage()));
      assertNull(get10(drop.getCompletionStage()));
    }
  }
}
