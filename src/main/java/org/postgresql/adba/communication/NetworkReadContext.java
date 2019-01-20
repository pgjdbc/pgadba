package org.postgresql.adba.communication;

import org.postgresql.adba.PgSessionDbProperty;

/**
 * Context for writing to the network.
 * 
 * @author Daniel Sagenschneider
 */
public interface NetworkReadContext extends NetworkContext {

  /**
   * Obtains the {@link BeFrame} just read.
   * 
   * @return {@link BeFrame} just read.
   */
  BeFrame getBeFrame();

  /**
   * Obtains the {@link PreparedStatementCache}.
   * 
   * @return {@link PreparedStatementCache}.
   */
  PreparedStatementCache getPreparedStatementCache();

  /**
   * set a {@link PgSessionDbProperty} that came from the server.
   * 
   * @param property {@link PgSessionDbProperty}.
   * @param value    Value.
   */
  void setProperty(PgSessionDbProperty property, Object value);

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