package org.postgresql.sql2.operations.helpers;

import jdk.incubator.sql2.SqlType;
import org.postgresql.sql2.communication.packets.parts.PGAdbaType;

import java.util.concurrent.ExecutionException;

public class ValueQueryParameter implements QueryParameter {
  private PGAdbaType type;
  private Object value;

  public ValueQueryParameter(Object value) {
    this.value = value;

    if (value == null) {
      type = PGAdbaType.NULL;
    } else {
      type = PGAdbaType.guessTypeFromClass(value.getClass());
    }
  }

  public ValueQueryParameter(Object value, SqlType type) {
    this.value = value;
    if (type != null) {
      this.type = PGAdbaType.convert(type);
    } else {
      if (value == null) {
        this.type = PGAdbaType.NULL;
      } else {
        this.type = PGAdbaType.guessTypeFromClass(value.getClass());
      }
    }
  }

  @Override
  public int getOID() {
    return type.getVendorTypeNumber();
  }

  @Override
  public short getParameterFormatCode() {
    return type.getFormatCodeTypes().getCode();
  }

  @Override
  public byte[] getParameter(int index) throws ExecutionException, InterruptedException {
    return type.getByteGenerator().apply(value);
  }

  @Override
  public int numberOfQueryRepetitions() {
    return 1;
  }
}
