package org.postgresql.sql2.operations;

import jdk.incubator.sql2.ParameterizedCountOperation;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.RowOperation;
import jdk.incubator.sql2.SqlType;
import jdk.incubator.sql2.Submission;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

public class PGCountOperation<R> implements ParameterizedCountOperation<R> {
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
    return this;
  }

  @Override
  public ParameterizedCountOperation<R> set(String id, Object value, SqlType type) {
    return this;
  }

  @Override
  public ParameterizedCountOperation<R> set(String id, CompletionStage<?> source) {
    return this;
  }

  @Override
  public ParameterizedCountOperation<R> set(String id, CompletionStage<?> source, SqlType type) {
    return this;
  }

  @Override
  public ParameterizedCountOperation<R> timeout(Duration minTime) {
    return this;
  }

  @Override
  public Submission<R> submit() {
    return null;
  }
}
