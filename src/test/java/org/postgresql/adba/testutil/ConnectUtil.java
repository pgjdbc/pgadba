package org.postgresql.adba.testutil;

import jdk.incubator.sql2.AdbaSessionProperty;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;
import org.postgresql.adba.PgSessionProperty;
import org.testcontainers.containers.PostgreSQLContainer;

public class ConnectUtil {

  /**
   * Opens a DataSource to the supplied database.
   * @param postgres the docker database
   * @return a datasource
   */
  public static DataSource openDb(PostgreSQLContainer postgres) {
    return DataSourceFactory.newFactory("org.postgresql.adba.PgDataSourceFactory")
        .builder()
        .url("jdbc:postgresql://" + postgres.getContainerIpAddress() + ":" + postgres.getMappedPort(5432)
            + "/" + postgres.getDatabaseName())
        .username(postgres.getUsername())
        .password(postgres.getPassword())
        .sessionProperty(AdbaSessionProperty.TRANSACTION_ISOLATION,
            AdbaSessionProperty.TransactionIsolation.REPEATABLE_READ)
        .build();
  }

  /**
   * Opens a DataSource to the supplied database that sends all traffic over tls.
   * @param postgres the docker database
   * @return a datasource
   */
  public static DataSource openDbWithTls(PostgreSQLContainer postgres) {
    return DataSourceFactory.newFactory("org.postgresql.adba.PgDataSourceFactory")
        .builder()
        .url("jdbc:postgresql://" + postgres.getContainerIpAddress() + ":" + postgres.getMappedPort(5432)
            + "/" + postgres.getDatabaseName())
        .username(postgres.getUsername())
        .password(postgres.getPassword())
        .sessionProperty(AdbaSessionProperty.TRANSACTION_ISOLATION,
            AdbaSessionProperty.TransactionIsolation.REPEATABLE_READ)
        .sessionProperty(PgSessionProperty.SSL, true)
        .build();
  }

  public static void createTable(DataSource ds, String tab, String idInt, String s, String answerInt) {

  }
}
