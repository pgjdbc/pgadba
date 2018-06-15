package org.postgresql.sql2.submissions;

import jdk.incubator.sql2.TransactionOutcome;
import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.communication.packets.CommandComplete;
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

public class TransactionSubmission implements PGSubmission<TransactionOutcome> {
  final private Supplier<Boolean> cancel;
  private CompletableFuture<TransactionOutcome> publicStage;
  private String sql;
  private final AtomicBoolean sendConsumed = new AtomicBoolean(false);
  private Consumer<Throwable> errorHandler;
  private ParameterHolder holder = new ParameterHolder();

  public TransactionSubmission(Supplier<Boolean> cancel, Consumer<Throwable> errorHandler) {
    this.cancel = cancel;
    this.errorHandler = errorHandler;
  }

  @Override
  public void setSql(String sql) {
    this.sql = sql;
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
    return Types.TRANSACTION;
  }

  @Override
  public void setCollector(Collector collector) {

  }

  @Override
  public Object finish(Object finishObject) {
    CommandComplete.Types type = (CommandComplete.Types)finishObject;
    if(type == CommandComplete.Types.ROLLBACK) {
      ((CompletableFuture<TransactionOutcome>) getCompletionStage())
          .complete(TransactionOutcome.ROLLBACK);
    } else if(type == CommandComplete.Types.COMMIT) {
      ((CompletableFuture<TransactionOutcome>) getCompletionStage())
          .complete(TransactionOutcome.COMMIT);
    } else {
      ((CompletableFuture<TransactionOutcome>) getCompletionStage())
          .complete(TransactionOutcome.UNKNOWN);
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
    return 1;
  }

  @Override
  public List<Long> countResult() {
    return null;
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
  public CompletionStage<TransactionOutcome> getCompletionStage() {
    if (publicStage == null)
      publicStage = new CompletableFuture<>();
    return publicStage;
  }
}
