package org.postgresql.sql2.communication.network;

import org.postgresql.sql2.communication.BEFrame;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.NetworkResponse;

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
    BEFrame frame = context.getBEFrame();
    switch (frame.getTag()) {

    case READY_FOR_QUERY:
      return null; // Nothing further

    default:
      throw new IllegalStateException("Invalid tag '" + frame.getTag() + "' for " + this.getClass().getSimpleName());
    }
  }
}