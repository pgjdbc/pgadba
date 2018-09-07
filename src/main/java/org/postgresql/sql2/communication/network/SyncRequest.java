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
  
  private final Query query;

  public SyncRequest(Query query) {
    this.query = query;
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

    if (this.query.hasMoreToExecute()) {
      return new BindRequest<>(this.query);
    }

    // Nothing further
    return null;
  }

}