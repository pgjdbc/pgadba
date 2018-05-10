package org.postgresql.sql2.operations;

import jdk.incubator.sql2.ArrayCountOperation;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.SqlType;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.operations.helpers.ArrayQueryParameter;
import org.postgresql.sql2.operations.helpers.FutureArrayQueryParameter;
import org.postgresql.sql2.operations.helpers.ParameterHolder;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.stream.Collector;

public class PGArrayCountOperation<R> implements ArrayCountOperation<R> {
  private final PGConnection connection;
  private final String sql;
  private ParameterHolder holder;

  public PGArrayCountOperation(PGConnection connection, String sql) {
    this.connection = connection;
    this.sql = sql;
    this.holder = new ParameterHolder();
  }

  @Override
  public ArrayCountOperation<R> set(String id, List<?> values, SqlType type) {
    holder.add(id, new ArrayQueryParameter(values, type));
    return this;
  }

  @Override
  public ArrayCountOperation<R> set(String id, List<?> values) {
    holder.add(id, new ArrayQueryParameter(values));
    return this;
  }

  @Override
  public <S> ArrayCountOperation<R> set(String id, S[] values, SqlType type) {
    holder.add(id, new ArrayQueryParameter(Arrays.asList(values), type));
    return this;
  }

  @Override
  public <S> ArrayCountOperation<R> set(String id, S[] values) {
    holder.add(id, new ArrayQueryParameter(Arrays.asList(values)));
    return this;
  }

  @Override
  public ArrayCountOperation<R> set(String id, CompletionStage<?> source, SqlType type) {
    holder.add(id, new FutureArrayQueryParameter(source, type));
    return this;
  }

  @Override
  public ArrayCountOperation<R> set(String id, CompletionStage<?> source) {
    holder.add(id, new FutureArrayQueryParameter(source));
    return this;
  }

  @Override
  public <A, S extends R> ArrayCountOperation<R> collect(Collector<? super Result.Count, A, S> c) {
    return this;
  }

  @Override
  public Submission<R> submit() {
    PGSubmission<R> submission = new PGSubmission<>(this::cancel);
    submission.setConnectionSubmission(false);
    submission.setSql(sql);
    submission.setHolder(holder);
    submission.setCompletionType(PGSubmission.Types.ARRAY_COUNT);
    connection.addSubmissionOnQue(submission);
    return submission;
  }

  @Override
  public ArrayCountOperation<R> onError(Consumer<Throwable> handler) {
    return this;
  }

  @Override
  public ArrayCountOperation<R> timeout(Duration minTime) {
    return this;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
