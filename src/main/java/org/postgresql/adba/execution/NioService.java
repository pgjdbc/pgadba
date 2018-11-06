package org.postgresql.adba.execution;

import java.nio.channels.Channel;

/**
 * NIO service for the {@link NioLoop}.
 * 
 * @author Daniel Sagenschneider
 */
public interface NioService {

  /**
   * Handles the connect.
   * 
   * @throws Exception If fails to handle the accept.
   */
  void handleConnect() throws Exception;

  /**
   * Indicates data is available to read.
   * 
   * @throws Exception If failure in reading and processing the data.
   */
  void handleRead() throws Exception;

  /**
   * Indicates underlying {@link Channel} has cleared space for further writing.
   * 
   * @throws Exception If failure in writing data.
   */
  void handleWrite() throws Exception;

  /**
   * Handles a {@link Throwable} in servicing.
   * 
   * @param ex {@link Throwable} to be handled.
   */
  void handleException(Throwable ex);
}
