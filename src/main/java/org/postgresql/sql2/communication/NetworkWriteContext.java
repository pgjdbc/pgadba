package org.postgresql.sql2.communication;

/**
 * Context for writing to the network.
 * 
 * @author Daniel Sagenschneider
 */
public interface NetworkWriteContext extends NetworkContext {

  /**
   * Obtains the {@link NetworkOutputStream} to write content to the network.
   * 
   * @return {@link NetworkOutputStream} to write content to the network.
   */
  NetworkOutputStream getOutputStream();

}