package org.postgresql.adba.submissions;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import jdk.incubator.sql2.Result.RowCount;
import org.postgresql.adba.PgSubmission;
import org.postgresql.adba.communication.packets.DataRow;
import org.postgresql.adba.operations.helpers.ParameterHolder;

public class CountSubmission<T> implements PgSubmission<T> {

  private final Supplier<Boolean> cancel;
  private CompletableFuture<T> publicStage;
  private String sql;
  private final AtomicBoolean sendConsumed = new AtomicBoolean(false);
  private Consumer<Throwable> errorHandler;
  private ParameterHolder holder;
  private PgSubmission returningRowSubmission;
  private GroupSubmission groupSubmission;
  private Function<RowCount, ?> processor;

  /**
   * Creates the count submission.
   *
   * @param cancel cancel method
   * @param errorHandler error handler method
   * @param holder holder for parameter values
   * @param returningRowSubmission submission to get the primary key
   * @param sql the query
   * @param groupSubmission group submission this submission is a part of
   * @param processor a function reference that transforms the produced RowCount object to something else, allowed to be null
   */
  public CountSubmission(Supplier<Boolean> cancel, Consumer<Throwable> errorHandler, ParameterHolder holder,
      PgSubmission returningRowSubmission, String sql, GroupSubmission groupSubmission,
      Function<RowCount, ?> processor) {
    this.cancel = cancel;
    this.errorHandler = errorHandler;
    this.holder = holder;
    this.returningRowSubmission = returningRowSubmission;
    this.sql = sql;
    this.groupSubmission = groupSubmission;
    this.processor = processor;
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
    return Types.COUNT;
  }

  @Override
  public void setCollector(Collector collector) {

  }

  @Override
  public Object finish(Object finishObject) {
    if (returningRowSubmission != null) {
      Object endResult = returningRowSubmission.finish(null);
      ((CompletableFuture) returningRowSubmission.getCompletionStage()).complete(endResult);
    }
    if (groupSubmission != null) {
      groupSubmission.addGroupResult(finishObject);
    }

    if (processor != null) {
      finishObject = processor.apply((RowCount) finishObject);
    }
    ((CompletableFuture) getCompletionStage())
        .complete(finishObject);

    return null;
  }

  @Override
  public void addRow(DataRow row) {
    if (returningRowSubmission != null) {
      returningRowSubmission.addRow(row);
    }
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
