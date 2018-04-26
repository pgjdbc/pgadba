package org.postgresql.sql2.operations.helpers;

import org.postgresql.sql2.communication.FEFrame;
import org.postgresql.sql2.util.BinaryHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FEFrameSerializer {
  public static FEFrame toBindPacket(ParameterHolder holder) {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      os.write(FEFrame.FrontendTag.BIND.getByte());
      os.write(0);
      os.write(0);
      os.write(0);
      os.write(0);
      os.write("name of portal".getBytes(StandardCharsets.UTF_8));
      os.write(0);
      os.write("name of query".getBytes(StandardCharsets.UTF_8));
      os.write(0);
      os.write(BinaryHelper.writeShort(holder.size()));
      for(QueryParameter qp : holder.parameters()) {
        os.write(BinaryHelper.writeShort(qp.getParameterFormatCode()));
      }
      os.write(BinaryHelper.writeShort(holder.size()));
      for(QueryParameter qp : holder.parameters()) {
        os.write(BinaryHelper.writeInt(qp.getParameterLength()));
        os.write(qp.getParameter());
      }
      os.write(BinaryHelper.writeShort((short) 0));
      return new FEFrame(os.toByteArray(), false);
    } catch (IOException e) {
      e.printStackTrace();
      throw new Error(e.getMessage());
    }
  }

  public static FEFrame toParsePacket(ParameterHolder holder, String sql) {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      os.write(FEFrame.FrontendTag.PARSE.getByte());
      os.write(0);
      os.write(0);
      os.write(0);
      os.write(0);
      os.write("name of query".getBytes(StandardCharsets.UTF_8));
      os.write(0);
      os.write(sql.getBytes(StandardCharsets.UTF_8));
      os.write(0);
      os.write(BinaryHelper.writeShort(holder.size()));
      for(QueryParameter qp : holder.parameters()) {
        os.write(BinaryHelper.writeInt(qp.getOID()));
      }
      return new FEFrame(os.toByteArray(), false);
    } catch (IOException e) {
      e.printStackTrace();
      throw new Error(e.getMessage());
    }
  }

  public static FEFrame toExecutePacket(ParameterHolder holder, String sql) {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      os.write(FEFrame.FrontendTag.EXECUTE.getByte());
      os.write(0);
      os.write(0);
      os.write(0);
      os.write(0);
      os.write("name of portal".getBytes(StandardCharsets.UTF_8));
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
}
