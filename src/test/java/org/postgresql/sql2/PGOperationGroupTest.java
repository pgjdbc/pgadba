package org.postgresql.sql2;

import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.OperationGroup;
import jdk.incubator.sql2.Submission;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.sql2.operations.helpers.PGTransaction;
import org.postgresql.sql2.testUtil.CollectorUtils;
import org.postgresql.sql2.testUtil.ConnectUtil;
import org.postgresql.sql2.testUtil.DatabaseHolder;
import org.postgresql.sql2.testUtil.SimpleRowSubscriber;
import org.postgresql.sql2.util.PGCount;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class PGOperationGroupTest {
  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

  private static DataSource ds;

  @BeforeAll
  public static void setUp() {
    ds = ConnectUtil.openDB(postgres);

    ConnectUtil.createTable(ds, "tab",
        "id int", "name varchar(100)", "answer int");
  }

  @AfterAll
  public static void tearDown() {
    ds.close();
  }

  @Test
  public void groupOperationSumOfRowOperations() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      OperationGroup<Integer, Integer> operationGroup = conn.operationGroup();

      Submission<Integer> sub = operationGroup
          .collect(CollectorUtils.summingCollector())
          .submitHoldingForMoreMembers();
      operationGroup.rowOperation("select 1 as t")
          .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      operationGroup.rowOperation("select 2 as t")
          .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      operationGroup.releaseProhibitingMoreMembers();

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, SECONDS);
      assertEquals(Integer.valueOf(3), result);
    }
  }

  @Test
  public void groupOperationSumOfLocalOperations() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      OperationGroup<Integer, Integer> operationGroup = conn.operationGroup();

      Submission<Integer> sub = operationGroup
          .collect(CollectorUtils.summingCollector())
          .submitHoldingForMoreMembers();
      operationGroup.localOperation()
          .onExecution(() -> 1)
          .submit();
      operationGroup.localOperation()
          .onExecution(() -> 2)
          .submit();
      operationGroup.releaseProhibitingMoreMembers();

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, SECONDS);
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
          "$$  LANGUAGE plpgsql").submit().getCompletionStage().toCompletableFuture().get(10, SECONDS);

      OperationGroup<Integer, Integer> operationGroup = conn.operationGroup();

      Submission<Integer> sub = operationGroup
          .collect(CollectorUtils.summingCollector())
          .submitHoldingForMoreMembers();

      operationGroup.outOperation("select * from groupOperationSumOfOutParameterOperations(1) as result")
          .outParameter("$1", AdbaType.INTEGER)
          .apply((r) -> r.at("y").get(Integer.class)).submit()
          .getCompletionStage().toCompletableFuture().get(10, SECONDS);

      operationGroup.outOperation("select * from groupOperationSumOfOutParameterOperations(2) as result")
          .outParameter("$1", AdbaType.INTEGER)
          .apply((r) -> r.at("y").get(Integer.class)).submit()
          .getCompletionStage().toCompletableFuture().get(10, SECONDS);

      operationGroup.releaseProhibitingMoreMembers();

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, SECONDS);
      assertEquals(Integer.valueOf(3), result);
    }
  }

  @Test
  public void groupOperationSumOfRowPublisherOperations() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      OperationGroup<Integer, Integer> operationGroup = conn.operationGroup();
      CompletableFuture<Integer> result1 = new CompletableFuture<>();
      CompletableFuture<Integer> result2 = new CompletableFuture<>();

      Submission<Integer> sub = operationGroup
          .collect(CollectorUtils.summingCollector())
          .submitHoldingForMoreMembers();
      operationGroup.rowPublisherOperation("select 1 as t")
          .subscribe(new SimpleRowSubscriber(result1), result1).submit().getCompletionStage().toCompletableFuture().get(10, SECONDS);
      operationGroup.rowPublisherOperation("select 2 as t")
          .subscribe(new SimpleRowSubscriber(result2), result2).submit().getCompletionStage().toCompletableFuture().get(10, SECONDS);
      operationGroup.releaseProhibitingMoreMembers();

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, SECONDS);
      assertEquals(Integer.valueOf(3), result);
    }
  }

  @Test
  public void groupOperationSumOfCountOperations() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      OperationGroup<PGCount, Integer> operationGroup = conn.operationGroup();
      conn.operation("create table goCount(id int)").submit().getCompletionStage().toCompletableFuture().get(10, SECONDS);

      Submission<Integer> sub = operationGroup
          .collect(CollectorUtils.summingCountCollector())
          .submitHoldingForMoreMembers();
      operationGroup.rowCountOperation("insert into goCount(id) values(3)")
          .submit().getCompletionStage().toCompletableFuture().get(10, SECONDS);
      operationGroup.rowCountOperation("insert into goCount(id) values(6)")
          .submit().getCompletionStage().toCompletableFuture().get(10, SECONDS);
      operationGroup.releaseProhibitingMoreMembers();

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, SECONDS);
      assertEquals(Integer.valueOf(2), result);

      conn.operation("drop table goCount").submit().getCompletionStage().toCompletableFuture().get(10, SECONDS);
    }
  }

  @Test
  public void groupOperationSumOfArrayCountOperations() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      OperationGroup<List<PGCount>, Integer> operationGroup = conn.operationGroup();
      conn.operation("create table goACount(id int)").submit().getCompletionStage().toCompletableFuture().get(10, SECONDS);

      Submission<Integer> sub = operationGroup
          .collect(CollectorUtils.summingCountListCollector())
          .submitHoldingForMoreMembers();
      operationGroup.arrayRowCountOperation("insert into goACount(id) values($1)")
          .set("$1", Arrays.asList(1, 2, 3), AdbaType.INTEGER)
          .submit().getCompletionStage().toCompletableFuture().get(10, SECONDS);
      operationGroup.arrayRowCountOperation("insert into goACount(id) values($1)")
          .set("$1", Arrays.asList(4, 5, 6), AdbaType.INTEGER)
          .submit().getCompletionStage().toCompletableFuture().get(10, SECONDS);
      operationGroup.releaseProhibitingMoreMembers();

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, SECONDS);
      assertEquals(Integer.valueOf(6), result);

      conn.operation("drop table goACount").submit().getCompletionStage().toCompletableFuture().get(10, SECONDS);
    }
  }

  @Test
  public void tryToAddCatchOperationAfterProhibit() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      OperationGroup<Integer, Integer> operationGroup = conn.operationGroup();

      Submission<Integer> sub = operationGroup
          .collect(CollectorUtils.summingCollector())
          .submitHoldingForMoreMembers();
      operationGroup.rowOperation("select 1 as t")
          .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      operationGroup.releaseProhibitingMoreMembers();

      try {
        operationGroup.catchOperation()
            .submit();
        fail("an IllegalStateException should have been thrown");
      } catch (IllegalStateException e) {
        assertEquals("It's not permitted to add more operations after an OperationGroup has been released", e.getMessage());
      }

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, SECONDS);
      assertEquals(Integer.valueOf(1), result);
    }
  }

  @Test
  public void tryToAddArrayRowCountOperationAfterProhibit() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      OperationGroup<Integer, Integer> operationGroup = conn.operationGroup();

      Submission<Integer> sub = operationGroup
          .collect(CollectorUtils.summingCollector())
          .submitHoldingForMoreMembers();
      operationGroup.rowOperation("select 1 as t")
          .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      operationGroup.releaseProhibitingMoreMembers();

      try {
        operationGroup.arrayRowCountOperation("select 1 as t")
            .submit();
        fail("an IllegalStateException should have been thrown");
      } catch (IllegalStateException e) {
        assertEquals("It's not permitted to add more operations after an OperationGroup has been released", e.getMessage());
      }

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, SECONDS);
      assertEquals(Integer.valueOf(1), result);
    }
  }

  @Test
  public void tryToAddRowCountOperationAfterProhibit() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      OperationGroup<Integer, Integer> operationGroup = conn.operationGroup();

      Submission<Integer> sub = operationGroup
          .collect(CollectorUtils.summingCollector())
          .submitHoldingForMoreMembers();
      operationGroup.rowOperation("select 1 as t")
          .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      operationGroup.releaseProhibitingMoreMembers();

      try {
        operationGroup.rowCountOperation("select 1 as t")
            .submit();
        fail("an IllegalStateException should have been thrown");
      } catch (IllegalStateException e) {
        assertEquals("It's not permitted to add more operations after an OperationGroup has been released", e.getMessage());
      }

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, SECONDS);
      assertEquals(Integer.valueOf(1), result);
    }
  }

  @Test
  public void tryToAddOperationAfterProhibit() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      OperationGroup<Integer, Integer> operationGroup = conn.operationGroup();

      Submission<Integer> sub = operationGroup
          .collect(CollectorUtils.summingCollector())
          .submitHoldingForMoreMembers();
      operationGroup.rowOperation("select 1 as t")
          .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      operationGroup.releaseProhibitingMoreMembers();

      try {
        operationGroup.operation("select 1 as t")
            .submit();
        fail("an IllegalStateException should have been thrown");
      } catch (IllegalStateException e) {
        assertEquals("It's not permitted to add more operations after an OperationGroup has been released", e.getMessage());
      }

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, SECONDS);
      assertEquals(Integer.valueOf(1), result);
    }
  }

  @Test
  public void tryToAddOutOperationAfterProhibit() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      OperationGroup<Integer, Integer> operationGroup = conn.operationGroup();

      Submission<Integer> sub = operationGroup
          .collect(CollectorUtils.summingCollector())
          .submitHoldingForMoreMembers();
      operationGroup.rowOperation("select 1 as t")
          .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      operationGroup.releaseProhibitingMoreMembers();

      try {
        operationGroup.outOperation("select 1 as t")
            .submit();
        fail("an IllegalStateException should have been thrown");
      } catch (IllegalStateException e) {
        assertEquals("It's not permitted to add more operations after an OperationGroup has been released", e.getMessage());
      }

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, SECONDS);
      assertEquals(Integer.valueOf(1), result);
    }
  }

  @Test
  public void tryToAddRowOperationAfterProhibit() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      OperationGroup<Integer, Integer> operationGroup = conn.operationGroup();

      Submission<Integer> sub = operationGroup
          .collect(CollectorUtils.summingCollector())
          .submitHoldingForMoreMembers();
      operationGroup.rowOperation("select 1 as t")
          .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      operationGroup.releaseProhibitingMoreMembers();

      try {
        operationGroup.rowOperation("select 1 as t")
            .collect(CollectorUtils.singleCollector(Integer.class)).submit();
        fail("an IllegalStateException should have been thrown");
      } catch (IllegalStateException e) {
        assertEquals("It's not permitted to add more operations after an OperationGroup has been released", e.getMessage());
      }

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, SECONDS);
      assertEquals(Integer.valueOf(1), result);
    }
  }

  @Test
  public void tryToAddRowPublisherOperationAfterProhibit() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      OperationGroup<Integer, Integer> operationGroup = conn.operationGroup();

      Submission<Integer> sub = operationGroup
          .collect(CollectorUtils.summingCollector())
          .submitHoldingForMoreMembers();
      operationGroup.rowOperation("select 1 as t")
          .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      operationGroup.releaseProhibitingMoreMembers();

      try {
        operationGroup.rowPublisherOperation("select 1 as t")
            .submit();
        fail("an IllegalStateException should have been thrown");
      } catch (IllegalStateException e) {
        assertEquals("It's not permitted to add more operations after an OperationGroup has been released", e.getMessage());
      }

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, SECONDS);
      assertEquals(Integer.valueOf(1), result);
    }
  }

  @Test
  public void tryToAddEndTransactionOperationAfterProhibit() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      OperationGroup<Integer, Integer> operationGroup = conn.operationGroup();

      Submission<Integer> sub = operationGroup
          .collect(CollectorUtils.summingCollector())
          .submitHoldingForMoreMembers();
      operationGroup.rowOperation("select 1 as t")
          .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      operationGroup.releaseProhibitingMoreMembers();

      try {
        operationGroup.endTransactionOperation(new PGTransaction())
            .submit();
        fail("an IllegalStateException should have been thrown");
      } catch (IllegalStateException e) {
        assertEquals("It's not permitted to add more operations after an OperationGroup has been released", e.getMessage());
      }

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, SECONDS);
      assertEquals(Integer.valueOf(1), result);
    }
  }

  @Test
  public void tryToAddLocalOperationAfterProhibit() throws InterruptedException, ExecutionException, TimeoutException {

    try (Connection conn = ds.getConnection()) {
      OperationGroup<Integer, Integer> operationGroup = conn.operationGroup();

      Submission<Integer> sub = operationGroup
          .collect(CollectorUtils.summingCollector())
          .submitHoldingForMoreMembers();
      operationGroup.rowOperation("select 1 as t")
          .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      operationGroup.releaseProhibitingMoreMembers();

      try {
        operationGroup.localOperation()
            .submit();
        fail("an IllegalStateException should have been thrown");
      } catch (IllegalStateException e) {
        assertEquals("It's not permitted to add more operations after an OperationGroup has been released", e.getMessage());
      }

      Integer result = sub.getCompletionStage().toCompletableFuture().get(10, SECONDS);
      assertEquals(Integer.valueOf(1), result);
    }
  }
}
