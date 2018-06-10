package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.RowProcessorOperation;
import jdk.incubator.sql2.SqlType;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.operations.helpers.FutureQueryParameter;
import org.postgresql.sql2.operations.helpers.ParameterHolder;
import org.postgresql.sql2.operations.helpers.ValueQueryParameter;
import org.postgresql.sql2.submissions.BaseSubmission;
import org.postgresql.sql2.submissions.ProcessorSubmission;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

public class PGRowProcessorOperation<R> implements RowProcessorOperation<R> {
  private PGConnection connection;
  private String sql;
  private ParameterHolder holder;
  private Consumer<Throwable> errorHandler;
  private Flow.Subscriber<R> subscriber;
  private SubmissionPublisher<Result.Row> publisher = new SubmissionPublisher<>();
  private R lastValue;
  private PGSubmission<R> submission;
  private BaseSubmission groupSubmission;

  public PGRowProcessorOperation(PGConnection connection, String sql, BaseSubmission groupSubmission) {
    this.connection = connection;
    this.sql = sql;
    this.holder = new ParameterHolder();
    this.groupSubmission = groupSubmission;

    subscriber = new Flow.Subscriber<>() {
      private Flow.Subscription subscription;
      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
      }

      @Override
      public void onNext(R item) {
        lastValue = item;
        subscription.request(1);
      }

      @Override
      public void onError(Throwable throwable) {
        submission.getCompletionStage().toCompletableFuture().completeExceptionally(throwable);
      }

      @Override
      public void onComplete() {
        if(submission.getGroupSubmission() != null) {
          submission.getGroupSubmission().addGroupResult(lastValue);
        }
        submission.getCompletionStage().toCompletableFuture().complete(lastValue);
      }
    };
  }

  @Override
  public RowProcessorOperation<R> rowProcessor(Flow.Processor<Result.Row, ? extends R> processor) {
    publisher.subscribe(processor);
    processor.subscribe(subscriber);
    return this;
  }

  @Override
  public RowProcessorOperation<R> inactivityTimeout(Duration minTime) {
    return this;
  }

  @Override
  public RowProcessorOperation<R> onError(Consumer<Throwable> errorHandler) {
    if (this.errorHandler != null) {
      throw new IllegalStateException("you are not allowed to call onError multiple times");
    }

    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public RowProcessorOperation<R> set(String id, Object value) {
    holder.add(id, new ValueQueryParameter(value));
    return this;
  }

  @Override
  public RowProcessorOperation<R> set(String id, Object value, SqlType type) {
    holder.add(id, new ValueQueryParameter(value, type));
    return this;
  }

  @Override
  public RowProcessorOperation<R> set(String id, CompletionStage<?> source) {
    holder.add(id, new FutureQueryParameter(source));
    return this;
  }

  @Override
  public RowProcessorOperation<R> set(String id, CompletionStage<?> source, SqlType type) {
    holder.add(id, new FutureQueryParameter(source, type));
    return this;
  }

  @Override
  public RowProcessorOperation<R> timeout(Duration minTime) {
    return this;
  }

  @Override
  public Submission<R> submit() {
    submission = new ProcessorSubmission<>(this::cancel, errorHandler, sql, publisher, holder, groupSubmission);
    connection.addSubmissionOnQue(submission);
    return submission;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
