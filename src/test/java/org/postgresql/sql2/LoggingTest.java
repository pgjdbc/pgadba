package org.postgresql.sql2;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.sql2.testutil.ConnectUtil;
import org.postgresql.sql2.testutil.DatabaseHolder;
import org.postgresql.sql2.util.TestLogHandler;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoggingTest {
  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

  private static DataSource ds;

  @BeforeAll
  public static void setUp() {
    ds = ConnectUtil.openDb(postgres);
  }

  @AfterAll
  public static void tearDown() {
    ds.close();
  }

  @Test
  public void supplyLogger() {
    try (Connection conn = ds.getConnection()) {

      Logger logger = Logger.getLogger("my junit-test logger");
      TestLogHandler handler = new TestLogHandler();
      handler.setLevel(Level.ALL);
      logger.setUseParentHandlers(false);
      logger.addHandler(handler);
      logger.setLevel(Level.ALL);

      conn.logger(logger);

      assertTrue(handler.checkMessage().startsWith("logger for connection "));
    }
  }
}
