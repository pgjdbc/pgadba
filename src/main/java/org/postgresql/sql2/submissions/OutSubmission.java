package org.postgresql.sql2.submissions;

import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.SqlType;
import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.communication.packets.DataRow;
import org.postgresql.sql2.operations.helpers.ParameterHolder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class OutSubmission<T> implements PGSubmission<T> {
  final private Supplier<Boolean> cancel;
  private CompletableFuture<T> publicStage;
  private Consumer<Throwable> errorHandler;
  private String sql;
  private ParameterHolder holder;
  private GroupSubmission groupSubmission;
  private final AtomicBoolean sendConsumed = new AtomicBoolean(false);
  private Function<Result.OutParameterMap, ? extends T> outParameterProcessor;
  private T outParameterValueHolder;
  private Map<String, SqlType> outParameterTypeMap;

  public OutSubmission(Supplier<Boolean> cancel, Consumer<Throwable> errorHandler, String sql, Map<String, SqlType> outParameterTypes,
                       Function<Result.OutParameterMap, ? extends T> processor, GroupSubmission groupSubmission, ParameterHolder holder) {
    this.cancel = cancel;
    this.errorHandler = errorHandler;
    this.sql = sql;
    this.outParameterTypeMap = outParameterTypes;
    this.outParameterProcessor = processor;
    this.groupSubmission = groupSubmission;
    this.holder = holder;
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
    return Types.OUT_PARAMETER;
  }

  @Override
  public void setCollector(Collector collector) {

  }

  @Override
  public Object finish(Object finishObject) {
    if(groupSubmission != null) {
      groupSubmission.addGroupResult(outParameterValueHolder);
    }
    ((CompletableFuture<T>) getCompletionStage())
        .complete(outParameterValueHolder);
    return null;
  }

  @Override
  public void addRow(DataRow row) {
    outParameterValueHolder = outParameterProcessor.apply(row);
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
