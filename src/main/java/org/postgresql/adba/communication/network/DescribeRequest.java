package org.postgresql.adba.communication.network;

import org.postgresql.adba.communication.FrontendTag;
import org.postgresql.adba.communication.NetworkOutputStream;
import org.postgresql.adba.communication.NetworkRequest;
import org.postgresql.adba.communication.NetworkResponse;
import org.postgresql.adba.communication.NetworkWriteContext;

/**
 * Describe {@link NetworkRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class DescribeRequest<T> implements NetworkRequest {

  private final Portal portal;

  /**
   * Instantiate.
   *
   * @param portal the portal this request connects to
   */
  public DescribeRequest(Portal portal) {
    this.portal = portal;
  }

  /*
   * ================= NetworkRequest =========================
   */

  @Override
  public NetworkRequest write(NetworkWriteContext context) throws Exception {

    // Send describe packet
    NetworkOutputStream wire = context.getOutputStream();
    wire.write(FrontendTag.DESCRIBE.getByte());
    wire.initPacket();
    wire.write('S');
    wire.write(portal.getQuery().getQueryName());
    wire.completePacket();

    // Next step to bind
    return new BindRequest<>(portal);
  }

  @Override
  public NetworkResponse getRequiredResponse() {
    return new DescribeResponse(portal);
  }

}