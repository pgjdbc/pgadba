package org.postgresql.sql2.communication;

import jdk.incubator.sql2.ConnectionProperty;

/**
 * Context for writing to the network.
 * 
 * @author Daniel Sagenschneider
 */
public interface NetworkReadContext extends NetworkContext {

  /**
   * Obtains the {@link BEFrame} just read.
   * 
   * @return {@link BEFrame} just read.
   */
  BEFrame getBEFrame();

  /**
   * Obtains the {@link PreparedStatementCache}.
   * 
   * @return {@link PreparedStatementCache}.
   */
  PreparedStatementCache getPreparedStatementCache();

  /**
   * Allows overriding {@link ConnectionProperty}.
   * 
   * @param property {@link ConnectionProperty}.
   * @param value    Value.
   */
  void setProperty(ConnectionProperty property, Object value);

  /**
   * Triggers for a {@link NetworkRequest} to be undertaken.
   * 
   * @param request {@link NetworkRequest} to be undertaken.
   */
  void write(NetworkRequest request);

  /**
   * Triggers for a write.
   */
  void writeRequired();

}