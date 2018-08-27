package org.postgresql.sql2.communication.network;

import java.io.IOException;

import org.postgresql.sql2.communication.BEFrameParser;
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
    switch (context.getFrameTag()) {

    case BEFrameParser.PARSE_COMPLETE:
      this.portal.getQuery().flagParsed();
      return null; // nothing further

    default:
      throw new IllegalStateException(
          "Invalid tag '" + context.getFrameTag() + "' for " + this.getClass().getSimpleName());
    }
  }

}
