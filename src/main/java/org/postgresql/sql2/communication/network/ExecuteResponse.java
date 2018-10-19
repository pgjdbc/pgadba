package org.postgresql.sql2.communication.network;

import java.io.IOException;

import org.postgresql.sql2.communication.BeFrame;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.packets.CommandComplete;
import org.postgresql.sql2.communication.packets.DataRow;

/**
 * Execute {@link NetworkResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecuteResponse extends AbstractPortalResponse {

  public ExecuteResponse(Portal portal) {
    super(portal);
  }

  @Override
  public NetworkResponse read(NetworkReadContext context) throws IOException {
    BeFrame frame = context.getBeFrame();
    switch (frame.getTag()) {

      case DATA_ROW:
        DataRow dataRow = new DataRow(frame.getPayload(), portal.getQuery().getRowDescription().getDescriptions(),
            portal.nextRowNumber());
        portal.addDataRow(dataRow);
        return this;

      case COMMAND_COMPLETE:
        CommandComplete complete = new CommandComplete(frame.getPayload());
        portal.commandComplete(complete, context.getSocketChannel());
        return this;

      case READY_FOR_QUERY:
        return null;

      default:
        throw new IllegalStateException("Invalid tag '" + frame.getTag() + "' for " + getClass().getSimpleName());
    }
  }

}