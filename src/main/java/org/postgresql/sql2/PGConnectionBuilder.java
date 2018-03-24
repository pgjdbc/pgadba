package org.postgresql.sql2;

import java2.sql2.Connection;
import java2.sql2.ConnectionProperty;
import org.postgresql.sql2.exceptions.PropertyException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class PGConnectionBuilder implements Connection.Builder {
  private Executor executor = null;
  private Map<ConnectionProperty, Object> properties = new HashMap<>();
  private PGDataSource dataSource;

  public PGConnectionBuilder(PGDataSource dataSource) {
    this.dataSource = dataSource;
    for(PGConnectionProperties prop : PGConnectionProperties.values())
      properties.put(prop, prop.defaultValue());
  }

  @Override
  public Connection.Builder executor(Executor exec) {
    this.executor = exec;
    return this;
  }

  @Override
  public Connection.Builder property(ConnectionProperty p, Object v) {
    if(!(p instanceof PGConnectionProperties))
      throw new PropertyException("Please make sure that the ConnectionProperty is of type PGConnectionProperties");

    if(!(v.getClass().isAssignableFrom(p.range())))
      throw new PropertyException("Please make sure that the ConnectionProperty is of type PGConnectionProperties");

    properties.put(p, v);

    return this;
  }

  @Override
  public Connection build() {
    if(executor == null)
      executor = dataSource.getExecutor();

    PGConnection connection = new PGConnection(executor, properties);
    dataSource.registerConnection(connection);
    return connection;
  }
}
