package org.postgresql.sql2.communication.network;

import java.io.IOException;

import org.postgresql.sql2.communication.BEFrameParser;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.packets.CommandComplete;
import org.postgresql.sql2.communication.packets.DataRow;

/**
 * Execute {@link NetworkResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecuteResponse extends AbstractQueryResponse {

  public ExecuteResponse(Query query) {
    super(query);
  }

  @Override
  public NetworkResponse read(NetworkReadContext context) throws IOException {
    switch (context.getFrameTag()) {

    case BEFrameParser.DATA_ROW:
      DataRow dataRow = new DataRow(context, this.query.getReuse().getRowDescription().getDescriptions(),
          this.query.nextRowNumber());
      this.query.addDataRow(dataRow);
      return this;

    case BEFrameParser.COMMAND_COMPLETE:
      CommandComplete complete = new CommandComplete(context);
      this.query.commandComplete(complete, context.getSocketChannel());
      return this;

    case BEFrameParser.READY_FOR_QUERY:
      return null;

    default:
      throw new IllegalStateException(
          "Invalid tag '" + context.getFrameTag() + "' for " + this.getClass().getSimpleName());
    }
  }

}