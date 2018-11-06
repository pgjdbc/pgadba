package org.postgresql.adba;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.adba.testutil.ConnectUtil;
import org.postgresql.adba.testutil.DatabaseHolder;
import org.postgresql.adba.util.TestLogHandler;
import org.testcontainers.containers.PostgreSQLContainer;

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
    try (Session session = ds.getSession()) {

      Logger logger = Logger.getLogger("my junit-test logger");
      TestLogHandler handler = new TestLogHandler();
      handler.setLevel(Level.ALL);
      logger.setUseParentHandlers(false);
      logger.addHandler(handler);
      logger.setLevel(Level.ALL);

      session.logger(logger);

      assertTrue(handler.checkMessage().startsWith("logger for connection "));
    }
  }
}
