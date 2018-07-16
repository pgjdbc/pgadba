package org.postgresql.sql2.testUtil;

import org.testcontainers.containers.PostgreSQLContainer;

public class DatabaseHolder {
  private static PostgreSQLContainer postgres = new PostgreSQLContainer();

  static {
    postgres.start();
  }

  public static PostgreSQLContainer getCached() {
    return postgres;
  }

  public static PostgreSQLContainer getNew() {
    PostgreSQLContainer container = new PostgreSQLContainer();
    container.start();
    return container;
  }

  public static PostgreSQLContainer getNewWithTLS() {
    PostgreSQLContainer container = new PostgreSQLContainer("capitol/postgresql-tls:debian-stretch-postgresql10");
    container.start();

    return container;
  }
}
