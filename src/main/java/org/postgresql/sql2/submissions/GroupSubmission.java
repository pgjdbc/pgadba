package org.postgresql.sql2.submissions;

import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.communication.packets.DataRow;
import org.postgresql.sql2.operations.helpers.ParameterHolder;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class GroupSubmission<T> implements PGSubmission<T> {
  final private Supplier<Boolean> cancel;
  private CompletableFuture<T> publicStage;
  private CompletionStage<T> membersTail;
  private Consumer<Throwable> errorHandler;
  private final AtomicBoolean sendConsumed = new AtomicBoolean(false);
  private Collector collector;
  private Object collectorHolder;

  public GroupSubmission(Supplier<Boolean> cancel, Consumer<Throwable> errorHandler) {
    this.cancel = cancel;
    this.errorHandler = errorHandler;
  }

  @Override
  public String getSql() {
    return null;
  }

  @Override
  public AtomicBoolean getSendConsumed() {
    return sendConsumed;
  }

  @Override
  public ParameterHolder getHolder() {
    return null;
  }

  @Override
  public Types getCompletionType() {
    return Types.GROUP;
  }

  @Override
  public void setCollector(Collector collector) {
    this.collector = collector;

    collectorHolder = collector.supplier().get();
  }

  @Override
  public Object finish(Object finishObject) {
    try {
      Object o = null;
      if (collector != null) {
        o = collector.finisher().apply(collectorHolder);
      }
      ((CompletableFuture<T>)getCompletionStage()).complete((T)o);
    } catch (Throwable t) {
      ((CompletableFuture<T>)getCompletionStage()).completeExceptionally(t);
    }
    return null;
  }

  @Override
  public void addRow(DataRow row) {

  }

  public void addGroupResult(Object result) {
    try {
      collector.accumulator().accept(collectorHolder, result);
    } catch (Throwable e) {
      publicStage.completeExceptionally(e);
    }
  }

  @Override
  public List<Integer> getParamTypes() throws ExecutionException, InterruptedException {
    return null;
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
    return new CompletableFuture<Boolean>().completeAsync(cancel);
  }

  @Override
  public CompletionStage<T> getCompletionStage() {
    if (publicStage == null)
      publicStage = new CompletableFuture<>();
    return publicStage;
  }

  public void stackFuture(CompletableFuture<T> completionStage) {
    if (membersTail == null)
      membersTail = getCompletionStage();

    membersTail.exceptionally(e -> {
      completionStage.completeExceptionally(e);
      return null;
    });
    membersTail = completionStage;
  }
}
