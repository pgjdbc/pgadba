package org.postgresql.sql2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.postgresql.sql2.testutil.CollectorUtils.singleCollector;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Transaction;
import jdk.incubator.sql2.TransactionOutcome;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.sql2.testutil.ConnectUtil;
import org.postgresql.sql2.testutil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

public class TransactionTest {
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
  public void insertAndRollback() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      Transaction transaction = conn.transaction();
      conn.operation("start transaction")
          .submit();
      conn.operation("create table tab(i int)")
          .submit();
      conn.rowCountOperation("insert into tab(i) values(123)")
          .submit();
      transaction.setRollbackOnly();
      CompletionStage<TransactionOutcome> roll = conn.commitMaybeRollback(transaction);

      assertEquals(TransactionOutcome.ROLLBACK, roll.toCompletableFuture().get(10, TimeUnit.SECONDS));

      CompletionStage<Boolean> idF = conn.<Boolean>rowOperation("SELECT EXISTS (\n"
          + "   SELECT 1 \n"
          + "   FROM   pg_catalog.pg_class c\n"
          + "   JOIN   pg_catalog.pg_namespace n ON n.oid = c.relnamespace\n"
          + "   WHERE  n.nspname = 'public'\n"
          + "   AND    c.relname = 'tab'\n"
          + "   AND    c.relkind = 'r'    -- only tables\n"
          + "   ) as t")
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertFalse(idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void insertAndCommit() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      Transaction transaction = conn.transaction();
      conn.operation("start transaction")
          .submit();
      conn.operation("create table tab(i int)")
          .submit();
      conn.rowCountOperation("insert into tab(i) values(123)")
          .submit();
      CompletionStage<TransactionOutcome> roll = conn.commitMaybeRollback(transaction);

      assertEquals(TransactionOutcome.COMMIT, roll.toCompletableFuture().get(10, TimeUnit.SECONDS));

      CompletionStage<Long> idF = conn.<Long>rowOperation("select count(*) as t from tab")
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(1), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));

      conn.rowCountOperation("drop table tab")
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }
  }

}
