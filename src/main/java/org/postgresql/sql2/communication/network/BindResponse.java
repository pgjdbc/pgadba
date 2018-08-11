package org.postgresql.sql2.communication.network;

import java.io.IOException;

import org.postgresql.sql2.communication.BEFrame;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.NetworkResponse;

/**
 * Bind {@link NetworkResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class BindResponse extends AbstractPortalResponse {

  public BindResponse(Portal portal) {
    super(portal);
  }

  @Override
  public NetworkResponse read(NetworkReadContext context) throws IOException {
    BEFrame frame = context.getBEFrame();
    switch (frame.getTag()) {

    case BIND_COMPLETE:
      return null; // Nothing further

    default:
      throw new IllegalStateException("Invalid tag '" + frame.getTag() + "' for " + this.getClass().getSimpleName());
    }
  }

}
