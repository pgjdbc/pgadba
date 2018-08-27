package org.postgresql.sql2;

import jdk.incubator.sql2.ConnectionProperty;
import jdk.incubator.sql2.DataSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PgDataSourceBuilder implements DataSource.Builder {
  private Map<ConnectionProperty, Object> properties = new HashMap<>();

  @Override
  public DataSource.Builder defaultConnectionProperty(ConnectionProperty property, Object value) {
    properties.put(property, value);

    return this;
  }

  @Override
  public DataSource.Builder connectionProperty(ConnectionProperty property, Object value) {
    properties.put(property, value);

    return this;
  }

  @Override
  public DataSource.Builder registerConnectionProperty(ConnectionProperty property) {
    properties.put(property, null);

    return this;
  }

  @Override
  public DataSource.Builder requestHook(Consumer<Long> request) {
    return this;
  }

  @Override
  public DataSource build() {
    return new PgDataSource(properties);
  }
}
