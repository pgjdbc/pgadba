package org.postgresql.adba.operations;

import jdk.incubator.sql2.ParameterizedRowCountOperation;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.RowOperation;
import jdk.incubator.sql2.SqlType;
import jdk.incubator.sql2.Submission;
import org.postgresql.adba.PgSession;
import org.postgresql.adba.PgSubmission;
import org.postgresql.adba.operations.helpers.FutureQueryParameter;
import org.postgresql.adba.operations.helpers.ParameterHolder;
import org.postgresql.adba.operations.helpers.ValueQueryParameter;
import org.postgresql.adba.submissions.CountSubmission;
import org.postgresql.adba.submissions.GroupSubmission;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

public class PgRowCountOperation<R> implements ParameterizedRowCountOperation<R> {
  private PgSession connection;
  private String sql;
  private ParameterHolder holder;
  private Consumer<Throwable> errorHandler;
  private PgSubmission returningRowSubmission;
  private GroupSubmission groupSubmission;

  /**
   * A RowCountOperation, this operation returns a count of the number of rows that the query affected.
   * @param connection connection that the query should be part of
   * @param sql the query
   * @param groupSubmission the group that this execution should be part of
   */
  public PgRowCountOperation(PgSession connection, String sql, GroupSubmission groupSubmission) {
    this.connection = connection;
    this.sql = sql;
    this.holder = new ParameterHolder();
    this.groupSubmission = groupSubmission;
  }

  @Override
  public RowOperation<R> returning(String... keys) {
    return new PgRowOperation<>(this, keys);
  }

  @Override
  public ParameterizedRowCountOperation<R> onError(Consumer<Throwable> errorHandler) {
    if (this.errorHandler != null) {
      throw new IllegalStateException("you are not allowed to call onError multiple times");
    }

    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public ParameterizedRowCountOperation<R> apply(Function<Result.RowCount, ? extends R> processor) {
    return this;
  }

  @Override
  public ParameterizedRowCountOperation<R> set(String id, Object value) {
    holder.add(id, new ValueQueryParameter(value));
    return this;
  }

  @Override
  public ParameterizedRowCountOperation<R> set(String id, Object value, SqlType type) {
    holder.add(id, new ValueQueryParameter(value, type));
    return this;
  }

  @Override
  public ParameterizedRowCountOperation<R> set(String id, CompletionStage<?> source) {
    holder.add(id, new FutureQueryParameter(source));
    return this;
  }

  @Override
  public ParameterizedRowCountOperation<R> set(String id, CompletionStage<?> source, SqlType type) {
    holder.add(id, new FutureQueryParameter(source, type));
    return this;
  }

  @Override
  public ParameterizedRowCountOperation<R> timeout(Duration minTime) {
    return this;
  }

  @Override
  public Submission<R> submit() {
    PgSubmission<R> submission = new CountSubmission<>(this::cancel, errorHandler, holder, returningRowSubmission, sql,
        groupSubmission);
    connection.submit(submission);
    return submission;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }

  public <T> void addReturningRowSubmission(PgSubmission<T> submission) {
    returningRowSubmission = submission;
  }
}
