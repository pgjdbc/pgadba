package org.postgresql.sql2;

import jdk.incubator.sql2.AdbaConnectionProperty;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;

public class TestUtil {
  public static DataSource openDB() {
    return DataSourceFactory.forName("org.postgresql.sql2.PGDataSourceFactory")
        .builder()
        .url("jdbc:postgresql://localhost/test")
        .username("test")
        .password("test")
        .connectionProperty(AdbaConnectionProperty.TRANSACTION_ISOLATION,
            AdbaConnectionProperty.TransactionIsolation.REPEATABLE_READ)
        .build();

  }

  public static void createTable(DataSource ds, String tab, String id_int, String s, String answer_int) {

  }
}
