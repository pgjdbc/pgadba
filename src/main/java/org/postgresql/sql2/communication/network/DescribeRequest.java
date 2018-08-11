package org.postgresql.sql2.communication.network;

import org.postgresql.sql2.communication.FEFrame;
import org.postgresql.sql2.communication.NetworkOutputStream;
import org.postgresql.sql2.communication.NetworkRequest;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.NetworkWriteContext;
import org.postgresql.sql2.submissions.RowSubmission;

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
   * @param query      {@link Query}.
   * @param submission {@link RowSubmission}.
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
    wire.write(FEFrame.FrontendTag.DESCRIBE.getByte());
    wire.initPacket();
    wire.write('S');
    wire.write(this.portal.getQuery().getQueryName());
    wire.completePacket();

    // Next step to bind
    return new BindRequest<>(this.portal);
  }

  @Override
  public NetworkResponse getRequiredResponse() {
    return new DescribeResponse(this.portal);
  }

}