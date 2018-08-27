package org.postgresql.sql2.submissions;

import jdk.incubator.sql2.Result;
import org.postgresql.sql2.PgSubmission;
import org.postgresql.sql2.communication.packets.DataRow;
import org.postgresql.sql2.operations.helpers.ParameterHolder;
import org.postgresql.sql2.util.PgCount;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ArrayCountSubmission<T> implements PgSubmission<T> {
  private static final Collector<Result.RowCount, List<Result.RowCount>, List<Result.RowCount>> defaultCollector = Collector.of(
      () -> new ArrayList<>(),
      (a, r) -> a.add(r),
      (l, r) -> null,
      a -> a);
  private final Supplier<Boolean> cancel;
  private CompletableFuture<T> publicStage;
  private String sql;
  private final AtomicBoolean sendConsumed = new AtomicBoolean(false);
  private ParameterHolder holder;

  private Collector collector = defaultCollector;
  private Object collectorHolder = defaultCollector.supplier().get();
  private Consumer<Throwable> errorHandler;
  private GroupSubmission groupSubmission;

  private int numResults = 0;
  private int numBindExecuteSent = 0;

  /**
   * Creates a submission object that waits for completion.
   * @param cancel cancel method
   * @param errorHandler error handler method
   * @param holder holder for parameter values
   * @param sql the query
   * @param groupSubmission group submission this submission is a part of
   */
  public ArrayCountSubmission(Supplier<Boolean> cancel, Consumer<Throwable> errorHandler, ParameterHolder holder, String sql,
      GroupSubmission groupSubmission) {
    this.cancel = cancel;
    this.errorHandler = errorHandler;
    this.holder = holder;
    this.sql = sql;
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
    return Types.ARRAY_COUNT;
  }

  @Override
  public void setCollector(Collector collector) {
    this.collector = collector;

    collectorHolder = collector.supplier().get();
  }

  @Override
  public Object finish(Object finishObject) {
    collector.accumulator().accept(collectorHolder, new PgCount(Long.valueOf((Integer) finishObject)));
    numResults++;
    try {
      if (numResults == numberOfQueryRepetitions()) {
        Object endObject = collector.finisher().apply(collectorHolder);
        ((CompletableFuture) getCompletionStage())
            .complete(endObject);
        if (groupSubmission != null) {
          groupSubmission.addGroupResult(endObject);
        }
        return true;
      }
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public void addRow(DataRow row) {
    try {
      collector.accumulator().accept(collectorHolder, row);
    } catch (Throwable e) {
      publicStage.completeExceptionally(e);
    }
  }

  @Override
  public List<Integer> getParamTypes() throws ExecutionException, InterruptedException {
    return holder.getParamTypes();
  }

  @Override
  public int numberOfQueryRepetitions() throws ExecutionException, InterruptedException {
    return holder.numberOfQueryRepetitions();
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
    if (publicStage == null) {
      publicStage = new CompletableFuture<>();
    }

    return publicStage;
  }

  public boolean hasMoreToExecute() throws ExecutionException, InterruptedException {
    numBindExecuteSent++;
    return numBindExecuteSent != numberOfQueryRepetitions();
  }
}
