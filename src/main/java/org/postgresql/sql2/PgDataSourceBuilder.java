package org.postgresql.sql2;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongConsumer;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSource.Builder;
import jdk.incubator.sql2.DataSourceProperty;
import jdk.incubator.sql2.SessionProperty;

public class PgDataSourceBuilder implements DataSource.Builder {
  private Map<SessionProperty, Object> properties = new HashMap<>();

  @Override
  public Builder property(DataSourceProperty p, Object v) {
    return null;
  }

  @Override
  public DataSource.Builder defaultSessionProperty(SessionProperty property, Object value) {
    properties.put(property, value);

    return this;
  }

  @Override
  public DataSource.Builder sessionProperty(SessionProperty property, Object value) {
    properties.put(property, value);

    return this;
  }

  @Override
  public DataSource.Builder registerSessionProperty(SessionProperty property) {
    properties.put(property, null);

    return this;
  }

  @Override
  public DataSource.Builder requestHook(LongConsumer request) {
    return this;
  }

  @Override
  public DataSource build() {
    return new PgDataSource(properties);
  }
}
