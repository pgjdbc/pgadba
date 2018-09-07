package org.postgresql.sql2.communication.network;

import org.postgresql.sql2.communication.FeFrame;
import org.postgresql.sql2.communication.NetworkOutputStream;
import org.postgresql.sql2.communication.NetworkRequest;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.NetworkWriteContext;
import org.postgresql.sql2.operations.helpers.ParameterHolder;
import org.postgresql.sql2.operations.helpers.QueryParameter;
import org.postgresql.sql2.util.BinaryHelper;

/**
 * Bind {@link NetworkRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class BindRequest<T> implements NetworkRequest {

  private final Query query;

  public BindRequest(Query query) {
    this.query = query;
  }

  /*
   * ================= NetworkRequest =========================
   */

  @Override
  public NetworkRequest write(NetworkWriteContext context) throws Exception {

    // Obtain the query details
    ParameterHolder holder = this.query.getSubmission().getHolder();

    // Write the packet
    NetworkOutputStream wire = context.getOutputStream();
    wire.write(FeFrame.FrontendTag.BIND.getByte());
    wire.initPacket();
    wire.write(this.query.getQueryName());
    wire.write(this.query.getReuse().getPortalNameOrUnnamed());
    wire.write(BinaryHelper.writeShort(holder.size()));
    for (QueryParameter qp : holder.parameters()) {
      wire.write(BinaryHelper.writeShort(qp.getParameterFormatCode()));
    }
    wire.write(BinaryHelper.writeShort(holder.size()));
    int paramIndex = 0;
    for (QueryParameter qp : holder.parameters()) {
      byte[] paramData = qp.getParameter(paramIndex++);
      if (paramData.length == 0) { // handling the null special case
        wire.write(BinaryHelper.writeInt(-1));
      } else {
        wire.write(BinaryHelper.writeInt(paramData.length));
        wire.write(paramData);
      }
    }
    wire.writeTerminator();
    wire.writeTerminator();
    wire.completePacket();

    // Next step to execute
    return new ExecuteRequest<>(this.query);
  }

  @Override
  public NetworkResponse getRequiredResponse() {
    return new BindResponse(this.query);
  }

}