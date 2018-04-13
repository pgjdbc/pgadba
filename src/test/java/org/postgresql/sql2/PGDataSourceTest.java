package org.postgresql.sql2;

import jdk.incubator.sql2.AdbaConnectionProperty;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;
import org.junit.Test;

public class PGDataSourceTest {

  @Test
  public void builder() throws Exception {
    DataSource ds = DataSourceFactory.forName("org.postgresql.sql2.PGDataSourceFactory")
        .builder()
        .url("jdbc:postgresql://localhost/test")
        .username("test")
        .password("test")
        .connectionProperty(AdbaConnectionProperty.TRANSACTION_ISOLATION,
            AdbaConnectionProperty.TransactionIsolation.REPEATABLE_READ)
        .build();
    Connection con = ds.getConnection();
    //con.connect();
  }

  @Test
  public void close() {
  }
}