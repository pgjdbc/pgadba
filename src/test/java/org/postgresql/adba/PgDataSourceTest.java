package org.postgresql.adba;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.postgresql.adba.testutil.FutureUtil.get10;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import jdk.incubator.sql2.AdbaSessionProperty;
import jdk.incubator.sql2.AdbaSessionProperty.TransactionIsolation;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;
import jdk.incubator.sql2.Session;
import jdk.incubator.sql2.Session.Builder;
import org.junit.jupiter.api.Test;
import org.postgresql.adba.testutil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

public class PgDataSourceTest {
  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

  @Test
  public void builder() throws Exception {
    DataSource ds = DataSourceFactory.newFactory("org.postgresql.adba.PgDataSourceFactory")
        .builder()
        .url("jdbc:postgresql://" + postgres.getContainerIpAddress() + ":" + postgres.getMappedPort(5432)
            + "/" + postgres.getDatabaseName())
        .username(postgres.getUsername())
        .password(postgres.getPassword())
        .sessionProperty(AdbaSessionProperty.TRANSACTION_ISOLATION,
            AdbaSessionProperty.TransactionIsolation.REPEATABLE_READ)
        .build();
    Session con = ds.getSession();
    assertNotNull(con);
    Thread.sleep(300);
    //con.connect();
  }

  @Test
  public void close() {
    DataSource ds = DataSourceFactory.newFactory("org.postgresql.adba.PgDataSourceFactory")
        .builder()
        .url("jdbc:postgresql://" + postgres.getContainerIpAddress() + ":" + postgres.getMappedPort(5432)
            + "/" + postgres.getDatabaseName())
        .username(postgres.getUsername())
        .password(postgres.getPassword())
        .sessionProperty(AdbaSessionProperty.TRANSACTION_ISOLATION,
            AdbaSessionProperty.TransactionIsolation.REPEATABLE_READ)
        .build();
    Session con = ds.getSession();
    assertNotNull(con);
    ds.close();
    try {
      ds.getSession();
      fail("you are not allowed to start connection on a closed datasource");
    } catch (IllegalStateException e) {
      assertEquals("this datasource has already been closed", e.getMessage());
    }
  }

  @Test
  public void loginWithIncorrectPassword() throws InterruptedException, TimeoutException {
    DataSource ds = DataSourceFactory.newFactory("org.postgresql.adba.PgDataSourceFactory")
        .builder()
        .url("jdbc:postgresql://" + postgres.getContainerIpAddress() + ":" + postgres.getMappedPort(5432)
            + "/" + postgres.getDatabaseName())
        .username(postgres.getUsername())
        .password("wrong password " + postgres.getPassword())
        .sessionProperty(AdbaSessionProperty.TRANSACTION_ISOLATION,
            AdbaSessionProperty.TransactionIsolation.REPEATABLE_READ)
        .build();

    Session c = ds.getSession();

    try {
      get10(c.<Integer>rowOperation("select 1").submit().getCompletionStage());
      fail("shouldn't be possible to log in with the wrong password");
    } catch (ExecutionException ee) {
      assertEquals("password authentication failed for user \"test\"", ee.getCause().getMessage());
    }
  }

  @Test
  public void loginWithIncorrectUsername() throws InterruptedException, TimeoutException {
    DataSource ds = DataSourceFactory.newFactory("org.postgresql.adba.PgDataSourceFactory")
        .builder()
        .url("jdbc:postgresql://" + postgres.getContainerIpAddress() + ":" + postgres.getMappedPort(5432)
            + "/" + postgres.getDatabaseName())
        .username("wrong username " + postgres.getUsername())
        .password(postgres.getPassword())
        .sessionProperty(AdbaSessionProperty.TRANSACTION_ISOLATION,
            AdbaSessionProperty.TransactionIsolation.REPEATABLE_READ)
        .build();

    Session c = ds.getSession();

    try {
      get10(c.<Integer>rowOperation("select 1").submit().getCompletionStage());
      fail("shouldn't be possible to log in with the wrong password");
    } catch (ExecutionException ee) {
      assertEquals("password authentication failed for user \"wrong username test\"", ee.getCause().getMessage());
    }
  }

  @Test
  public void overrideSessionProperty() {
    DataSource ds = DataSourceFactory.newFactory("org.postgresql.adba.PgDataSourceFactory")
        .builder()
        .url("jdbc:postgresql://" + postgres.getContainerIpAddress() + ":" + postgres.getMappedPort(5432)
            + "/" + postgres.getDatabaseName())
        .username(postgres.getUsername())
        .password(postgres.getPassword())
        .sessionProperty(AdbaSessionProperty.TRANSACTION_ISOLATION,
            AdbaSessionProperty.TransactionIsolation.REPEATABLE_READ)
        .build();
    Builder builder = ds.builder();

    assertThrows(IllegalArgumentException.class, () -> builder.property(AdbaSessionProperty.TRANSACTION_ISOLATION,
        TransactionIsolation.READ_COMMITTED));
  }

  @Test
  public void overrideDefaultSessionProperty() {
    DataSource ds = DataSourceFactory.newFactory("org.postgresql.adba.PgDataSourceFactory")
        .builder()
        .url("jdbc:postgresql://" + postgres.getContainerIpAddress() + ":" + postgres.getMappedPort(5432)
            + "/" + postgres.getDatabaseName())
        .username(postgres.getUsername())
        .password(postgres.getPassword())
        .defaultSessionProperty(AdbaSessionProperty.TRANSACTION_ISOLATION,
            AdbaSessionProperty.TransactionIsolation.REPEATABLE_READ)
        .build();
    Builder builder = ds.builder();

    //This shouldn't throw, as it was set with the method defaultSessionProperty previously
    builder.property(AdbaSessionProperty.TRANSACTION_ISOLATION,
        TransactionIsolation.READ_COMMITTED);
  }
}
