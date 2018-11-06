package org.postgresql.adba.communication;

import java.io.IOException;

/**
 * Connect to the PostgreSql.
 * 
 * @author Daniel Sagenschneider
 */
public interface NetworkConnect extends NetworkErrorHandler {

  /**
   * Triggers the connect.
   * 
   * @param context {@link NetworkConnectContext}.
   * @throws IOException If failure to initialise.
   */
  void connect(NetworkConnectContext context) throws IOException;

  /**
   * Handles the connect by the {@link NetworkRequest}.
   * 
   * @param context {@link NetworkConnectContext}.
   * @return Possible {@link NetworkRequest} to undertake immediately after
   *         connection established.
   * @throws IOException If failure in handling the connect.
   */
  NetworkRequest finishConnect(NetworkConnectContext context) throws IOException;

}
