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
   */
  default void handleException(Throwable ex) {
    // TODO provide exception back up the layers
    ex.printStackTrace();
  }

}