package org.postgresql.adba.communication.network;

import org.postgresql.adba.communication.BeFrame;
import org.postgresql.adba.communication.NetworkReadContext;
import org.postgresql.adba.communication.NetworkResponse;

import java.io.IOException;

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
    BeFrame frame = context.getBeFrame();
    switch (frame.getTag()) {

      case READY_FOR_QUERY:
        return null; // Nothing further

      default:
        throw new IllegalStateException("Invalid tag '" + frame.getTag() + "' for " + this.getClass().getSimpleName());
    }
  }
}