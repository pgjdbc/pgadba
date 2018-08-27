package org.postgresql.sql2.communication.network;

import org.postgresql.sql2.communication.FeFrame;
import org.postgresql.sql2.communication.NetworkOutputStream;
import org.postgresql.sql2.communication.NetworkRequest;
import org.postgresql.sql2.communication.NetworkWriteContext;

/**
 * Sync {@link NetworkRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class SyncRequest implements NetworkRequest {
  private final Portal portal;

  public SyncRequest(Portal portal) {
    this.portal = portal;
  }

  /*
   * ================= NetworkRequest =========================
   */

  @Override
  public NetworkRequest write(NetworkWriteContext context) throws Exception {

    NetworkOutputStream wire = context.getOutputStream();
    wire.write(FeFrame.FrontendTag.SYNC.getByte());
    wire.initPacket();
    wire.completePacket();

    if (portal.hasMoreToExecute()) {
      return new BindRequest<>(portal);
    }

    // Nothing further
    return null;
  }

}