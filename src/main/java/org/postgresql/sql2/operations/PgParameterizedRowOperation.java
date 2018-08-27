package org.postgresql.sql2.operations;

import jdk.incubator.sql2.ParameterizedRowOperation;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.SqlType;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PgConnection;
import org.postgresql.sql2.operations.helpers.FutureQueryParameter;
import org.postgresql.sql2.operations.helpers.ParameterHolder;
import org.postgresql.sql2.operations.helpers.ValueQueryParameter;
import org.postgresql.sql2.submissions.GroupSubmission;
import org.postgresql.sql2.submissions.RowSubmission;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.stream.Collector;

public class PgParameterizedRowOperation<R> implements ParameterizedRowOperation<R> {
  private PgConnection connection;
  private String sql;
  private ParameterHolder holder;
  private Collector collector = Collector.of(() -> null, (a, v) -> {
  }, (a, b) -> null, a -> null);
  private Consumer<Throwable> errorHandler;
  private GroupSubmission groupSubmission;

  /**
   * Creates a ParameterizedRowOperation, an operation that accepts parameters and processes a sequence of rows.
   * @param connection connection that the query should be part of
   * @param sql the query
   * @param groupSubmission the group that this execution should be part of
   */
  public PgParameterizedRowOperation(PgConnection connection, String sql, GroupSubmission groupSubmission) {
    this.connection = connection;
    this.sql = sql;
    this.holder = new ParameterHolder();
    this.groupSubmission = groupSubmission;
  }

  @Override
  public ParameterizedRowOperation<R> onError(Consumer<Throwable> errorHandler) {
    if (this.errorHandler != null) {
      throw new IllegalStateException("you are not allowed to call onError multiple times");
    }

    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public ParameterizedRowOperation<R> fetchSize(long rows) throws IllegalArgumentException {
    return this;
  }

  @Override
  public <A, S extends R> ParameterizedRowOperation<R> collect(Collector<? super Result.RowColumn, A, S> c) {
    collector = c;
    return this;
  }

  @Override
  public ParameterizedRowOperation<R> set(String id, Object value) {
    holder.add(id, new ValueQueryParameter(value));
    return this;
  }

  @Override
  public ParameterizedRowOperation<R> set(String id, Object value, SqlType type) {
    holder.add(id, new ValueQueryParameter(value, type));
    return this;
  }

  @Override
  public ParameterizedRowOperation<R> set(String id, CompletionStage<?> source) {
    holder.add(id, new FutureQueryParameter(source));
    return this;
  }

  @Override
  public ParameterizedRowOperation<R> set(String id, CompletionStage<?> source, SqlType type) {
    holder.add(id, new FutureQueryParameter(source, type));
    return this;
  }

  @Override
  public ParameterizedRowOperation<R> timeout(Duration minTime) {
    return this;
  }

  @Override
  public Submission<R> submit() {
    RowSubmission<R> submission = new RowSubmission<>(this::cancel, errorHandler, holder, groupSubmission, sql);
    submission.setCollector(collector);
    connection.submit(submission);

    return submission;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
