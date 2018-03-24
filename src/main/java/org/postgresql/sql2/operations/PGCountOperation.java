package org.postgresql.sql2.operations;

import java2.sql2.CountOperation;
import java2.sql2.ParameterizedCountOperation;
import java2.sql2.Result;
import java2.sql2.RowOperation;
import java2.sql2.SqlType;
import java2.sql2.Submission;

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
  public ParameterizedCountOperation<R> apply(Function<? super Result.Count, ? extends R> processor) {
    return this;
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
