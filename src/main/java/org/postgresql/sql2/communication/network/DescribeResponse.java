package org.postgresql.sql2.communication.network;

import org.postgresql.sql2.communication.BEFrame;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.packets.RowDescription;

import java.io.IOException;

/**
 * Describe {@link NetworkResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class DescribeResponse extends AbstractPortalResponse {

  public DescribeResponse(Portal portal) {
    super(portal);
  }

  @Override
  public NetworkResponse read(NetworkReadContext context) throws IOException {
    BEFrame frame = context.getBEFrame();
    switch (frame.getTag()) {

    case NO_DATA:
      return null;

    case PARAM_DESCRIPTION:
      return this; // wait on row description

    case ROW_DESCRIPTION:
      RowDescription rowDescription = new RowDescription(frame.getPayload());
      this.portal.getQuery().setRowDescription(rowDescription);
      return null; // nothing further

    default:
      throw new IllegalStateException("Invalid tag '" + frame.getTag() + "' for " + this.getClass().getSimpleName());
    }
  }

}