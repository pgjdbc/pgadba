package org.postgresql.adba.submissions;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collector;
import org.postgresql.adba.PgSubmission;
import org.postgresql.adba.communication.packets.DataRow;
import org.postgresql.adba.operations.helpers.ParameterHolder;

public class CloseSubmission implements PgSubmission<Void> {
  private final Supplier<Boolean> cancel;
  private CompletableFuture<Void> publicStage;
  private final AtomicBoolean sendConsumed = new AtomicBoolean(false);

  private Collector collector;
  private Object collectorHolder;
  private Consumer<Throwable> errorHandler;

  public CloseSubmission(Supplier<Boolean> cancel, Consumer<Throwable> errorHandler) {
    this.cancel = cancel;
    this.errorHandler = errorHandler;
  }

  @Override
  public CompletionStage<Boolean> cancel() {
    return new CompletableFuture<Boolean>().completeAsync(cancel);
  }

  @Override
  public CompletionStage<Void> getCompletionStage() {
    if (publicStage == null) {
      publicStage = new CompletableFuture<>();
    }

    return publicStage;
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
    return BaseSubmission.Types.CLOSE;
  }

  @Override
  public void setCollector(Collector collector) {
    this.collector = collector;

    collectorHolder = collector.supplier().get();
  }

  @Override
  public Object finish(Object socketChannel) {
    return null;
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
}
