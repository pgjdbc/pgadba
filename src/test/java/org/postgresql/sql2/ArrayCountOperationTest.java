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
import org.postgresql.sql2.util.PGCount;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ArrayCountOperationTest {
  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

  private static DataSource ds;

  @BeforeClass
  public static void setUp() {
    ds = ConnectUtil.openDB(postgres);
  }

  @AfterClass
  public static void tearDown() {
    ds.close();
  }


  @Test
  public void multiInsertWithATable() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      conn.rowCountOperation("create table tabForInsert(id int)")
          .submit();
      Submission<List<Integer>> arrayCount = conn.<List<Integer>>arrayRowCountOperation("insert into tabForInsert(id) values ($1)")
          .set("$1", new Integer[]{1, 2, 3}, AdbaType.NUMERIC)
          .submit();
      Submission<Long> count = conn.<Long>rowOperation("select count(*) as t from tabForInsert")
          .collect(CollectorUtils.singleCollector(Long.class))
          .submit();
      Submission<Object> drop = conn.operation("drop table tabForInsert")
          .submit();

      assertArrayEquals(new PGCount[]{new PGCount(1), new PGCount(1), new PGCount(1)}, arrayCount.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS).toArray());
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
      Submission<List<Integer>> arrayCount = conn.<List<Integer>>arrayRowCountOperation("insert into tabForInsert(id) values ($1)")
          .set("$1", f, AdbaType.NUMERIC)
          .submit();
      Submission<Long> count = conn.<Long>rowOperation("select count(*) as t from tabForInsert")
          .collect(CollectorUtils.singleCollector(Long.class))
          .submit();
      Submission<Object> drop = conn.operation("drop table tabForInsert")
          .submit();

      assertNull(noReturn.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS));
      assertArrayEquals(new PGCount[]{new PGCount(1), new PGCount(1), new PGCount(1)}, arrayCount.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS).toArray());
      assertEquals(Long.valueOf(3), count.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS));
      assertNull(drop.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }


  @Test
  public void multiInsertWithCustomCollector() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      conn.rowCountOperation("create table secondTabForInsert(id int)")
          .submit();
      Submission<List<Long>> arrayCount = conn.<List<Long>>arrayRowCountOperation("insert into secondTabForInsert(id) values ($1)")
          .set("$1", new Integer[]{1, 2, 3}, AdbaType.NUMERIC)
          .collect(Collector.of(
              () -> new ArrayList<Long>(),
              (a, r) -> a.add(r.getCount()),
              (l, r) -> null,
              a -> a
          ))
          .submit();
      Submission<Long> count = conn.<Long>rowOperation("select count(*) as t from secondTabForInsert")
          .collect(CollectorUtils.singleCollector(Long.class))
          .submit();
      Submission<Object> drop = conn.operation("drop table secondTabForInsert")
          .submit();

      assertArrayEquals(new Long[]{1L, 1L, 1L}, arrayCount.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS).toArray());
      assertEquals(Long.valueOf(3), count.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS));
      assertNull(drop.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }
}
