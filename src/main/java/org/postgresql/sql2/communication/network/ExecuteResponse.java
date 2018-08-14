package org.postgresql.sql2.communication.network;

import java.io.IOException;

import org.postgresql.sql2.communication.BEFrame;
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
    BEFrame frame = context.getBEFrame();
    switch (frame.getTag()) {

    case DATA_ROW:
      DataRow dataRow = new DataRow(frame.getPayload(), this.portal.getQuery().getRowDescription().getDescriptions(),
          this.portal.nextRowNumber());
      this.portal.addDataRow(dataRow);
      return this;

    case COMMAND_COMPLETE:
      CommandComplete complete = new CommandComplete(frame.getPayload());
      this.portal.commandComplete(complete, context.getSocketChannel());
      return this;

    case READY_FOR_QUERY:
      return null;

    default:
      throw new IllegalStateException("Invalid tag '" + frame.getTag() + "' for " + this.getClass().getSimpleName());
    }
  }

}