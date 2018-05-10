package org.postgresql.sql2.operations.helpers;

import jdk.incubator.sql2.SqlType;
import org.postgresql.sql2.communication.packets.parts.PGAdbaType;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ArrayQueryParameter implements QueryParameter {
  private PGAdbaType type;
  private List<?> values;

  public ArrayQueryParameter(List<?> values) {
    this.values = values;

    Object value = firstNonNull(values);
    if(value == null) {
      type = PGAdbaType.NULL;
    } else {
      type = PGAdbaType.guessTypeFromClass(value.getClass());
    }
  }

  public ArrayQueryParameter(List<?> values, SqlType type) {
    this.values = values;
    if(type != null) {
      this.type = PGAdbaType.convert(type);
    } else {
      Object value = firstNonNull(values);
      if(value == null) {
        this.type = PGAdbaType.NULL;
      } else {
        this.type = PGAdbaType.guessTypeFromClass(value.getClass());
      }
    }
  }

  private Object firstNonNull(List<?> values) {
    return values.stream().filter(Objects::nonNull).findFirst();
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
    return type.getByteGenerator().apply(values.get(index));
  }

  @Override
  public int numberOfQueryRepetitions() {
    return values.size();
  }
}
