package org.postgresql.sql2.communication.network;

import org.postgresql.sql2.communication.FeFrame;
import org.postgresql.sql2.communication.NetworkOutputStream;
import org.postgresql.sql2.communication.NetworkRequest;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.NetworkWriteContext;
import org.postgresql.sql2.util.BinaryHelper;

/**
 * Execute {@link NetworkRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecuteRequest<T> implements NetworkRequest {

  private final Portal portal;

  public ExecuteRequest(Portal portal) {
    this.portal = portal;
  }

  /*
   * ================= NetworkRequest =========================
   */

  @Override
  public NetworkRequest write(NetworkWriteContext context) throws Exception {

    // Obtain the query details
    NetworkOutputStream wire = context.getOutputStream();
    wire.write(FeFrame.FrontendTag.EXECUTE.getByte());
    wire.initPacket();
    wire.write(this.portal.getPortalName());
    wire.write(BinaryHelper.writeInt(0)); // number of rows to return, 0 == all
    wire.completePacket();

    // TODO Auto-generated method stub
    return new SyncRequest(portal);
  }

  @Override
  public NetworkResponse getRequiredResponse() {
    return new ExecuteResponse(this.portal);
  }

}