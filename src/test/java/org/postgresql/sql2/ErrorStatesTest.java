package org.postgresql.sql2;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.SqlException;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class ErrorStatesTest {
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
  public void testSqlError() throws InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select select")
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), idF.toCompletableFuture().get());
    } catch (ExecutionException e) {
      SqlException ex = (SqlException)e.getCause();

      assertEquals("Severity: ERROR\n" +
          "Message: syntax error at or near \"select\"", ex.getMessage());
      assertEquals("42601", ex.getSqlState());
      assertEquals("select select", ex.getSqlString());
    }
  }
}
