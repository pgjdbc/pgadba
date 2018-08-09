package org.postgresql.sql2.operations;

import jdk.incubator.sql2.ArrayRowCountOperation;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.SqlType;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.operations.helpers.ArrayQueryParameter;
import org.postgresql.sql2.operations.helpers.FutureArrayQueryParameter;
import org.postgresql.sql2.operations.helpers.ParameterHolder;
import org.postgresql.sql2.submissions.ArrayCountSubmission;
import org.postgresql.sql2.submissions.GroupSubmission;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.stream.Collector;

public class PGArrayRowCountOperation<R> implements ArrayRowCountOperation<R> {
  private final PGConnection connection;
  private final String sql;
  private ParameterHolder holder;
  private Consumer<Throwable> errorHandler;
  private Collector collector;
  private GroupSubmission groupSubmission;

  public PGArrayRowCountOperation(PGConnection connection, String sql, GroupSubmission groupSubmission) {
    this.connection = connection;
    this.sql = sql;
    this.holder = new ParameterHolder();
    this.groupSubmission = groupSubmission;
  }

  @Override
  public ArrayRowCountOperation<R> set(String id, List<?> values, SqlType type) {
    holder.add(id, new ArrayQueryParameter(values, type));
    return this;
  }

  @Override
  public ArrayRowCountOperation<R> set(String id, List<?> values) {
    holder.add(id, new ArrayQueryParameter(values));
    return this;
  }

  @Override
  public <S> ArrayRowCountOperation<R> set(String id, S[] values, SqlType type) {
    holder.add(id, new ArrayQueryParameter(Arrays.asList(values), type));
    return this;
  }

  @Override
  public <S> ArrayRowCountOperation<R> set(String id, S[] values) {
    holder.add(id, new ArrayQueryParameter(Arrays.asList(values)));
    return this;
  }

  @Override
  public ArrayRowCountOperation<R> set(String id, CompletionStage<?> source, SqlType type) {
    holder.add(id, new FutureArrayQueryParameter(source, type));
    return this;
  }

  @Override
  public ArrayRowCountOperation<R> set(String id, CompletionStage<?> source) {
    holder.add(id, new FutureArrayQueryParameter(source));
    return this;
  }

  @Override
  public <A, S extends R> ArrayRowCountOperation<R> collect(Collector<? super Result.RowCount, A, S> c) {
    this.collector = c;
    return this;
  }

  @Override
  public Submission<R> submit() {
    PGSubmission<R> submission = new ArrayCountSubmission<>(this::cancel, errorHandler, holder, sql, groupSubmission);
    if (collector != null) {
      submission.setCollector(collector);
    }
    connection.submit(submission);
    return submission;
  }

  @Override
  public ArrayRowCountOperation<R> onError(Consumer<Throwable> errorHandler) {
    if (this.errorHandler != null) {
      throw new IllegalStateException("you are not allowed to call onError multiple times");
    }

    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public ArrayRowCountOperation<R> timeout(Duration minTime) {
    return this;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
