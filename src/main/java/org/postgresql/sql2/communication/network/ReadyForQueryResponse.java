package org.postgresql.sql2.communication.network;

import java.io.IOException;

import org.postgresql.sql2.communication.BEFrameParser;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.NetworkResponse;

/**
 * Ready for query {@link NetworkResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class ReadyForQueryResponse implements NetworkResponse {

  @Override
  public NetworkResponse handleException(Throwable ex) {
    throw new IllegalStateException("Ready For Query should not fail", ex);
  }

  @Override
  public NetworkResponse read(NetworkReadContext context) throws IOException {
    switch (context.getFrameTag()) {

    case BEFrameParser.READY_FOR_QUERY:
      return null; // Nothing further

    default:
      throw new IllegalStateException(
          "Invalid tag '" + context.getFrameTag() + "' for " + this.getClass().getSimpleName());
    }
  }
}