package org.postgresql.sql2.communication.network;

import java.io.IOException;

import org.postgresql.sql2.communication.BEFrame;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.packets.RowDescription;

/**
 * Describe {@link NetworkResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class DescribeResponse implements NetworkResponse {

  private Query query;

  public DescribeResponse(Query query) {
    this.query = query;
  }

  @Override
  public NetworkResponse read(NetworkReadContext context) throws IOException {
    BEFrame frame = context.getBEFrame();
    switch (frame.getTag()) {

    case PARAM_DESCRIPTION:
      return this; // wait on row description

    case ROW_DESCRIPTION:
      RowDescription rowDescription = new RowDescription(frame.getPayload());
      this.query.setRowDescription(rowDescription);
      return null; // nothing further

    default:
      throw new IllegalStateException("Invalid tag '" + frame.getTag() + "' for " + this.getClass().getSimpleName());
    }
  }

}