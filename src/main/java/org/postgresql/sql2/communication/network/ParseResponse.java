package org.postgresql.sql2.communication.network;

import java.io.IOException;

import org.postgresql.sql2.communication.BeFrame;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.NetworkResponse;

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
