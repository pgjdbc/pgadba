package org.postgresql.sql2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import jdk.incubator.sql2.AdbaConnectionProperty;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;
import org.junit.jupiter.api.Test;
import org.postgresql.sql2.testutil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

public class PgDataSourceTest {
  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

  @Test
  public void builder() throws Exception {
    DataSource ds = DataSourceFactory.newFactory("org.postgresql.sql2.PgDataSourceFactory")
        .builder()
        .url("jdbc:postgresql://" + postgres.getContainerIpAddress() + ":" + postgres.getMappedPort(5432)
            + "/" + postgres.getDatabaseName())
        .username(postgres.getUsername())
        .password(postgres.getPassword())
        .connectionProperty(AdbaConnectionProperty.TRANSACTION_ISOLATION,
            AdbaConnectionProperty.TransactionIsolation.REPEATABLE_READ)
        .build();
    Connection con = ds.getConnection();
    assertNotNull(con);
    Thread.sleep(300);
    //con.connect();
  }

  @Test
  public void close() throws InterruptedException, ExecutionException, TimeoutException {
    DataSource ds = DataSourceFactory.newFactory("org.postgresql.sql2.PgDataSourceFactory")
        .builder()
        .url("jdbc:postgresql://" + postgres.getContainerIpAddress() + ":" + postgres.getMappedPort(5432)
            + "/" + postgres.getDatabaseName())
        .username(postgres.getUsername())
        .password(postgres.getPassword())
        .connectionProperty(AdbaConnectionProperty.TRANSACTION_ISOLATION,
            AdbaConnectionProperty.TransactionIsolation.REPEATABLE_READ)
        .build();
    Connection con = ds.getConnection();
    assertNotNull(con);
    ds.close();
    try {
      ds.getConnection();
      fail("you are not allowed to start connection on a closed datasource");
    } catch (IllegalStateException e) {
      assertEquals("this datasource has already been closed", e.getMessage());
    }
  }
}