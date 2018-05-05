package org.postgresql.sql2.testUtil;

import jdk.incubator.sql2.AdbaConnectionProperty;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;
import org.testcontainers.containers.PostgreSQLContainer;

public class ConnectUtil {
  public static DataSource openDB(PostgreSQLContainer postgres) {
    return DataSourceFactory.forName("org.postgresql.sql2.PGDataSourceFactory")
        .builder()
        .url("jdbc:postgresql://" + postgres.getContainerIpAddress() + ":" + postgres.getMappedPort(5432) +
            "/" + postgres.getDatabaseName())
        .username(postgres.getUsername())
        .password(postgres.getPassword())
        .connectionProperty(AdbaConnectionProperty.TRANSACTION_ISOLATION,
            AdbaConnectionProperty.TransactionIsolation.REPEATABLE_READ)
        .build();
  }

  public static void createTable(DataSource ds, String tab, String id_int, String s, String answer_int) {

  }
}
