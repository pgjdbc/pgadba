package org.postgresql.sql2.communication.network;

import java.io.IOException;

import org.postgresql.sql2.communication.BEFrame;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.NetworkResponse;

/**
 * Parse {@link NetworkResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class ParseResponse implements NetworkResponse {

  private final Query query;

  public ParseResponse(Query query) {
    this.query = query;
  }

  @Override
  public NetworkResponse read(NetworkReadContext context) throws IOException {
    BEFrame frame = context.getBEFrame();
    switch (frame.getTag()) {

    case PARSE_COMPLETE:
      this.query.flagParsed();
      return null; // nothing further

    default:
      throw new IllegalStateException("Invalid tag '" + frame.getTag() + "' for " + this.getClass().getSimpleName());
    }
  }

}
