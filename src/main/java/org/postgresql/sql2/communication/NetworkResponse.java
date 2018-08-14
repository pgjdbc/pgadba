package org.postgresql.sql2.communication;

import java.io.IOException;

/**
 * Handler for response from network.
 * 
 * @author Daniel Sagenschneider
 */
public interface NetworkResponse extends NetworkErrorHandler {

  /**
   * Handles the read by the {@link NetworkRequest}.
   * 
   * @param context {@link NetworkReadContext}.
   * @return Optional further {@link NetworkResponse} to be received.
   * @throws IOException If failure in handling the read.
   */
  NetworkResponse read(NetworkReadContext context) throws IOException;

}