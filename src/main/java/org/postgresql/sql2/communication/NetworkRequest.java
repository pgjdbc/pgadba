package org.postgresql.sql2.communication;

/**
 * Action over the network to the PostgreSql database.
 * 
 * @author Daniel Sagenschneider
 */
public interface NetworkRequest extends NetworkErrorHandler {

  /**
   * Writes this {@link NetworkRequest} to the network.
   * 
   * @param context {@link NetworkWriteContext}.
   * @return Optional further {@link NetworkRequest} to be undertaken. May be
   *         <code>null</code> if no further {@link NetworkRequest}.
   * @throws Exception If failure in writing to the network.
   */
  NetworkRequest write(NetworkWriteContext context) throws Exception;

  /**
   * Indicates the {@link NetworkRequest} is blocking further
   * {@link NetworkRequest} instances from being sent over the network.
   * 
   * @return <code>true</code> to block further {@link NetworkRequest} instances
   *         from being sent.
   */
  default boolean isBlocking() {
    return false;
  }

  /**
   * Obtains the {@link NetworkResponse} for a required response.
   * 
   * @return {@link NetworkResponse} for required response. <code>null</code> if
   *         no response.
   */
  default NetworkResponse getRequiredResponse() {
    return null;
  }

}