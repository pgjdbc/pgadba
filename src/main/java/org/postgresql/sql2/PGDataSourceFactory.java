package org.postgresql.sql2;

import java2.sql2.DataSource;
import java2.sql2.DataSourceFactory;

public class PGDataSourceFactory implements DataSourceFactory {
  private static DataSourceFactory registeredFactory;

  static {
    registeredFactory = new PGDataSourceFactory();
    DataSourceFactory.registerDataSourceFactory(registeredFactory);
  }

  @Override
  public DataSource.Builder builder() {
    return new PGDataSourceBuilder();
  }

  @Override
  public String getName() {
    return "Postgres Database";
  }
}
