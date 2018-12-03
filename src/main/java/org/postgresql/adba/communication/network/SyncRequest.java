package org.postgresql.adba.communication.network;

import org.postgresql.adba.communication.FrontendTag;
import org.postgresql.adba.communication.NetworkOutputStream;
import org.postgresql.adba.communication.NetworkRequest;
import org.postgresql.adba.communication.NetworkWriteContext;

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
    wire.write(FrontendTag.SYNC.getByte());
    wire.initPacket();
    wire.completePacket();

    if (portal.hasMoreToExecute()) {
      return new BindRequest<>(portal);
    }

    // Nothing further
    return null;
  }

}