package org.postgresql.adba;

import java.util.function.LongConsumer;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSource.Builder;
import jdk.incubator.sql2.DataSourceProperty;
import jdk.incubator.sql2.SessionProperty;
import org.postgresql.adba.util.PropertyHolder;

public class PgDataSourceBuilder implements DataSource.Builder {
  private PropertyHolder properties = new PropertyHolder();
  private boolean buildCalled = false;

  @Override
  public Builder property(DataSourceProperty property, Object value) {
    properties.property(property, value);

    return this;
  }

  @Override
  public Builder defaultSessionProperty(SessionProperty property, Object value) {
    if (buildCalled) {
      throw new IllegalStateException("can't modify properties after build() has been called");
    }

    properties.defaultSessionProperty(property, value);

    return this;
  }

  @Override
  public Builder sessionProperty(SessionProperty property, Object value) {
    if (buildCalled) {
      throw new IllegalStateException("can't modify properties after build() has been called");
    }

    properties.sessionProperty(property, value);

    return this;
  }

  @Override
  public Builder registerSessionProperty(SessionProperty property) {
    if (buildCalled) {
      throw new IllegalStateException("can't modify properties after build() has been called");
    }

    properties.registerSessionProperty(property);

    return this;
  }

  @Override
  public Builder requestHook(LongConsumer request) {
    return this;
  }

  @Override
  public DataSource build() {
    buildCalled = true;
    return new PgDataSource(properties);
  }
}
