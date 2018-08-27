package org.postgresql.sql2.submissions;

import jdk.incubator.sql2.Result;
import org.postgresql.sql2.PgSubmission;
import org.postgresql.sql2.communication.packets.DataRow;
import org.postgresql.sql2.operations.helpers.ParameterHolder;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ProcessorSubmission<T> implements PgSubmission<T> {
  private final Supplier<Boolean> cancel;
  private CompletableFuture<T> publicStage;
  private Consumer<Throwable> errorHandler;
  private String sql;
  private ParameterHolder holder;
  private SubmissionPublisher<Result.RowColumn> publisher;
  private GroupSubmission groupSubmission;
  private final AtomicBoolean sendConsumed = new AtomicBoolean(false);

  /**
   * A submission for a Processor operation.
   *
   * @param cancel cancel method
   * @param errorHandler error handler method
   * @param sql the query
   * @param publisher publisher that consumes rows
   * @param holder holder for parameter values
   * @param groupSubmission group submission this submission is a part of
   */
  public ProcessorSubmission(Supplier<Boolean> cancel, Consumer<Throwable> errorHandler, String sql,
      SubmissionPublisher<Result.RowColumn> publisher, ParameterHolder holder, GroupSubmission groupSubmission) {
    this.cancel = cancel;
    this.errorHandler = errorHandler;
    this.sql = sql;
    this.publisher = publisher;
    this.holder = holder;
    this.groupSubmission = groupSubmission;
  }

  @Override
  public String getSql() {
    return sql;
  }

  @Override
  public AtomicBoolean getSendConsumed() {
    return sendConsumed;
  }

  @Override
  public ParameterHolder getHolder() {
    return holder;
  }

  @Override
  public Types getCompletionType() {
    return PgSubmission.Types.PROCESSOR;
  }

  @Override
  public void setCollector(Collector collector) {

  }

  @Override
  public Object finish(Object finishObject) {
    publisher.close();
    return null;
  }

  @Override
  public void addRow(DataRow row) {
    publisher.offer(row, (subscriber, rowItem) -> {
      subscriber.onError(new IllegalStateException("failed to offer item to subscriber"));
      return false;
    });
  }

  @Override
  public List<Integer> getParamTypes() throws ExecutionException, InterruptedException {
    return holder.getParamTypes();
  }

  @Override
  public int numberOfQueryRepetitions() throws ExecutionException, InterruptedException {
    return 1;
  }

  @Override
  public Consumer<Throwable> getErrorHandler() {
    return errorHandler;
  }

  @Override
  public CompletionStage<Boolean> cancel() {
    return null;
  }

  @Override
  public CompletionStage<T> getCompletionStage() {
    if (publicStage == null) {
      publicStage = new CompletableFuture<>();
    }

    return publicStage;
  }
}
