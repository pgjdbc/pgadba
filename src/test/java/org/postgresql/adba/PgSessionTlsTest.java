package org.postgresql.adba;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.postgresql.adba.testutil.FutureUtil.get10;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.adba.testutil.CollectorUtils;
import org.postgresql.adba.testutil.ConnectUtil;
import org.postgresql.adba.testutil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

public class PgSessionTlsTest {
  public static PostgreSQLContainer postgresTls11WithScram = DatabaseHolder.getNew11();
  public static PostgreSQLContainer postgresTls = DatabaseHolder.getNewWithTls();
  public static PostgreSQLContainer postgres = DatabaseHolder.getNew();

  private static DataSource ds;
  private static DataSource dsTls;
  private static DataSource dsWithoutTls;
  private static DataSource dsTls11WithScram;

  @BeforeAll
  public static void setUp() {
    ds = ConnectUtil.openDb(postgresTls);
    dsTls = ConnectUtil.openDbWithTls(postgresTls);
    dsWithoutTls = ConnectUtil.openDbWithTls(postgres);
    dsTls11WithScram = ConnectUtil.openDbWithTls(postgresTls11WithScram);

    ConnectUtil.createTable(ds, "tab",
        "id int", "name varchar(100)", "answer int");
  }

  @AfterAll
  public static void tearDown() {
    ds.close();
  }

  @Test
  public void connectWithoutTlsToTlsOnlyDb() throws InterruptedException, ExecutionException, TimeoutException {

    String sql = "select 1 as t";
    try (Session session = ds.getSession()) {
      get10(session.rowOperation(sql)
          .collect(CollectorUtils.singleCollector(Integer.class))
          .submit().getCompletionStage());
      fail("Exception should have been thrown, as the connection properties doesn't include TLS");
    } catch (ExecutionException e) {
      assertEquals("no pg_hba.conf entry for host \"172.17.0.1\", user \"test\", database \"test\", SSL off",
          e.getCause().getMessage());
    }
  }

  @Test
  public void connectWithTls() throws InterruptedException, ExecutionException, TimeoutException, URISyntaxException {
    URL resource = PgSessionTlsTest.class.getResource("/keystore.jks");
    System.setProperty("javax.net.ssl.trustStore", String.valueOf(Paths.get(resource.toURI()).toFile()));
    System.setProperty("javax.net.ssl.trustStorePassword","changeit");
    String sql = "select 1 as t";
    try (Session session = dsTls.getSession()) {
      Integer result = session.<Integer>rowOperation(sql)
          .collect(CollectorUtils.singleCollector(Integer.class))
          .submit().getCompletionStage().toCompletableFuture().get(30, TimeUnit.SECONDS);
      assertEquals(Integer.valueOf(1), result);
    }
    System.clearProperty("javax.net.ssl.trustStore");
    System.clearProperty("javax.net.ssl.trustStorePassword");
  }

  @Test
  public void connectWithTlsScram() throws InterruptedException, ExecutionException, TimeoutException, URISyntaxException {
    URL resource = PgSessionTlsTest.class.getResource("/keystore.jks");
    System.setProperty("javax.net.ssl.trustStore", String.valueOf(Paths.get(resource.toURI()).toFile()));
    System.setProperty("javax.net.ssl.trustStorePassword","changeit");
    String sql = "select 2 as t";
    try (Session session = dsTls11WithScram.getSession()) {
      Integer result = session.<Integer>rowOperation(sql)
          .collect(CollectorUtils.singleCollector(Integer.class))
          .submit().getCompletionStage().toCompletableFuture().get(30, TimeUnit.SECONDS);
      assertEquals(Integer.valueOf(2), result);
    }
    System.clearProperty("javax.net.ssl.trustStore");
    System.clearProperty("javax.net.ssl.trustStorePassword");
  }

  @Test
  public void connectWithTlsToDbWithoutTls() throws InterruptedException, TimeoutException, URISyntaxException {
    URL resource = PgSessionTlsTest.class.getResource("/keystore.jks");
    System.setProperty("javax.net.ssl.trustStore", String.valueOf(Paths.get(resource.toURI()).toFile()));
    System.setProperty("javax.net.ssl.trustStorePassword","changeit");
    String sql = "select 1 as t";
    try (Session session = dsWithoutTls.getSession()) {
      Integer result = session.<Integer>rowOperation(sql)
          .collect(CollectorUtils.singleCollector(Integer.class))
          .submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
      assertEquals(Integer.valueOf(1), result);
    } catch (ExecutionException e) {
      assertEquals("server doesn't support TLS, but TLS was required",
          e.getCause().getMessage());
    }
    System.clearProperty("javax.net.ssl.trustStore");
    System.clearProperty("javax.net.ssl.trustStorePassword");
  }
}