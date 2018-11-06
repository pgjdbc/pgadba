package org.postgresql.adba.communication.network;

import java.io.IOException;

import org.postgresql.adba.communication.BeFrame;
import org.postgresql.adba.communication.NetworkReadContext;
import org.postgresql.adba.communication.NetworkResponse;

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
    BeFrame frame = context.getBeFrame();
    switch (frame.getTag()) {

      case BIND_COMPLETE:
        return null; // Nothing further

      default:
        throw new IllegalStateException("Invalid tag '" + frame.getTag() + "' for " + this.getClass().getSimpleName());
    }
  }

}
