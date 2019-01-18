package org.postgresql.adba;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongConsumer;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSource.Builder;
import jdk.incubator.sql2.DataSourceProperty;
import jdk.incubator.sql2.SessionProperty;

public class PgDataSourceBuilder implements DataSource.Builder {
  private Map<SessionProperty, Object> properties = new HashMap<>();
  private boolean buildCalled = false;

  @Override
  public Builder property(DataSourceProperty p, Object v) {
    throw new RuntimeException("not implemented yet");
  }

  @Override
  public DataSource.Builder defaultSessionProperty(SessionProperty property, Object value) {
    if (property == null) {
      throw new IllegalArgumentException("property object may not be null");
    }

    if (buildCalled) {
      throw new IllegalStateException("can't modify properties after build() has been called");
    }

    try {
      if (!property.validate(value)) {
        throw new IllegalArgumentException("value of " + property.name() + " is of the wrong type");
      }
    } catch (Throwable e) {
      if (e instanceof IllegalArgumentException) {
        throw e;
      }
      throw new IllegalStateException("Exception thrown while validating value", e);
    }

    if (properties.containsKey(property)) {
      throw new IllegalArgumentException("you are not allowed to register the same property twice");
    }

    if (value instanceof Cloneable) {
      try {
        Method clone = value.getClass().getDeclaredMethod("clone");
        properties.put(property, clone.invoke(value));
      } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        throw new IllegalStateException("problem with the clone call", e);
      }
    } else {
      properties.put(property, value);
    }

    return this;
  }

  @Override
  public Builder sessionProperty(SessionProperty property, Object value) {
    if (property == null) {
      throw new IllegalArgumentException("property object may not be null");
    }

    if (buildCalled) {
      throw new IllegalStateException("can't modify properties after build() has been called");
    }

    try {
      if (!property.validate(value)) {
        throw new IllegalArgumentException("value of " + property.name() + " is of the wrong type");
      }
    } catch (Throwable e) {
      if (e instanceof IllegalArgumentException) {
        throw e;
      }
      throw new IllegalStateException("Exception thrown while validating value", e);
    }

    if (properties.containsKey(property)) {
      throw new IllegalArgumentException("you are not allowed to register the same property twice");
    }

    if (value instanceof Cloneable) {
      try {
        Method clone = value.getClass().getDeclaredMethod("clone");
        properties.put(property, clone.invoke(value));
      } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        throw new IllegalStateException("problem with the clone call", e);
      }
    } else {
      properties.put(property, value);
    }

    return this;
  }

  @Override
  public DataSource.Builder registerSessionProperty(SessionProperty property) {
    if (property == null) {
      throw new IllegalArgumentException("property object may not be null");
    }

    if (buildCalled) {
      throw new IllegalStateException("can't modify properties after build() has been called");
    }

    if (properties.containsKey(property)) {
      throw new IllegalArgumentException("you are not allowed to register the same property twice");
    }

    properties.put(property, null);

    return this;
  }

  @Override
  public DataSource.Builder requestHook(LongConsumer request) {
    return this;
  }

  @Override
  public DataSource build() {
    buildCalled = true;
    return new PgDataSource(properties);
  }
}
