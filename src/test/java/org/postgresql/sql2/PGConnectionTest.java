package org.postgresql.sql2;

import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.SqlException;
import jdk.incubator.sql2.Submission;
import jdk.incubator.sql2.Transaction;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class PGConnectionTest {
  @ClassRule
  public static PostgreSQLContainer postgres = new PostgreSQLContainer();

  private static DataSource ds;

  @BeforeClass
  public static void setUp() throws Exception {
    ds = TestUtil.openDB(postgres);

    TestUtil.createTable(ds, "tab",
        "id int", "name varchar(100)", "answer int");
  }

  @Test
  public void trivialInsert() {

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
  public void createTable() {

    try (Connection conn = ds.getConnection()) {
      conn.countOperation("create table table1(i int)")
          .submit();
      CompletionStage<Result.Count> idF = conn.<Result.Count>countOperation("insert into table1(i) values(1)")
          .submit()
          .getCompletionStage();

      assertEquals(1, idF.toCompletableFuture().get().getCount());
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void exampleFromADBAOverJDBCProject() {
    // get a DataSource and a Connection
    try (DataSource ds = TestUtil.openDB(postgres);
         Connection conn = ds.getConnection(t -> System.out.println("ERROR: " + t.getMessage()))) {
      // get a Transaction
      Transaction trans = conn.transaction();
      // select the EMPNO of CLARK
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select empno, ename from emp where ename = $1 for update")
          .set("$1", "CLARK", AdbaType.VARCHAR)
          .collect(Collector.of(
              () -> new int[1],
              (a, r) -> {
                a[0] = r.get("empno", Integer.class);
              },
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();
      // update CLARK to work in department 50
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
          .onError(Throwable::printStackTrace)
          .submit();

      conn.catchErrors();  // resume normal execution if there were any errors
      conn.commitMaybeRollback(trans); // commit (or rollback) the transaction
    }
    // wait for the async tasks to complete before exiting
    ForkJoinPool.commonPool().awaitQuiescence(1, TimeUnit.MINUTES);
  }
}