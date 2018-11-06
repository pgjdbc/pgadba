package org.postgresql.adba.submissions;

import org.postgresql.adba.PgSubmission;
import org.postgresql.adba.communication.packets.DataRow;
import org.postgresql.adba.operations.helpers.ParameterHolder;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class BaseSubmission<T> implements PgSubmission<T> {
  private final Supplier<Boolean> cancel;
  private CompletableFuture<T> publicStage;
  private String sql;
  private final AtomicBoolean sendConsumed = new AtomicBoolean(false);
  private ParameterHolder holder;
  private PgSubmission.Types completionType;

  private Collector collector;
  private Object collectorHolder;
  private Consumer<Throwable> errorHandler;

  private GroupSubmission groupSubmission;

  /**
   * submission that waits for completion from the database server.
   * @param cancel cancel method
   * @param completionType the operation type this submission belongs to
   * @param errorHandler error handler method
   * @param holder holder for parameter values
   * @param groupSubmission group submission this submission is a part of
   * @param sql the query
   */
  public BaseSubmission(Supplier<Boolean> cancel, Types completionType, Consumer<Throwable> errorHandler, ParameterHolder holder,
                        GroupSubmission groupSubmission, String sql) {
    this.cancel = cancel;
    this.completionType = completionType;
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

  public PgSubmission.Types getCompletionType() {
    return completionType;
  }

  /**
   * Sets the collector object.
   * @param collector .supplier().get() is called on this object
   */
  public void setCollector(Collector collector) {
    this.collector = collector;

    collectorHolder = collector.supplier().get();
  }

  /**
   * When all rows are read from the database, finish should be called.
   * @param finishObject not used in this class
   * @return the result of the submission
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
   * Adds another DataRow to the submission.
   * @param row row to add
   */
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

  /**
   * Returns the number of times this query should repeat.
   * @return number of times this query should repeat
   * @throws ExecutionException if one of the parameters is a future that throws when waited on
   * @throws InterruptedException if one of the parameters is a future that throws when waited on
   */
  public int numberOfQueryRepetitions() throws ExecutionException, InterruptedException {
    if (completionType == PgSubmission.Types.ARRAY_COUNT) {
      return holder.numberOfQueryRepetitions();
    } else {
      return 1;
    }
  }

  public Consumer<Throwable> getErrorHandler() {
    return errorHandler;
  }
}
