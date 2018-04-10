package org.postgresql.sql2;

import jdk.incubator.sql2.AdbaConnectionProperty;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;

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
        .connectionProperty(AdbaConnectionProperty.TRANSACTION_ISOLATION,
            AdbaConnectionProperty.TransactionIsolation.REPEATABLE_READ)
        .build();

  }

  public static void createTable(DataSource ds, String tab, String id_int, String s, String answer_int) {

  }
}
