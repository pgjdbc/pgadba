package org.postgresql.sql2.operations;

import jdk.incubator.sql2.ParameterizedCountOperation;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.RowOperation;
import jdk.incubator.sql2.SqlType;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.communication.FEFrame;
import org.postgresql.sql2.operations.helpers.ParameterHolder;
import org.postgresql.sql2.operations.helpers.QueryParameter;
import org.postgresql.sql2.util.BinaryHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

public class PGCountOperation<R> implements ParameterizedCountOperation<R> {
  private PGConnection connection;
  private String sql;
  private ParameterHolder holder;

  public PGCountOperation(PGConnection connection, String sql) {
    this.connection = connection;
    this.sql = sql;
    this.holder = new ParameterHolder();
  }

  @Override
  public RowOperation<R> returning(String... keys) {
    return null;
  }

  @Override
  public ParameterizedCountOperation<R> onError(Consumer<Throwable> handler) {
    return this;
  }

  @Override
  public ParameterizedCountOperation<R> apply(Function<Result.Count, ? extends R> processor) {
    return null;
  }

  @Override
  public ParameterizedCountOperation<R> set(String id, Object value) {
    holder.add(id, new QueryParameter(value));
    return this;
  }

  @Override
  public ParameterizedCountOperation<R> set(String id, Object value, SqlType type) {
    holder.add(id, new QueryParameter(value, type));
    return this;
  }

  @Override
  public ParameterizedCountOperation<R> set(String id, CompletionStage<?> source) {
    holder.add(id, new QueryParameter(source));
    return this;
  }

  @Override
  public ParameterizedCountOperation<R> set(String id, CompletionStage<?> source, SqlType type) {
    holder.add(id, new QueryParameter(source, type));
    return this;
  }

  @Override
  public ParameterizedCountOperation<R> timeout(Duration minTime) {
    return this;
  }

  @Override
  public Submission<R> submit() {
    connection.queFrame(toParsePacket());
    connection.queFrame(toBindPacket());
    return null;
  }

  private FEFrame toBindPacket() {
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

  private FEFrame toParsePacket() {
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
}
