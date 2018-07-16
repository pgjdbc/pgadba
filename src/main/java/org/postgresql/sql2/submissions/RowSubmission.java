package org.postgresql.sql2.submissions;

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

public class RowSubmission<T> implements org.postgresql.sql2.PGSubmission<T> {
  final private Supplier<Boolean> cancel;
  private CompletableFuture<T> publicStage;
  private String sql;
  private final AtomicBoolean sendConsumed = new AtomicBoolean(false);
  private ParameterHolder holder;

  private Collector collector;
  private Object collectorHolder;
  private Consumer<Throwable> errorHandler;

  private GroupSubmission groupSubmission;

  public RowSubmission(Supplier<Boolean> cancel, Consumer<Throwable> errorHandler, ParameterHolder holder,
                       GroupSubmission groupSubmission, String sql) {
    this.cancel = cancel;
    this.errorHandler = errorHandler;
    this.holder = holder;
    this.groupSubmission = groupSubmission;
    this.sql = sql;

    if(groupSubmission != null) {
      groupSubmission.stackFuture((CompletableFuture<T>)getCompletionStage());
    }
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

  public String getSql() {
    return sql;
  }

  public AtomicBoolean getSendConsumed() {
    return sendConsumed;
  }

  public ParameterHolder getHolder() {
    return holder;
  }

  public Types getCompletionType() {
    return Types.ROW;
  }

  public void setCollector(Collector collector) {
    this.collector = collector;

    collectorHolder = collector.supplier().get();
  }

  public Object finish(Object finishObject) {
    T o = null;
    if(collector != null) {
      o = (T)collector.finisher().apply(collectorHolder);
      if(groupSubmission != null) {
        groupSubmission.addGroupResult(o);
      }
    }
    ((CompletableFuture<T>) getCompletionStage())
        .complete(o);
    return o;
  }

  public void addRow(DataRow row) {
    try {
      collector.accumulator().accept(collectorHolder, row);
    } catch (Throwable e) {
      publicStage.completeExceptionally(e);
    }
  }

  public List<Integer> getParamTypes() throws ExecutionException, InterruptedException {
    return holder.getParamTypes();
  }

  public int numberOfQueryRepetitions() {
    return 1;
  }

  public Consumer<Throwable> getErrorHandler() {
    return errorHandler;
  }
}
