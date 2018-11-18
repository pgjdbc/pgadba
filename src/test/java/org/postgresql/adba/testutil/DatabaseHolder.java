package org.postgresql.adba.testutil;

import org.testcontainers.containers.PostgreSQLContainer;

public class DatabaseHolder {
  private static PostgreSQLContainer postgres = new PostgreSQLContainer();

  static {
    postgres.start();
  }

  public static PostgreSQLContainer getCached() {
    return postgres;
  }

  /**
   * returns a new database, instead of the cached one.
   * @return a docker instance running a postgresql database
   */
  public static PostgreSQLContainer getNew() {
    PostgreSQLContainer container = new PostgreSQLContainer();
    container.start();
    return container;
  }

  /**
   * returns a new database that requires TLS, instead of the cached one.
   * @return a docker instance running a postgresql database
   */
  public static PostgreSQLContainer getNewWithTls() {
    PostgreSQLContainer container = new PostgreSQLContainer("capitol/postgresql-tls:debian-stretch-postgresql10");
    container.start();

    return container;
  }

  /**
   * returns a new database that that runs postgresql 11.
   * @return a docker instance running a postgresql 11 database
   */
  public static PostgreSQLContainer getNew11() {
    PostgreSQLContainer container = new PostgreSQLContainer("capitol/debian-buster-postgresql11-tls:latest");
    container.start();

    return container;
  }
}
