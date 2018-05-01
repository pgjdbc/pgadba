package org.postgresql.sql2.operations.helpers;

import jdk.incubator.sql2.SqlType;
import org.postgresql.sql2.communication.packets.parts.PGAdbaType;

import java.util.concurrent.CompletionStage;

public class QueryParameter {
  private PGAdbaType type;
  private Object value;
  private CompletionStage<?> valueHolder;

  public QueryParameter(Object value) {
    this.value = value;
  }

  public QueryParameter(Object value, SqlType type) {
    this.value = value;
    this.type = PGAdbaType.convert(type);
  }

  public QueryParameter(CompletionStage<?> valueHolder) {
    this.valueHolder = valueHolder;
  }

  public QueryParameter(CompletionStage<?> valueHolder, SqlType type) {
    this.valueHolder = valueHolder;
    this.type = PGAdbaType.convert(type);
  }

  public int getOID() {
    return type.getVendorTypeNumber();
  }

  public short getParameterFormatCode() {
    return type.getFormatCodeTypes().getCode();
  }

  public byte[] getParameter() {
    return type.getByteGenerator().apply(value);
  }
}
