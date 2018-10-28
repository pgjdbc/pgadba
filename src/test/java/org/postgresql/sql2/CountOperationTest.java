package org.postgresql.sql2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.postgresql.sql2.testutil.FutureUtil.get10;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.ParameterizedRowCountOperation;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.Session;
import jdk.incubator.sql2.Submission;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.sql2.testutil.CollectorUtils;
import org.postgresql.sql2.testutil.ConnectUtil;
import org.postgresql.sql2.testutil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

public class CountOperationTest {
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
  public void insertWithoutATable() throws InterruptedException, TimeoutException {

    String sql = "insert into tab(id, name, answer) values ($1, $2, $3)";
    Submission sub;
    try (Session session = ds.getSession()) {
      sub = session.rowCountOperation(sql)
          .set("$1", 1, AdbaType.NUMERIC)
          .set("$2", "Deep Thought", AdbaType.VARCHAR)
          .set("$3", 42, AdbaType.NUMERIC)
          .submit();
    }
    try {
      get10(sub.getCompletionStage());
      fail("table 'tab' doesn't exist, so an exception should be thrown");
    } catch (ExecutionException e) {
      assertEquals("relation \"tab\" does not exist", e.getCause().getMessage());
    }
  }

  @Test
  public void insertWithATableWithWaitingBetween() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      get10(session.operation("create table tabForInsert(id int)")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into tabForInsert(id) values ($1)")
          .set("$1", 1, AdbaType.NUMERIC)
          .submit().getCompletionStage());
      Long count = get10(session.<Long>rowOperation("select count(*) as t from tabForInsert")
          .collect(CollectorUtils.singleCollector(Long.class))
          .submit().getCompletionStage());
      get10(session.operation("drop table tabForInsert")
          .submit().getCompletionStage());

      assertEquals(Long.valueOf(1), count);
    }
  }

  @Test
  public void insertWithATableWithoutWaiting() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      session.operation("create table tabForInsert(id int)")
          .submit();
      session.rowCountOperation("insert into tabForInsert(id) values ($1)")
          .set("$1", 1, AdbaType.NUMERIC)
          .submit();
      Submission<Long> count = session.<Long>rowOperation("select count(*) as t from tabForInsert")
          .collect(CollectorUtils.singleCollector(Long.class))
          .submit();
      Submission<Object> drop = session.operation("drop table tabForInsert")
          .submit();

      assertEquals(Long.valueOf(1), get10(count.getCompletionStage()));
      assertNull(get10(drop.getCompletionStage()));
    }
  }

  @Test
  public void insertAfterClose() {

    String sql = "insert into tab(id, name, answer) values ($1, $2, $3)";
    try (Session session = ds.getSession()) {
      session.closeOperation()
          .submit();
      session.rowCountOperation(sql)
          .set("$1", 1, AdbaType.NUMERIC)
          .set("$2", "Deep Thought", AdbaType.VARCHAR)
          .set("$3", 42, AdbaType.NUMERIC)
          .submit();
      fail("an IllegalStateException should have been thrown");
    } catch (IllegalStateException e) {
      assertEquals("session lifecycle in state: CLOSING and not open for new work", e.getMessage());
    }
  }

  @Test
  public void createTable() throws TimeoutException, ExecutionException, InterruptedException {

    try (Session session = ds.getSession()) {
      session.operation("create table table1(i int)")
          .submit();
      CompletionStage<Result.RowCount> idF = session.<Result.RowCount>rowCountOperation("insert into table1(i) values(1)")
          .submit()
          .getCompletionStage();

      assertEquals(1, get10(idF).getCount());
    }
  }

  @Test
  public void insertAndReturnKey() throws InterruptedException, ExecutionException, TimeoutException {

    try (Session session = ds.getSession()) {
      session.operation("create table tabWithKey(t serial primary key)")
          .submit();
      ParameterizedRowCountOperation countOp = session.rowCountOperation("insert into tabWithKey(t) values ($1) returning t")
          .set("$1", 1, AdbaType.NUMERIC);
      Submission rowSub = countOp
          .returning("t")
          .collect(CollectorUtils.singleCollector(Integer.class))
          .submit();

      countOp.submit();
      Submission<Object> drop = session.operation("drop table tabWithKey")
          .submit();

      assertEquals(1, get10(rowSub.getCompletionStage()));
      assertNull(get10(drop.getCompletionStage()));
    }
  }
}
