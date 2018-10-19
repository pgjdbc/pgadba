package org.postgresql.sql2.communication.network;

import org.postgresql.sql2.communication.FeFrame;
import org.postgresql.sql2.communication.NetworkOutputStream;
import org.postgresql.sql2.communication.NetworkRequest;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.NetworkWriteContext;
import org.postgresql.sql2.communication.PreparedStatementCache;
import org.postgresql.sql2.operations.helpers.ParameterHolder;
import org.postgresql.sql2.operations.helpers.QueryParameter;
import org.postgresql.sql2.util.BinaryHelper;

/**
 * Row operation {@link NetworkRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class ParseRequest<T> implements NetworkRequest {

  private final Portal portal;

  public ParseRequest(Portal portal) {
    this.portal = portal;
  }

  /*
   * ================== NetworkRequest ========================
   */

  @Override
  public NetworkRequest write(NetworkWriteContext context) throws Exception {

    // Determine if already query
    Query query = portal.getQuery();
    if (query == null) {

      // Obtain the prepared statement cache
      PreparedStatementCache cache = context.getPreparedStatementCache();

      // Obtain the query
      String sql = portal.getSql();
      ParameterHolder holder = portal.getParameterHolder();
      query = cache.getQuery(sql, holder.getParamTypes());

      // Associate query to portal
      portal.setQuery(query);
    }

    // Determine if prepare query
    if ((!query.isParsed()) && (!query.isWaitingParse())) {

      // Obtain the query details
      String sql = portal.getSql();
      ParameterHolder holder = portal.getParameterHolder();

      // Send the prepare packet
      NetworkOutputStream wire = context.getOutputStream();
      wire.write(FeFrame.FrontendTag.PARSE.getByte());
      wire.initPacket();
      wire.write(query.getQueryName());
      wire.write(sql);
      wire.write(BinaryHelper.writeShort(holder.size()));
      for (QueryParameter qp : holder.parameters()) {
        wire.write(BinaryHelper.writeInt(qp.getOid()));
      }
      wire.completePacket();
    }

    // Determine if describe or bind
    return new DescribeRequest<>(portal);

  }

  @Override
  public NetworkResponse getRequiredResponse() {
    Query query = portal.getQuery();

    // Determine if waiting on parse
    if (!query.isWaitingParse()) {
      query.flagWaitingParse();
      return new ParseResponse(portal);
    }

    // Already waiting on parse
    return null;
  }

}