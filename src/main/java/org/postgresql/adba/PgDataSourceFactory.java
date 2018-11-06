package org.postgresql.adba;

import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;

public class PgDataSourceFactory implements DataSourceFactory {
  @Override
  public DataSource.Builder builder() {
    return new PgDataSourceBuilder();
  }
}
