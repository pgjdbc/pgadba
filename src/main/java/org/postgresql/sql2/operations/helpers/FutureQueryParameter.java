package org.postgresql.sql2.operations.helpers;

import jdk.incubator.sql2.SqlType;
import org.postgresql.sql2.communication.packets.parts.PgAdbaType;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class FutureQueryParameter implements QueryParameter {
  private PgAdbaType type;
  private Object value;
  private CompletionStage<?> valueHolder;

  public FutureQueryParameter(CompletionStage<?> valueHolder) {
    this.valueHolder = valueHolder;
  }

  public FutureQueryParameter(CompletionStage<?> valueHolder, SqlType type) {
    this.valueHolder = valueHolder;
    this.type = PgAdbaType.convert(type);
  }

  private void resolveType() throws ExecutionException, InterruptedException {
    if (type == null && value == null && valueHolder == null) {
      type = PgAdbaType.NULL;
    } else if (type == null && valueHolder != null) {
      value = valueHolder.toCompletableFuture().get();
      valueHolder = null;

      if (value == null) {
        type = PgAdbaType.NULL;
      } else {
        type = PgAdbaType.guessTypeFromClass(value.getClass());
      }
    }

  }

  @Override
  public int getOid() throws ExecutionException, InterruptedException {
    resolveType();

    return type.getVendorTypeNumber();
  }

  @Override
  public short getParameterFormatCode() throws ExecutionException, InterruptedException {
    resolveType();

    return type.getFormatCodeTypes().getCode();
  }

  @Override
  public byte[] getParameter(int index) throws ExecutionException, InterruptedException {
    if (valueHolder != null) {
      return type.getByteGenerator().apply(valueHolder.toCompletableFuture().get());
    } else {
      return type.getByteGenerator().apply(value);
    }
  }

  @Override
  public int numberOfQueryRepetitions() {
    return 1;
  }
}
