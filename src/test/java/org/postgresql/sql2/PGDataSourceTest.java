package org.postgresql.sql2;

import static org.junit.Assert.*;

import org.junit.Test;

import java2.sql2.Connection;
import java2.sql2.DataSource;
import java2.sql2.DataSourceFactory;
import java2.sql2.JdbcConnectionProperty;

public class PGDataSourceTest {

  @Test
  public void builder() {
    DataSource ds = DataSourceFactory.forName("Postgres Database")
        .builder()
        .url("postgresql:database:@//localhost:5432/test")
        .username("test")
        .password("test")
        .connectionProperty(JdbcConnectionProperty.TRANSACTION_ISOLATION,
            JdbcConnectionProperty.TransactionIsolation.REPEATABLE_READ)
        .build();
    Connection con = ds.getConnection();
    con.connect();
  }

  @Test
  public void close() {
  }
}