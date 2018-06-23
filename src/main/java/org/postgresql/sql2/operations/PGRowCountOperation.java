package org.postgresql.sql2.operations;

import jdk.incubator.sql2.ParameterizedRowCountOperation;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.RowOperation;
import jdk.incubator.sql2.SqlType;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.operations.helpers.FutureQueryParameter;
import org.postgresql.sql2.operations.helpers.ParameterHolder;
import org.postgresql.sql2.operations.helpers.ValueQueryParameter;
import org.postgresql.sql2.submissions.CountSubmission;
import org.postgresql.sql2.submissions.GroupSubmission;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

public class PGRowCountOperation<R> implements ParameterizedRowCountOperation<R> {
  private PGConnection connection;
  private String sql;
  private ParameterHolder holder;
  private Consumer<Throwable> errorHandler;
  private PGSubmission returningRowSubmission;
  private GroupSubmission groupSubmission;

  public PGRowCountOperation(PGConnection connection, String sql, GroupSubmission groupSubmission) {
    this.connection = connection;
    this.sql = sql;
    this.holder = new ParameterHolder();
    this.groupSubmission = groupSubmission;
  }

  @Override
  public RowOperation<R> returning(String... keys) {
    return new PGRowOperation<>(this, keys);
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
    PGSubmission<R> submission = new CountSubmission<>(this::cancel, errorHandler, holder, returningRowSubmission, sql, groupSubmission);
    connection.addSubmissionOnQue(submission);
    return submission;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }

  public <T> void addReturningRowSubmission(PGSubmission<T> submission) {
    returningRowSubmission = submission;
  }
}
