package org.postgresql.adba;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.postgresql.adba.testutil.CollectorUtils.singleCollector;
import static org.postgresql.adba.testutil.FutureUtil.get10;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Session;
import jdk.incubator.sql2.TransactionCompletion;
import jdk.incubator.sql2.TransactionOutcome;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.adba.testutil.ConnectUtil;
import org.postgresql.adba.testutil.DatabaseHolder;
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
    try (Session session = ds.getSession()) {
      TransactionCompletion transaction = session.transactionCompletion();
      session.operation("start transaction")
          .submit();
      session.operation("create table tab(i int)")
          .submit();
      session.rowCountOperation("insert into tab(i) values(123)")
          .submit();
      transaction.setRollbackOnly();
      CompletionStage<TransactionOutcome> roll = session.commitMaybeRollback(transaction);

      assertEquals(TransactionOutcome.ROLLBACK, get10(roll));

      CompletionStage<Boolean> idF = session.<Boolean>rowOperation("SELECT EXISTS (\n"
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

      assertFalse(get10(idF));
    }
  }

  @Test
  public void insertAndCommit() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      TransactionCompletion transaction = session.transactionCompletion();
      session.operation("start transaction")
          .submit();
      session.operation("create table tab(i int)")
          .submit();
      session.rowCountOperation("insert into tab(i) values(123)")
          .submit();
      CompletionStage<TransactionOutcome> roll = session.commitMaybeRollback(transaction);

      assertEquals(TransactionOutcome.COMMIT, get10(roll));

      CompletionStage<Long> idF = session.<Long>rowOperation("select count(*) as t from tab")
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(1), get10(idF));

      get10(session.rowCountOperation("drop table tab")
          .submit().getCompletionStage());
    }
  }

}
