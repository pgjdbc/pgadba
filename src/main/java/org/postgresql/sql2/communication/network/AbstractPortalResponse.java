package org.postgresql.sql2.communication.network;

import org.postgresql.sql2.communication.NetworkResponse;

/**
 * Abstract {@link Portal} {@link NetworkResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractPortalResponse implements NetworkResponse {

  /**
   * {@link Portal}.
   */
  protected final Portal portal;

  /**
   * Instantiate.
   * 
   * @param portal {@link Portal}.
   */
  public AbstractPortalResponse(Portal portal) {
    this.portal = portal;
  }

  @Override
  public NetworkResponse handleException(Throwable ex) {
    portal.handleException(ex);
    return new ReadyForQueryResponse();
  }
}