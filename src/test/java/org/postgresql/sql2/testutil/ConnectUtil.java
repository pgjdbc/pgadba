package org.postgresql.sql2.testutil;

import jdk.incubator.sql2.AdbaConnectionProperty;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;
import org.postgresql.sql2.PgConnectionProperty;
import org.testcontainers.containers.PostgreSQLContainer;

public class ConnectUtil {

  /**
   * Opens a DataSource to the supplied database.
   * @param postgres the docker database
   * @return a datasource
   */
  public static DataSource openDb(PostgreSQLContainer postgres) {
    return DataSourceFactory.newFactory("org.postgresql.sql2.PgDataSourceFactory")
        .builder()
        .url("jdbc:postgresql://" + postgres.getContainerIpAddress() + ":" + postgres.getMappedPort(5432)
            + "/" + postgres.getDatabaseName())
        .username(postgres.getUsername())
        .password(postgres.getPassword())
        .connectionProperty(AdbaConnectionProperty.TRANSACTION_ISOLATION,
            AdbaConnectionProperty.TransactionIsolation.REPEATABLE_READ)
        .build();
  }

  /**
   * Opens a DataSource to the supplied database that sends all traffic over tls.
   * @param postgres the docker database
   * @return a datasource
   */
  public static DataSource openDbWithTls(PostgreSQLContainer postgres) {
    return DataSourceFactory.newFactory("org.postgresql.sql2.PgDataSourceFactory")
        .builder()
        .url("jdbc:postgresql://" + postgres.getContainerIpAddress() + ":" + postgres.getMappedPort(5432)
            + "/" + postgres.getDatabaseName())
        .username(postgres.getUsername())
        .password(postgres.getPassword())
        .connectionProperty(AdbaConnectionProperty.TRANSACTION_ISOLATION,
            AdbaConnectionProperty.TransactionIsolation.REPEATABLE_READ)
        .connectionProperty(PgConnectionProperty.SSL, true)
        .build();
  }

  public static void createTable(DataSource ds, String tab, String idInt, String s, String answerInt) {

  }
}
