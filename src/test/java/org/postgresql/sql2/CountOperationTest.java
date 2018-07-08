package org.postgresql.sql2;

import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.ParameterizedRowCountOperation;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.Submission;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.sql2.testUtil.CollectorUtils;
import org.postgresql.sql2.testUtil.ConnectUtil;
import org.postgresql.sql2.testUtil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

public class CountOperationTest {
  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

  private static DataSource ds;

  @BeforeAll
  public static void setUp() {
    ds = ConnectUtil.openDB(postgres);
  }

  @AfterAll
  public static void tearDown() {
    ds.close();
  }

  @Test
  public void insertWithoutATable() {

    String sql = "insert into tab(id, name, answer) values ($1, $2, $3)";
    Submission sub;
    try (Connection conn = ds.getConnection()) {
      sub = conn.rowCountOperation(sql)
          .set("$1", 1, AdbaType.NUMERIC)
          .set("$2", "Deep Thought", AdbaType.VARCHAR)
          .set("$3", 42, AdbaType.NUMERIC)
          .submit();
    }
    try {
      ((CompletableFuture) sub.getCompletionStage()).join();
      fail("table 'tab' doesn't exist, so an exception should be thrown");
    } catch (CompletionException e) {
      assertEquals("jdk.incubator.sql2.SqlException: Severity: ERROR\n" +
          "Message: relation \"tab\" does not exist", e.getMessage());
    }
  }

  @Test
  public void insertWithATableWithWaitingBetween() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      conn.operation("create table tabForInsert(id int)")
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
      conn.rowCountOperation("insert into tabForInsert(id) values ($1)")
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
      conn.rowCountOperation("insert into tabForInsert(id) values ($1)")
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
  public void insertAfterClose() {

    String sql = "insert into tab(id, name, answer) values ($1, $2, $3)";
    try (Connection conn = ds.getConnection()) {
      conn.closeOperation()
          .submit();
      conn.rowCountOperation(sql)
          .set("$1", 1, AdbaType.NUMERIC)
          .set("$2", "Deep Thought", AdbaType.VARCHAR)
          .set("$3", 42, AdbaType.NUMERIC)
          .submit();
      fail("an IllegalStateException should have been thrown");
    } catch (IllegalStateException e) {
      assertEquals("connection lifecycle in state: CLOSING and not open for new work", e.getMessage());
    }
  }

  @Test
  public void insertAfterDeactivate() {

    String sql = "insert into tab(id, name, answer) values ($1, $2, $3)";
    try (Connection conn = ds.getConnection()) {
      conn.deactivate();
      conn.rowCountOperation(sql)
          .set("$1", 1, AdbaType.NUMERIC)
          .set("$2", "Deep Thought", AdbaType.VARCHAR)
          .set("$3", 42, AdbaType.NUMERIC)
          .submit();
      fail("an IllegalStateException should have been thrown");
    } catch (IllegalStateException e) {
      assertEquals("connection lifecycle in state: NEW_INACTIVE and not open for new work", e.getMessage());
    }
  }

  @Test
  public void createTable() throws TimeoutException, ExecutionException, InterruptedException {

    try (Connection conn = ds.getConnection()) {
      conn.operation("create table table1(i int)")
          .submit();
      CompletionStage<Result.RowCount> idF = conn.<Result.RowCount>rowCountOperation("insert into table1(i) values(1)")
          .submit()
          .getCompletionStage();

      assertEquals(1, idF.toCompletableFuture().get(10, TimeUnit.SECONDS).getCount());
    }
  }

  @Test
  public void insertAndReturnKey() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      conn.operation("create table tabWithKey(t serial primary key)")
          .submit();
      ParameterizedRowCountOperation countOp = conn.rowCountOperation("insert into tabWithKey(t) values ($1) returning t")
          .set("$1", 1, AdbaType.NUMERIC);
      Submission rowSub = countOp
          .returning("t")
          .collect(CollectorUtils.singleCollector(Integer.class))
          .submit();

      countOp.submit();
      Submission<Object> drop = conn.operation("drop table tabWithKey")
          .submit();

      assertEquals(1, rowSub.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS));
      assertNull(drop.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }
}
