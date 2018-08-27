package org.postgresql.sql2.submissions;

import org.postgresql.sql2.PgSubmission;
import org.postgresql.sql2.communication.packets.DataRow;
import org.postgresql.sql2.operations.helpers.ParameterHolder;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class LocalSubmission<T> implements PgSubmission<T> {
  private final Supplier<Boolean> cancel;
  private CompletableFuture<T> publicStage;
  private Consumer<Throwable> errorHandler;
  private Callable<T> localAction;
  private GroupSubmission groupSubmission;

  /**
   * The submission for a local operation.
   * @param cancel cancel method
   * @param errorHandler error handler method
   * @param localAction the method that should be called
   * @param groupSubmission group submission this submission is a part of
   */
  public LocalSubmission(Supplier<Boolean> cancel, Consumer<Throwable> errorHandler, Callable<T> localAction,
      GroupSubmission groupSubmission) {
    this.cancel = cancel;
    this.errorHandler = errorHandler;
    this.localAction = localAction;
    this.groupSubmission = groupSubmission;
  }

  @Override
  public String getSql() {
    return null;
  }

  @Override
  public AtomicBoolean getSendConsumed() {
    return null;
  }

  @Override
  public ParameterHolder getHolder() {
    return null;
  }

  @Override
  public Types getCompletionType() {
    return Types.LOCAL;
  }

  @Override
  public void setCollector(Collector collector) {

  }

  @Override
  public Object finish(Object finishObject) {
    try {
      T localResult = localAction.call();
      if (groupSubmission != null) {
        groupSubmission.addGroupResult(localResult);
      }
      getCompletionStage().toCompletableFuture().complete(localResult);
    } catch (Exception e) {
      getCompletionStage().toCompletableFuture().completeExceptionally(e);
    }
    return null;
  }

  @Override
  public void addRow(DataRow row) {

  }

  @Override
  public List<Integer> getParamTypes() throws ExecutionException, InterruptedException {
    return null;
  }

  @Override
  public int numberOfQueryRepetitions() throws ExecutionException, InterruptedException {
    return 0;
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
