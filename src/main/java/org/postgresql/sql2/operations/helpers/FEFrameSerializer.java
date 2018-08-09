package org.postgresql.sql2.operations.helpers;

import org.postgresql.sql2.communication.FEFrame;
import org.postgresql.sql2.communication.PreparedStatementCache;
import org.postgresql.sql2.util.BinaryHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

@Deprecated
public class FEFrameSerializer {
  public static FEFrame toBindPacket(ParameterHolder holder, String sql, PreparedStatementCache cache, int index) {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      os.write(FEFrame.FrontendTag.BIND.getByte());
      os.write(0);
      os.write(0);
      os.write(0);
      os.write(0);
      //os.write(cache.getNameForPortal(sql, holder.getParamTypes()).getBytes(StandardCharsets.UTF_8));
      os.write(0);
      //os.write(cache.getNameForQuery(sql, holder.getParamTypes()).getBytes(StandardCharsets.UTF_8));
      os.write(0);
      os.write(BinaryHelper.writeShort(holder.size()));
      for (QueryParameter qp : holder.parameters()) {
        os.write(BinaryHelper.writeShort(qp.getParameterFormatCode()));
      }
      os.write(BinaryHelper.writeShort(holder.size()));
      for (QueryParameter qp : holder.parameters()) {
        byte[] paramData = qp.getParameter(index);
        if (paramData.length == 0) { //handling the null special case
          os.write(BinaryHelper.writeInt(-1));
        } else {
          os.write(BinaryHelper.writeInt(paramData.length));
          os.write(paramData);
        }
      }
      os.write(BinaryHelper.writeShort((short) 0));
      return new FEFrame(os.toByteArray(), false);
    } catch (IOException | InterruptedException | ExecutionException e) {
      e.printStackTrace();
      throw new Error(e.getMessage());
    }
  }

  public static FEFrame toParsePacket(ParameterHolder holder, String sql, PreparedStatementCache cache) {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      os.write(FEFrame.FrontendTag.PARSE.getByte());
      os.write(0);
      os.write(0);
      os.write(0);
      os.write(0);
      //os.write(cache.getNameForQuery(sql, holder.getParamTypes()).getBytes(StandardCharsets.UTF_8));
      os.write(0);
      os.write(sql.getBytes(StandardCharsets.UTF_8));
      os.write(0);
      os.write(BinaryHelper.writeShort(holder.size()));
      for (QueryParameter qp : holder.parameters()) {
        os.write(BinaryHelper.writeInt(qp.getOID()));
      }
      return new FEFrame(os.toByteArray(), false);
    } catch (IOException | InterruptedException | ExecutionException e) {
      e.printStackTrace();
      throw new Error(e.getMessage());
    }
  }

  public static FEFrame toExecutePacket(ParameterHolder holder, String sql, PreparedStatementCache cache) throws ExecutionException, InterruptedException {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      os.write(FEFrame.FrontendTag.EXECUTE.getByte());
      os.write(0);
      os.write(0);
      os.write(0);
      os.write(0);
      //os.write(cache.getNameForPortal(sql, holder.getParamTypes()).getBytes(StandardCharsets.UTF_8));
      os.write(0);
      os.write(BinaryHelper.writeInt(0)); // number of rows to return, 0 == all
      return new FEFrame(os.toByteArray(), false);
    } catch (IOException e) {
      e.printStackTrace();
      throw new Error(e.getMessage());
    }
  }

  public static FEFrame toSyncPacket() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    os.write(FEFrame.FrontendTag.SYNC.getByte());
    os.write(0);
    os.write(0);
    os.write(0);
    os.write(0);
    return new FEFrame(os.toByteArray(), false);
  }

  public static FEFrame toDescribePacket(ParameterHolder holder, String sql, PreparedStatementCache preparedStatementCache) {
//    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      os.write(FEFrame.FrontendTag.DESCRIBE.getByte());
      os.write(0);
      os.write(0);
      os.write(0);
      os.write(0);
      os.write('S');
      //os.write(preparedStatementCache.getNameForQuery(sql, holder.getParamTypes()).getBytes(StandardCharsets.UTF_8));
      os.write(0);
      return new FEFrame(os.toByteArray(), false);
//    } catch (IOException | ExecutionException | InterruptedException e) {
//      e.printStackTrace();
//      throw new Error(e.getMessage());
//    }
  }
}
