package org.postgresql.sql2.communication.network;

import java.io.IOException;

import org.postgresql.sql2.communication.BEFrameParser;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.packets.RowDescription;

/**
 * Describe {@link NetworkResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class DescribeResponse extends AbstractQueryResponse {

  public DescribeResponse(Query query) {
    super(query);
  }

  @Override
  public NetworkResponse read(NetworkReadContext context) throws IOException {
    switch (context.getFrameTag()) {

    case BEFrameParser.NO_DATA:
      return null;

    case BEFrameParser.PARAM_DESCRIPTION:
      return this; // wait on row description

    case BEFrameParser.ROW_DESCRIPTION:
      RowDescription rowDescription = new RowDescription(context.getPayload());
      QueryReuse reuse = this.query.getReuse();
      reuse.setRowDescription(rowDescription);
      return null; // nothing further

    default:
      throw new IllegalStateException(
          "Invalid tag '" + context.getFrameTag() + "' for " + this.getClass().getSimpleName());
    }
  }

}