/*
 * Copyright (c) 2018, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.postgresql.sql2;

import java2.sql2.Connection;
import java2.sql2.ConnectionProperty;
import java2.sql2.DataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PGDataSource implements DataSource {
  private List<PGConnection> connections = new ArrayList<>();
  private Executor executor = null;
  private boolean closed;

  public PGDataSource() {
    executor = new ThreadPoolExecutor(1, 2, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    executor.execute(new Runnable() {
      @Override
      public void run() {
        while (!closed) {
          for(PGConnection connection : connections) {
            connection.visit();
          }
        }
      }
    });
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
    return new PGConnectionBuilder(this);
  }

  @Override
  public void close() {
    for(PGConnection connection : connections) {
      connection.close();
    }
    closed = true;
  }

  public void registerConnection(PGConnection connection) {
    connections.add(connection);
  }

  public Executor getExecutor() {
    return executor;
  }
}
