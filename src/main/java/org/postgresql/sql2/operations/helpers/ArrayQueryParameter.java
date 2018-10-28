package org.postgresql.sql2.operations.helpers;

import jdk.incubator.sql2.SqlType;
import org.postgresql.sql2.communication.packets.parts.PgAdbaType;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ArrayQueryParameter implements QueryParameter {
  private PgAdbaType type;
  private List<?> values;

  /**
   * If a parameter needs an array of values, in order to repeat the query multiple times.
   * @param values the values
   */
  public ArrayQueryParameter(List<?> values) {
    this.values = values;

    Object value = firstNonNull(values);
    if (value == null) {
      type = PgAdbaType.NULL;
    } else {
      type = PgAdbaType.guessTypeFromClass(value.getClass());
    }
  }

  /**
   * If a parameter needs an array of values, in order to repeat the query multiple times.
   * @param values the values
   * @param type the type these values have
   */
  public ArrayQueryParameter(List<?> values, SqlType type) {
    this.values = values;
    if (type != null) {
      this.type = PgAdbaType.convert(type);
    } else {
      Object value = firstNonNull(values);
      if (value == null) {
        this.type = PgAdbaType.NULL;
      } else {
        this.type = PgAdbaType.guessTypeFromClass(value.getClass());
      }
    }
  }

  private Object firstNonNull(List<?> values) {
    return values.stream().filter(Objects::nonNull).findFirst();
  }

  @Override
  public int getOid() {
    return type.getOid();
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
