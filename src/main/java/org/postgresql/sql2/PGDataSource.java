/*
 * Copyright (c) 2018, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.postgresql.sql2;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.ConnectionProperty;
import jdk.incubator.sql2.DataSource;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.postgresql.sql2.execution.DefaultNioLoop;
import org.postgresql.sql2.execution.NioLoop;

public class PGDataSource implements DataSource {
  private final NioLoop loop;
  private Queue<PGConnection> connections = new ConcurrentLinkedQueue<>();
  private boolean closed;
  private Map<ConnectionProperty, Object> properties;
  private DefaultNioLoop defaultLoop = null;

  public PGDataSource(Map<ConnectionProperty, Object> properties) {
    this.properties = properties;
    
    // Deprecated
//    Executor executor = new ThreadPoolExecutor(1, 2, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
//    executor.execute(() -> {
//      while (!closed) {
//        for (PGConnection connection : connections) {
//          connection.visit();
//        }
//      }
//    });
    
    // Obtain the NIO loop
    NioLoop loop = (NioLoop) this.properties.get(PGConnectionProperties.NIO_LOOP);
    if (loop == null) {
      // Provide default loop
      this.defaultLoop = new DefaultNioLoop();
      new Thread(this.defaultLoop).run();
      loop = defaultLoop;
    }
    this.loop = loop;
  }
  
  /**
   * Obtains the {@link NioLoop}.
   * 
   * @return {@link NioLoop}.
   */
  public NioLoop getNioLoop() {
    return this.loop;
  }

  /**
   * Returns a {@link Connection} builder. By default that builder will return
   * {@link Connection}s with the {@code ConnectionProperty}s specified when creating this
   * DataSource. Default and unspecified {@link ConnectionProperty}s can be set with
   * the returned builder.
   *
   * @return a new {@link Connection} builder. Not {@code null}.
   */
  @Override
  public Connection.Builder builder() {
    if (closed) {
      throw new IllegalStateException("this datasource has already been closed");
    }

    return new PGConnectionBuilder(this);
  }

  @Override
  public void close() {
    for (PGConnection connection : connections) {
      connection.close();
    }
    if (this.defaultLoop != null) {
      this.defaultLoop.close();
    }
    closed = true;
  }

  public void registerConnection(PGConnection connection) {
    connections.add(connection);
  }

  public Map<ConnectionProperty, Object> getProperties() {
    return properties;
  }
}
