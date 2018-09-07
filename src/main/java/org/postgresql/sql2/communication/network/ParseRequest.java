package org.postgresql.sql2.communication.network;

import org.postgresql.sql2.PgSubmission;
import org.postgresql.sql2.communication.FeFrame;
import org.postgresql.sql2.communication.NetworkOutputStream;
import org.postgresql.sql2.communication.NetworkRequest;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.NetworkWriteContext;
import org.postgresql.sql2.communication.QueryFactory;
import org.postgresql.sql2.operations.helpers.ParameterHolder;
import org.postgresql.sql2.operations.helpers.QueryParameter;
import org.postgresql.sql2.util.BinaryHelper;

/**
 * Row operation {@link NetworkRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class ParseRequest<T> implements NetworkRequest {

  private final PgSubmission<T> submission;

  private Query query = null;

  public ParseRequest(PgSubmission<T> submission) {
    this.submission = submission;
  }

  /*
   * ================== NetworkRequest ========================
   */

  @Override
  public NetworkRequest write(NetworkWriteContext context) throws Exception {

    // Obtain the query
    this.query = context.getQueryFactory().createQuery(this.submission);

    // Obtain the details of query
    String sql = this.query.getSubmission().getSql();
    ParameterHolder parameters = this.query.getSubmission().getHolder();

    // Determine if simple query (not yet executed enough)
    QueryReuse reuse = this.query.getReuse();
    if (reuse.isSimpleQuery()) {
      // TODO send simple query
      throw new UnsupportedOperationException("TODO implement simple query");
    }

    // Determine if prepare query
    if ((!reuse.isParsed()) && (!reuse.isWaitingParse())) {

      // Send the prepare packet
      NetworkOutputStream wire = context.getOutputStream();
      wire.write(FeFrame.FrontendTag.PARSE.getByte());
      wire.initPacket();
      wire.write(this.query.getReuse().getPortalNameOrUnnamed());
      wire.write(sql);
      wire.write(BinaryHelper.writeShort(parameters.size()));
      for (QueryParameter qp : parameters.parameters()) {
        wire.write(BinaryHelper.writeInt(qp.getOid()));
      }
      wire.completePacket();
    }

    // Determine if describe or bind
    return new DescribeRequest<>(this.query);
  }

  @Override
  public NetworkResponse getRequiredResponse() {

    // Determine if waiting on parse
    QueryReuse reuse = this.query.getReuse();
    if (!reuse.isWaitingParse()) {
      reuse.flagWaitingParse();
      return new ParseResponse(this.query);
    }

    // Already waiting on parse
    return null;
  }

}