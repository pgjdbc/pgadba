package org.postgresql.sql2.communication;

import java.io.IOException;

/**
 * Action over the network to the PostgreSql database.
 * 
 * @author Daniel Sagenschneider
 */
public interface NetworkAction {

  /**
   * Writes this {@link NetworkAction} to the network.
   * 
   * @param context {@link NetworkWriteContext}.
   * @throws IOException If failure in writing to the network.
   */
  void write(NetworkWriteContext context) throws IOException;

  /**
   * Indicates the {@link NetworkAction} is blocking further {@link NetworkAction}
   * instances from being sent over the network.
   * 
   * @return <code>true</code> to block further {@link NetworkAction} instances
   *         from being sent.
   */
  default boolean isBlocking() {
    return false;
  }

  /**
   * Indicates if the {@link NetworkAction} requires a response.
   * 
   * @return <code>true</code> indicating a response is expected for this
   *         {@link NetworkAction}. <code>false</code> to indicate may be
   *         discarded once written.
   */
  default boolean isRequireResponse() {
    return false;
  }

  /**
   * Handles the read by the {@link NetworkAction}.
   * 
   * @param context {@link NetworkReadContext}.
   * @return Optional further {@link NetworkAction} to be undertaken.
   * @throws IOException If failure in handling the read.
   */
  default NetworkAction read(NetworkReadContext context) throws IOException {
    throw new IllegalStateException(this.getClass().getName() + " not overriding read");
  }

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