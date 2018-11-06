package org.postgresql.adba.communication.network;

import java.io.IOException;

import org.postgresql.adba.communication.BeFrame;
import org.postgresql.adba.communication.NetworkReadContext;
import org.postgresql.adba.communication.NetworkResponse;

/**
 * Parse {@link NetworkResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class ParseResponse extends AbstractPortalResponse {

  public ParseResponse(Portal portal) {
    super(portal);
  }

  @Override
  public NetworkResponse read(NetworkReadContext context) throws IOException {
    BeFrame frame = context.getBeFrame();
    switch (frame.getTag()) {

      case PARSE_COMPLETE:
        portal.getQuery().flagParsed();
        return null; // nothing further

      default:
        throw new IllegalStateException("Invalid tag '" + frame.getTag() + "' for " + this.getClass().getSimpleName());
    }
  }

}
