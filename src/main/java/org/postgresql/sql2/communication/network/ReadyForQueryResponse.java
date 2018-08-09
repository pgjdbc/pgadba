package org.postgresql.sql2.communication.network;

import java.io.IOException;

import org.postgresql.sql2.communication.BEFrame;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.NetworkResponse;

/**
 * Ready for Query {@link NetworkResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class ReadyForQueryResponse implements NetworkResponse {

  @Override
  public NetworkResponse read(NetworkReadContext context) throws IOException {
    BEFrame frame = context.getBEFrame();
    switch (frame.getTag()) {

    case READY_FOR_QUERY:
      // Consume ready for query
      return null; // nothing further

    default:
      throw new IllegalStateException("Invalid tag '" + frame.getTag() + "' for " + this.getClass().getSimpleName());
    }
  }

}