package org.postgresql.sql2.communication;

import java.io.OutputStream;

/**
 * Context for writing to the network.
 * 
 * @author Daniel Sagenschneider
 */
public interface NetworkWriteContext extends NetworkContext {

  /**
   * Obtains the {@link OutputStream} to write content to the network.
   * 
   * @return {@link OutputStream} to write content to the network.
   */
  OutputStream getOutputStream();

}