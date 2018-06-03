package org.postgresql.sql2;

import jdk.incubator.sql2.AdbaConnectionProperty;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;
import org.junit.Test;
import org.postgresql.sql2.util.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.Assert.assertNotNull;

public class PGDataSourceTest {
  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

  @Test
  public void builder() throws Exception {
    DataSource ds = DataSourceFactory.forName("org.postgresql.sql2.PGDataSourceFactory")
        .builder()
        .url("jdbc:postgresql://" + postgres.getContainerIpAddress() + ":" + postgres.getMappedPort(5432) +
            "/" + postgres.getDatabaseName())
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
  public void close() {
  }
}