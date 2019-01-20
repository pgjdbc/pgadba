package org.postgresql.adba;

import jdk.incubator.sql2.DataSourceProperty;
import org.postgresql.adba.buffer.ByteBufferPool;
import org.postgresql.adba.execution.NioLoop;

public enum PgDataSourceProperty implements DataSourceProperty {
  /**
   * Allows specifying the {@link NioLoop}.
   */
  NIO_LOOP(NioLoop.class, null, false),

  /**
   * Allows specifying the {@link ByteBufferPool}.
   */
  BYTE_BUFFER_POOL(ByteBufferPool.class, null, false);

  private Class range;
  private Object defaultValue;
  private boolean sensitive;

  PgDataSourceProperty(Class range, Object defaultValue, boolean sensitive) {
    this.range = range;
    this.defaultValue = defaultValue;
    this.sensitive = sensitive;
  }

  @Override
  public Class<?> range() {
    return range;
  }

  @Override
  public Object defaultValue() {
    return defaultValue;
  }

  @Override
  public boolean isSensitive() {
    return sensitive;
  }
}
