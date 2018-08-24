package org.postgresql.sql2.communication.network;

import org.postgresql.sql2.communication.FEFrame;
import org.postgresql.sql2.communication.NetworkOutputStream;
import org.postgresql.sql2.communication.NetworkRequest;
import org.postgresql.sql2.communication.NetworkWriteContext;

/**
 * Close {@link NetworkRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class CloseRequest implements NetworkRequest {

  @Override
  public NetworkRequest write(NetworkWriteContext context) throws Exception {

    // Send the close
    NetworkOutputStream wire = context.getOutputStream();
    wire.write(FEFrame.FrontendTag.TERMINATE.getByte());
    wire.initPacket();
    wire.completePacket();

    // Nothing further
    return null;
  }

}