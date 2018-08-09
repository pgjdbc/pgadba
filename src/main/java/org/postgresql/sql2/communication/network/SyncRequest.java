package org.postgresql.sql2.communication.network;

import java.io.IOException;

import org.postgresql.sql2.communication.FEFrame;
import org.postgresql.sql2.communication.NetworkOutputStream;
import org.postgresql.sql2.communication.NetworkRequest;
import org.postgresql.sql2.communication.NetworkWriteContext;

/**
 * Sync {@link NetworkRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class SyncRequest implements NetworkRequest {

  /*
   * ================= NetworkRequest =========================
   */

  @Override
  public NetworkRequest write(NetworkWriteContext context) throws IOException {

    NetworkOutputStream wire = context.getOutputStream();
    wire.write(FEFrame.FrontendTag.SYNC.getByte());
    wire.initPacket();
    wire.completePacket();

    // Nothing further
    return null;
  }

}