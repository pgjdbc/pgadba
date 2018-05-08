package org.postgresql.sql2;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Transaction;
import jdk.incubator.sql2.TransactionOutcome;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.postgresql.sql2.testUtil.ConnectUtil;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.postgresql.sql2.testUtil.CollectorUtils.singleCollector;

public class TransactionTest {
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
  public void insertAndRollback() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      Transaction transaction = conn.transaction();
      conn.countOperation("start transaction")
          .submit();
      conn.countOperation("create table tab(i int)")
          .submit();
      conn.countOperation("insert into tab(i) values(123)")
          .submit();
      transaction.setRollbackOnly();
      CompletionStage<TransactionOutcome> roll = conn.commitMaybeRollback(transaction);

      assertEquals(TransactionOutcome.ROLLBACK, roll.toCompletableFuture().get());

      CompletionStage<Boolean> idF = conn.<Boolean>rowOperation("SELECT EXISTS (\n" +
          "   SELECT 1 \n" +
          "   FROM   pg_catalog.pg_class c\n" +
          "   JOIN   pg_catalog.pg_namespace n ON n.oid = c.relnamespace\n" +
          "   WHERE  n.nspname = 'public'\n" +
          "   AND    c.relname = 'tab'\n" +
          "   AND    c.relkind = 'r'    -- only tables\n" +
          "   ) as t")
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertFalse(idF.toCompletableFuture().get());
    }
  }

  @Test
  public void insertAndCommit() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      Transaction transaction = conn.transaction();
      conn.countOperation("start transaction")
          .submit();
      conn.countOperation("create table tab(i int)")
          .submit();
      conn.countOperation("insert into tab(i) values(123)")
          .submit();
      CompletionStage<TransactionOutcome> roll = conn.commitMaybeRollback(transaction);

      assertEquals(TransactionOutcome.COMMIT, roll.toCompletableFuture().get());

      CompletionStage<Long> idF = conn.<Long>rowOperation("select count(*) as t from tab")
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(1), idF.toCompletableFuture().get());

      conn.countOperation("drop table tab")
          .submit().getCompletionStage().toCompletableFuture().get();
    }
  }

}
