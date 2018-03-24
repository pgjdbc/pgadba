package org.postgresql.sql2;

import java2.sql2.DataSource;
import java2.sql2.DataSourceFactory;
import java2.sql2.JdbcConnectionProperty;

public class TestUtil {
  public static DataSource openDB() {
    try {
      Class.forName("org.postgresql.sql2.PGDataSourceFactory", true, ClassLoader.getSystemClassLoader());
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    return DataSourceFactory.forName("Postgres Database")
        .builder()
        .url("postgresql:database:@//localhost:5432/test")
        .username("test")
        .password("test")
        .connectionProperty(JdbcConnectionProperty.TRANSACTION_ISOLATION,
            JdbcConnectionProperty.TransactionIsolation.REPEATABLE_READ)
        .build();

  }

  public static void createTable(DataSource ds, String tab, String id_int, String s, String answer_int) {

  }
}
