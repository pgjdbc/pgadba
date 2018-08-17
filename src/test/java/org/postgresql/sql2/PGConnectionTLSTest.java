package org.postgresql.sql2;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.sql2.testUtil.CollectorUtils;
import org.postgresql.sql2.testUtil.ConnectUtil;
import org.postgresql.sql2.testUtil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class PGConnectionTLSTest {
  public static PostgreSQLContainer postgres = DatabaseHolder.getNewWithTLS();

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
  public void connectWithoutTLStoTLSOnlyDB() throws InterruptedException, ExecutionException, TimeoutException {

    String sql = "select 1 as t";
    try (Connection conn = ds.getConnection()) {
      conn.rowOperation(sql)
          .collect(CollectorUtils.singleCollector(Integer.class))
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
      fail("Exception should have been thrown, as the connection properties doesn't include TLS");
    } catch (ExecutionException e) {
      assertEquals("no pg_hba.conf entry for host \"172.17.0.1\", user \"test\", database \"test\", SSL off", e.getCause().getMessage());
    }
  }
}