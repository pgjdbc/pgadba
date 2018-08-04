package org.postgresql.sql2.communication;

import java.io.IOException;

/**
 * Connect to the PostgreSql.
 * 
 * @author Daniel Sagenschneider
 */
public interface NetworkConnect {

  /**
   * Triggers the connect.
   * 
   * @param context {@link NetworkConnectContext}.
   * @throws IOException If failure to initialise.
   */
  void connect(NetworkConnectContext context) throws IOException;

  /**
   * Handles the connect by the {@link NetworkAction}.
   * 
   * @param context {@link NetworkConnectContext}.
   * @return Possible {@link NetworkAction} to undertake immediately after
   *         connection established.
   * @throws IOException If failure in handling the connect.
   */
  NetworkAction finishConnect(NetworkConnectContext context) throws IOException;

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
