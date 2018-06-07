package org.postgresql.sql2;

import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.OperationGroup;
import jdk.incubator.sql2.Submission;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.sql2.testUtil.CollectorUtils;
import org.postgresql.sql2.testUtil.ConnectUtil;
import org.postgresql.sql2.util.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;

import static org.junit.Assert.assertEquals;

public class PGOperationGroupTest {
  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

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
  }

  @Test
  public void groupOperationSumOfRowOperations() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      OperationGroup<Integer, Integer> operationGroup = conn.operationGroup();

      Submission<Integer> sub = operationGroup
          .collect(Collector.of(
              () -> new Integer[] {0},
              (a, r) -> {
                a[0] += r;
              },
              (l, r) -> null,
              a -> a[0]))
          .submitHoldingForMoreMembers();
      operationGroup.rowOperation("select 1 as t")
          .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      operationGroup.rowOperation("select 2 as t")
          .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      operationGroup.releaseProhibitingMoreMembers();

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
      assertEquals(Integer.valueOf(3), result);
    }
  }

  @Test
  public void groupOperationSumOfLocalOperations() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      OperationGroup<Integer, Integer> operationGroup = conn.operationGroup();

      Submission<Integer> sub = operationGroup
          .collect(Collector.of(
              () -> new Integer[] {0},
              (a, r) -> {
                a[0] += r;
              },
              (l, r) -> null,
              a -> a[0]))
          .submitHoldingForMoreMembers();
      operationGroup.localOperation()
          .onExecution(() -> 1)
          .submit();
      operationGroup.localOperation()
          .onExecution(() -> 2)
          .submit();
      operationGroup.releaseProhibitingMoreMembers();

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
      assertEquals(Integer.valueOf(3), result);
    }
  }

  @Test
  public void groupOperationSumOfOutParameterOperations() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      conn.operation("CREATE OR REPLACE FUNCTION groupOperationSumOfOutParameterOperations(x integer, OUT y integer)\n" +
          "AS $$\n" +
          "BEGIN\n" +
          "   y := x;\n" +
          "END;\n" +
          "$$  LANGUAGE plpgsql").submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

      OperationGroup<Integer, Integer> operationGroup = conn.operationGroup();

      Submission<Integer> sub = operationGroup
          .collect(Collector.of(
              () -> new Integer[] {0},
              (a, r) -> {
                a[0] += r;
              },
              (l, r) -> null,
              a -> a[0]))
          .submitHoldingForMoreMembers();

      operationGroup.outOperation("select * from groupOperationSumOfOutParameterOperations(1) as result")
          .outParameter("$1", AdbaType.INTEGER)
          .apply((r) -> r.get("y", Integer.class)).submit()
          .getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

      operationGroup.outOperation("select * from groupOperationSumOfOutParameterOperations(2) as result")
          .outParameter("$1", AdbaType.INTEGER)
          .apply((r) -> r.get("y", Integer.class)).submit()
          .getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

      operationGroup.releaseProhibitingMoreMembers();

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
      assertEquals(Integer.valueOf(3), result);
    }
  }
}
