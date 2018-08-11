package org.postgresql.sql2.communication;

/**
 * Interaction with the network.
 * 
 * @author Daniel Sagenschneider
 */
public interface NetworkErrorHandler {

  /**
   * Handles the failure.
   * 
   * @param ex Failure.
   * @return Optional {@link NetworkResponse}. May be <code>null</code>.
   */
  NetworkResponse handleException(Throwable ex);

}