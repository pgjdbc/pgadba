package org.postgresql.adba.communication.network;

import org.postgresql.adba.communication.FeFrame;
import org.postgresql.adba.communication.NetworkOutputStream;
import org.postgresql.adba.communication.NetworkRequest;
import org.postgresql.adba.communication.NetworkResponse;
import org.postgresql.adba.communication.NetworkWriteContext;
import org.postgresql.adba.util.BinaryHelper;

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
    wire.write(portal.getPortalName());
    wire.write(BinaryHelper.writeInt(0)); // number of rows to return, 0 == all
    wire.completePacket();

    // TODO Auto-generated method stub
    return new SyncRequest(portal);
  }

  @Override
  public NetworkResponse getRequiredResponse() {
    return new ExecuteResponse(portal);
  }

}