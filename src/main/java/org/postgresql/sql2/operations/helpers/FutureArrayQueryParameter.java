package org.postgresql.sql2.operations.helpers;

import jdk.incubator.sql2.SqlType;
import org.postgresql.sql2.communication.packets.parts.PGAdbaType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class FutureArrayQueryParameter implements QueryParameter {
  private PGAdbaType type;
  private List<?> values;
  private CompletionStage<?> valueHolder;

  public FutureArrayQueryParameter(CompletionStage<?> valueHolder) {
    this.valueHolder = valueHolder;
  }

  public FutureArrayQueryParameter(CompletionStage<?> valueHolder, SqlType type) {
    this.valueHolder = valueHolder;
    this.type = PGAdbaType.convert(type);
  }

  private void resolveType() throws ExecutionException, InterruptedException {
    if (type == null && values == null && valueHolder == null) {
      type = PGAdbaType.NULL;
    } else if (type == null && valueHolder != null) {
      Object value = valueHolder.toCompletableFuture().get();
      valueHolder = null;

      if (value == null) {
        type = PGAdbaType.NULL;
      } else {
        assignValues(value);

        Object firstNonNull = firstNonNull(values);
        if (firstNonNull == null) {
          type = PGAdbaType.NULL;
        } else {
          type = PGAdbaType.guessTypeFromClass(firstNonNull.getClass());
        }
      }
    } else if (values == null && valueHolder != null) {
      assignValues(valueHolder.toCompletableFuture().get());
      valueHolder = null;
    }
  }

  private void assignValues(Object value) {
    if (List.class.isAssignableFrom(value.getClass())) {
      values = (List<?>) value;
    } else if (value.getClass().isArray()) {
      values = Arrays.asList((Object[]) value);
    } else {
      throw new IllegalArgumentException("the future didn't produce neither an array nor a list");
    }
  }

  private Object firstNonNull(List<?> values) {
    return values.stream().filter(Objects::nonNull).findFirst();
  }

  @Override
  public int getOID() throws ExecutionException, InterruptedException {
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
    resolveType();

    return type.getByteGenerator().apply(values.get(index));
  }

  @Override
  public int numberOfQueryRepetitions() throws ExecutionException, InterruptedException {
    resolveType();

    return values.size();
  }
}
