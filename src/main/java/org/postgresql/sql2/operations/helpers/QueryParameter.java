package org.postgresql.sql2.operations.helpers;

import jdk.incubator.sql2.SqlType;
import org.postgresql.sql2.communication.packets.parts.PGAdbaType;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class QueryParameter {
  private PGAdbaType type;
  private Object value;
  private CompletionStage<?> valueHolder;

  public QueryParameter(Object value) {
    this.value = value;

    if(value == null) {
      type = PGAdbaType.NULL;
    } else {
      type = PGAdbaType.guessTypeFromClass(value.getClass());
    }
  }

  public QueryParameter(Object value, SqlType type) {
    this.value = value;
    if(type != null) {
      this.type = PGAdbaType.convert(type);
    } else {
      if(value == null) {
        this.type = PGAdbaType.NULL;
      } else {
        this.type = PGAdbaType.guessTypeFromClass(value.getClass());
      }
    }
  }

  public QueryParameter(CompletionStage<?> valueHolder) {
    this.valueHolder = valueHolder;
  }

  public QueryParameter(CompletionStage<?> valueHolder, SqlType type) {
    this.valueHolder = valueHolder;
    this.type = PGAdbaType.convert(type);
  }

  private void resolveType() throws ExecutionException, InterruptedException {
    if(type == null && value == null && valueHolder == null) {
      type = PGAdbaType.NULL;
    } else if(type == null && valueHolder != null) {
      value = valueHolder.toCompletableFuture().get();
      valueHolder = null;

      if(value == null) {
        type = PGAdbaType.NULL;
      } else {
        type = PGAdbaType.guessTypeFromClass(value.getClass());
      }
    }

  }

  public int getOID() throws ExecutionException, InterruptedException {
    resolveType();

    return type.getVendorTypeNumber();
  }

  public short getParameterFormatCode() throws ExecutionException, InterruptedException {
    resolveType();

    return type.getFormatCodeTypes().getCode();
  }

  public byte[] getParameter() throws ExecutionException, InterruptedException {
    if (valueHolder != null) {
      return type.getByteGenerator().apply(valueHolder.toCompletableFuture().get());
    } else {
      return type.getByteGenerator().apply(value);
    }
  }
}
