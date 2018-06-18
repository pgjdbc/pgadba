package org.postgresql.sql2.submissions;

import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.communication.packets.DataRow;
import org.postgresql.sql2.operations.helpers.ParameterHolder;
import org.postgresql.sql2.util.PGCount;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class CountSubmission<T> implements PGSubmission<T> {
  final private Supplier<Boolean> cancel;
  private CompletableFuture<T> publicStage;
  private String sql;
  private final AtomicBoolean sendConsumed = new AtomicBoolean(false);
  private Consumer<Throwable> errorHandler;
  private ParameterHolder holder;
  private PGSubmission returningRowSubmission;
  private GroupSubmission groupSubmission;

  public CountSubmission(Supplier<Boolean> cancel, Consumer<Throwable> errorHandler, ParameterHolder holder,
                         PGSubmission returningRowSubmission, String sql, GroupSubmission groupSubmission) {
    this.cancel = cancel;
    this.errorHandler = errorHandler;
    this.holder = holder;
    this.returningRowSubmission = returningRowSubmission;
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
    return Types.COUNT;
  }

  @Override
  public void setCollector(Collector collector) {

  }

  @Override
  public Object finish(Object finishObject) {
    ((CompletableFuture<PGCount>) getCompletionStage())
        .complete((PGCount)finishObject);

    if(returningRowSubmission != null) {
      Object endResult = returningRowSubmission.finish(null);
      returningRowSubmission.getCompletionStage().toCompletableFuture().complete(endResult);
    }
    if(groupSubmission != null) {
      groupSubmission.addGroupResult(finishObject);
    }
    return null;
  }

  @Override
  public void addRow(DataRow row) {
    if(returningRowSubmission != null) {
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
    if (publicStage == null)
      publicStage = new CompletableFuture<>();
    return publicStage;
  }
}
