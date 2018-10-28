package org.postgresql.sql2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.postgresql.sql2.testutil.FutureUtil.get10;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;
import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.Session;
import jdk.incubator.sql2.SqlException;
import jdk.incubator.sql2.TransactionCompletion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.sql2.testutil.ConnectUtil;
import org.postgresql.sql2.testutil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

public class FirstLight {
  public static final String TRIVIAL = "SELECT 1";

  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

  @BeforeAll
  public static void setup() throws InterruptedException, ExecutionException, TimeoutException {
    try (Session session = ConnectUtil.openDb(postgres).getSession()) {
      get10(session.operation("create table emp(id int, empno int, ename varchar(10), deptno int)")
          .submit().getCompletionStage());
      get10(session.operation("insert into emp(id, empno, ename, deptno) values(1, 2, 'empname', 3)")
          .submit().getCompletionStage());
    }
  }

  /**
   * create a Session and send a SQL to the database.
   */
  @Test
  public void sqlOperation() {
    DataSource ds = ConnectUtil.openDb(postgres);
    Session session = ds.getSession(t -> fail("ERROR: " + t.getMessage()));
    try (session) {
      assertNotNull(session);
      session.operation(TRIVIAL).submit();
    }
    ForkJoinPool.commonPool().awaitQuiescence(1, TimeUnit.MINUTES);
  }

  /**
   * Execute a few trivial queries.
   */
  @Test
  public void rowOperation() {
    try (DataSource ds = ConnectUtil.openDb(postgres);
         Session session = ds.getSession(t -> fail("ERROR: " + t.getMessage()))) {
      assertNotNull(session);
      session.<Void>rowOperation(TRIVIAL)
          .collect(Collector.of(() -> null,
              (a, r) -> assertEquals(Integer.valueOf(1), r.at("1").get(Integer.class)),
              (x, y) -> null))
          .submit();
      session.<Integer>rowOperation("select * from emp")
          .collect(Collector.<Result.RowColumn, int[], Integer>of(
              () -> new int[1],
              (int[] a, Result.RowColumn r) -> {
                a[0] = a[0] + r.at("sal").get(Integer.class);
              },
              (l, r) -> l,
              a -> (Integer)a[0]))
          .submit()
          .getCompletionStage()
          .thenAccept(n -> assertEquals(Integer.valueOf(2), n))
          .toCompletableFuture();
      session.<Integer>rowOperation("select * from emp where empno = $1")
          .set("$1", 7782)
          .collect(Collector.of(
              () -> null,
              (a, r) -> {
                System.out.println("salary: $" + r.at("sal").get(Integer.class));
              },
              (l, r) -> null))
          .submit();
    }
    ForkJoinPool.commonPool().awaitQuiescence(1, TimeUnit.MINUTES);
  }

  /**
   * check does error handling do anything.
   */
  @Test
  public void errorHandling() {
    try (DataSource ds = ConnectUtil.openDb(postgres);
         Session session = ds.getSession(t -> fail("ERROR: " + t.toString()))) {
      session.<Void>rowOperation(TRIVIAL)
          .collect(Collector.of(() -> null,
              (a, r) -> assertEquals(Integer.valueOf(1), r.at("1").get(Integer.class)),
              (x, y) -> null))
          .onError(t -> fail(t.toString()))
          .submit();
    }

    try (DataSource ds = ConnectUtil.openDb(postgres);
         Session session = ds.getSession(t -> System.out.println("ERROR: " + t.toString()))) {
      session.<Integer>rowOperation("select * from emp where empno = $1")
          .set("$1", 7782)
          .collect(Collector.of(
              () -> null,
              (a, r) -> assertEquals(Integer.valueOf(1), r.at("sal").get(Integer.class)),
              (l, r) -> null))
          .onError(t -> fail(t.getMessage()))
          .submit();
    }
    ForkJoinPool.commonPool().awaitQuiescence(1, TimeUnit.MINUTES);
  }

  /**
   * Do something that approximates real work. Do a transaction. Uses
   * Transaction, CompletionStage args, and catch Operation.
   */
  @Test
  public void transaction() {
    try (DataSource ds = ConnectUtil.openDb(postgres);
         Session session = ds.getSession(t -> fail("ERROR: " + t.toString()))) {
      TransactionCompletion trans = session.transactionCompletion();
      CompletionStage<Integer> idF = session.<Integer>rowOperation("select empno, ename from emp where ename = $1 for update")
          .set("$1", "CLARK", AdbaType.VARCHAR)
          .collect(Collector.of(
              () -> new int[1],
              (a, r) -> a[0] = r.at("empno").get(Integer.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();
      idF.thenAccept(id -> assertEquals(Integer.valueOf(1), id));
      session.<Long>rowCountOperation("update emp set deptno = $1 where empno = $2")
          .set("$1", 50, AdbaType.INTEGER)
          .set("$2", idF, AdbaType.INTEGER)
          .apply(c -> {
            if (c.getCount() != 1L) {
              trans.setRollbackOnly();
              throw new SqlException("updated wrong number of rows", null, null, -1, null, -1);
            }
            return c.getCount();
          })
          .onError(t -> fail(t.getMessage()))
          .submit()
          .getCompletionStage()
          .thenAccept(c -> assertEquals(Long.valueOf(1), c));
      session.catchErrors();
      session.commitMaybeRollback(trans);
    }
    ForkJoinPool.commonPool().awaitQuiescence(1, TimeUnit.MINUTES);
  }
}
