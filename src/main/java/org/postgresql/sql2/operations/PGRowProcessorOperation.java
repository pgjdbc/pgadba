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

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

public class PGRowProcessorOperation<R> implements RowProcessorOperation<R> {
  private PGConnection connection;
  private String sql;
  private ParameterHolder holder;
  private Flow.Processor<Result.Row, ? extends R> processor;
  private Consumer<Throwable> errorHandler;

  public PGRowProcessorOperation(PGConnection connection, String sql) {
    this.connection = connection;
    this.sql = sql;
    this.holder = new ParameterHolder();
  }

  @Override
  public RowProcessorOperation<R> rowProcessor(Flow.Processor<Result.Row, ? extends R> processor) {
    this.processor = processor;
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
    PGSubmission<R> submission = new PGSubmission<>(this::cancel, PGSubmission.Types.PROCESSOR, errorHandler);
    submission.setConnectionSubmission(false);
    submission.setSql(sql);
    submission.setHolder(holder);
    submission.setProcessor(processor);
    connection.addSubmissionOnQue(submission);
    return submission;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
