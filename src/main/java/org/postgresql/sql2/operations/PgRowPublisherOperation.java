package org.postgresql.sql2.operations;

import jdk.incubator.sql2.ParameterizedRowPublisherOperation;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.SqlType;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PgConnection;
import org.postgresql.sql2.PgSubmission;
import org.postgresql.sql2.operations.helpers.FutureQueryParameter;
import org.postgresql.sql2.operations.helpers.ParameterHolder;
import org.postgresql.sql2.operations.helpers.ValueQueryParameter;
import org.postgresql.sql2.submissions.GroupSubmission;
import org.postgresql.sql2.submissions.ProcessorSubmission;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

public class PgRowPublisherOperation<R> implements ParameterizedRowPublisherOperation<R> {
  private PgConnection connection;
  private String sql;
  private ParameterHolder holder;
  private Consumer<Throwable> errorHandler;
  private SubmissionPublisher<Result.RowColumn> publisher = new SubmissionPublisher<>();
  private PgSubmission<R> submission;
  private GroupSubmission groupSubmission;
  private CompletionStage<? extends R> result;

  /**
   * An Operation that accepts parameters, subscribes to a sequence of rows, and
   * returns a result.
   * @param connection connection that the query should be part of
   * @param sql the query
   * @param groupSubmission the group that this execution should be part of
   */
  public PgRowPublisherOperation(PgConnection connection, String sql, GroupSubmission groupSubmission) {
    this.connection = connection;
    this.sql = sql;
    this.holder = new ParameterHolder();
    this.groupSubmission = groupSubmission;
  }

  @Override
  public ParameterizedRowPublisherOperation<R> onError(Consumer<Throwable> errorHandler) {
    if (this.errorHandler != null) {
      throw new IllegalStateException("you are not allowed to call onError multiple times");
    }

    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public ParameterizedRowPublisherOperation<R> set(String id, Object value) {
    holder.add(id, new ValueQueryParameter(value));
    return this;
  }

  @Override
  public ParameterizedRowPublisherOperation<R> subscribe(Flow.Subscriber<? super Result.RowColumn> subscriber,
      CompletionStage<? extends R> result) {
    publisher.subscribe(subscriber);
    this.result = result;
    result.thenAccept(r -> {
      if (groupSubmission != null) {
        groupSubmission.addGroupResult(r);
      }
      submission.getCompletionStage().toCompletableFuture().complete(r);
    });
    return this;
  }

  @Override
  public ParameterizedRowPublisherOperation<R> set(String id, Object value, SqlType type) {
    holder.add(id, new ValueQueryParameter(value, type));
    return this;
  }

  @Override
  public ParameterizedRowPublisherOperation<R> set(String id, CompletionStage<?> source) {
    holder.add(id, new FutureQueryParameter(source));
    return this;
  }

  @Override
  public ParameterizedRowPublisherOperation<R> set(String id, CompletionStage<?> source, SqlType type) {
    holder.add(id, new FutureQueryParameter(source, type));
    return this;
  }

  @Override
  public ParameterizedRowPublisherOperation<R> timeout(Duration minTime) {
    return this;
  }

  @Override
  public Submission<R> submit() {
    submission = new ProcessorSubmission<>(this::cancel, errorHandler, sql, publisher, holder, groupSubmission);
    connection.submit(submission);
    return submission;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
