package org.postgresql.sql2.communication.network;

import org.postgresql.sql2.communication.FeFrame;
import org.postgresql.sql2.communication.NetworkOutputStream;
import org.postgresql.sql2.communication.NetworkRequest;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.NetworkWriteContext;

/**
 * Describe {@link NetworkRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class DescribeRequest<T> implements NetworkRequest {

  private final Query query;

  /**
   * Instantiate.
   *
   * @param query {@link Query}.
   */
  public DescribeRequest(Query query) {
    this.query = query;
  }

  /*
   * ================= NetworkRequest =========================
   */

  @Override
  public NetworkRequest write(NetworkWriteContext context) throws Exception {

    // Obtain the reuse
    QueryReuse reuse = this.query.getReuse();

    // Determine if describe query
    if ((reuse.getRowDescription() == null) && (!reuse.isWaitingDescribe())) {

      // Send describe packet
      NetworkOutputStream wire = context.getOutputStream();
      wire.write(FeFrame.FrontendTag.DESCRIBE.getByte());
      wire.initPacket();
      wire.write('S');
      wire.write(this.query.getReuse().getPortalNameOrUnnamed());
      wire.completePacket();
    }

    // Next step to bind
    return new BindRequest<>(this.query);
  }

  @Override
  public NetworkResponse getRequiredResponse() {

    // Determine if waiting on describe
    QueryReuse reuse = this.query.getReuse();
    if (!reuse.isWaitingDescribe()) {
      reuse.flagWaitingDescribe();
      return new DescribeResponse(this.query);
    }

    // Already waiting on describe
    return null;
  }

}