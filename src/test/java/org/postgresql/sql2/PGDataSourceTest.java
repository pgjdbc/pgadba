package org.postgresql.sql2;

import static org.junit.Assert.*;

import jdk.incubator.sql2.AdbaConnectionProperty;
import org.junit.Test;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;

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
    Thread.sleep(10000);
  }

  @Test
  public void close() {
  }
}