/*
 * Copyright (c) 2018, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.postgresql.sql2;

import java2.sql2.Connection;
import java2.sql2.ConnectionProperty;
import java2.sql2.DataSource;

public class PGDataSource implements DataSource {
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
    return null;
  }

  @Override
  public void close() {

  }
}
