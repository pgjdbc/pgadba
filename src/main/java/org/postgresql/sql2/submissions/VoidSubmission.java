package org.postgresql.sql2.submissions;

import org.postgresql.sql2.PgSubmission;
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

public class VoidSubmission<T> implements PgSubmission<T> {
  private final Supplier<Boolean> cancel;
  private CompletableFuture<T> publicStage;
  private String sql;
  private final AtomicBoolean sendConsumed = new AtomicBoolean(false);
  private ParameterHolder holder;

  private Collector collector;
  private Object collectorHolder;
  private Consumer<Throwable> errorHandler;

  private GroupSubmission groupSubmission;

  /**
   * Creates the void submission.
   *
   * @param cancel cancel method
   * @param errorHandler error handler method
   * @param holder holder for parameter values
   * @param groupSubmission group submission this submission is a part of
   * @param sql the query
   */
  public VoidSubmission(Supplier<Boolean> cancel, Consumer<Throwable> errorHandler, ParameterHolder holder,
      GroupSubmission groupSubmission, String sql) {
    this.cancel = cancel;
    this.errorHandler = errorHandler;
    this.holder = holder;
    this.groupSubmission = groupSubmission;
    this.sql = sql;
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
    return Types.VOID;
  }

  /**
   * Sets the collector for the rows.
   * @param collector collector object, .supplier().get() gets called on this
   */
  public void setCollector(Collector collector) {
    this.collector = collector;

    collectorHolder = collector.supplier().get();
  }

  /**
   * When all rows in the result set is consumed, this is called.
   * @param finishObject not in use
   * @return the result of the operation
   */
  public Object finish(Object finishObject) {
    Object o = null;
    if (collector != null) {
      o = collector.finisher().apply(collectorHolder);
      if (groupSubmission != null) {
        groupSubmission.addGroupResult(o);
      }
    }
    return o;
  }

  /**
   * Adds another row to the submission.
   * @param row row to add
   */
  public void addRow(DataRow row) {
    try {
      if (collector != null) {
        collector.accumulator().accept(collectorHolder, row);
      }
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
