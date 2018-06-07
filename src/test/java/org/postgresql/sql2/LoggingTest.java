package org.postgresql.sql2;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.sql2.testUtil.ConnectUtil;
import org.postgresql.sql2.testUtil.DatabaseHolder;
import org.postgresql.sql2.util.TestLogHandler;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

public class LoggingTest {
  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

  private static DataSource ds;

  @BeforeClass
  public static void setUp() {
    ds = ConnectUtil.openDB(postgres);
  }

  @AfterClass
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
