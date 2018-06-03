package org.postgresql.sql2;

import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.SqlException;
import jdk.incubator.sql2.Transaction;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.sql2.testUtil.ConnectUtil;
import org.postgresql.sql2.util.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class FirstLight {
  public static final String TRIVIAL = "SELECT 1";

  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

  @BeforeClass
  public static void setup() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ConnectUtil.openDB(postgres).getConnection()) {
      conn.operation("create table emp(id int, empno int, ename varchar(10), deptno int)")
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
      conn.operation("insert into emp(id, empno, ename, deptno) values(1, 2, 'empname', 3)")
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }
  }

  /**
   * create a Connection and send a SQL to the database
   */
  @Test
  public void sqlOperation() {
    DataSource ds = ConnectUtil.openDB(postgres);
    Connection conn = ds.getConnection(t -> fail("ERROR: " + t.getMessage()));
    try (conn) {
      assertNotNull(conn);
      conn.operation(TRIVIAL).submit();
    }
    ForkJoinPool.commonPool().awaitQuiescence(1, TimeUnit.MINUTES);
  }

  /**
   * Execute a few trivial queries.
   */
  @Test
  public void rowOperation() {
    try (DataSource ds = ConnectUtil.openDB(postgres);
         Connection conn = ds.getConnection(t -> fail("ERROR: " + t.getMessage()))) {
      assertNotNull(conn);
      conn.<Void>rowOperation(TRIVIAL)
          .collect(Collector.of(() -> null,
              (a, r) -> assertEquals(Integer.valueOf(1), r.get("1", Integer.class)),
              (x, y) -> null))
          .submit();
      conn.<Integer>rowOperation("select * from emp")
          .collect(Collector.<Result.Row, int[], Integer>of(
              () -> new int[1],
              (int[] a, Result.Row r) -> {
                a[0] = a[0]+r.get("sal", Integer.class);
              },
              (l, r) -> l,
              a -> (Integer)a[0]))
          .submit()
          .getCompletionStage()
          .thenAccept( n -> assertEquals(Integer.valueOf(2), n))
          .toCompletableFuture();
      conn.<Integer>rowOperation("select * from emp where empno = $1")
          .set("$1", 7782)
          .collect(Collector.of(
              () -> null,
              (a, r) -> {
                System.out.println("salary: $" + r.get("sal", Integer.class));
              },
              (l, r) -> null))
          .submit();
    }
    ForkJoinPool.commonPool().awaitQuiescence(1, TimeUnit.MINUTES);
  }

  /**
   * check does error handling do anything
   */
  @Test
  public void errorHandling() {
    try (DataSource ds = ConnectUtil.openDB(postgres);
         Connection conn = ds.getConnection(t -> fail("ERROR: " + t.toString()))) {
      conn.<Void>rowOperation(TRIVIAL)
          .collect(Collector.of(() -> null,
              (a, r) -> assertEquals(Integer.valueOf(1), r.get("1", Integer.class)),
              (x, y) -> null))
          .onError( t -> { fail(t.toString()); })
          .submit();
    }

    try (DataSource ds = ConnectUtil.openDB(postgres);
         Connection conn = ds.getConnection(t -> System.out.println("ERROR: " + t.toString()))) {
      conn.<Integer>rowOperation("select * from emp where empno = $1")
          .set("$1", 7782)
          .collect(Collector.of(
              () -> null,
              (a, r) -> assertEquals(Integer.valueOf(1), r.get("sal", Integer.class)),
              (l, r) -> null))
          .onError( t -> { fail(t.getMessage()); } )
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
    try (DataSource ds = ConnectUtil.openDB(postgres);
         Connection conn = ds.getConnection(t -> fail("ERROR: " + t.toString()))) {
      Transaction trans = conn.transaction();
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select empno, ename from emp where ename = $1 for update")
          .set("$1", "CLARK", AdbaType.VARCHAR)
          .collect(Collector.of(
              () -> new int[1],
              (a, r) -> {a[0] = r.get("empno", Integer.class); },
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();
      idF.thenAccept( id -> { assertEquals(Integer.valueOf(1), id); } );
      conn.<Long>countOperation("update emp set deptno = $1 where empno = $2")
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
          .thenAccept( c -> { assertEquals(Long.valueOf(1), c); } );
      conn.catchErrors();
      conn.commitMaybeRollback(trans);
    }
    ForkJoinPool.commonPool().awaitQuiescence(1, TimeUnit.MINUTES);
  }
}
