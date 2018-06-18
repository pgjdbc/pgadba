package org.postgresql.sql2.operations;

import jdk.incubator.sql2.ArrayCountOperation;
import jdk.incubator.sql2.LocalOperation;
import jdk.incubator.sql2.MultiOperation;
import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.OperationGroup;
import jdk.incubator.sql2.OutOperation;
import jdk.incubator.sql2.ParameterizedCountOperation;
import jdk.incubator.sql2.ParameterizedRowOperation;
import jdk.incubator.sql2.PrimitiveOperation;
import jdk.incubator.sql2.RowProcessorOperation;
import jdk.incubator.sql2.Submission;
import jdk.incubator.sql2.Transaction;
import jdk.incubator.sql2.TransactionOutcome;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.submissions.GroupSubmission;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;

public class PGOperationGroup<S, T> implements OperationGroup<S, T> {

  static final Collector DEFAULT_COLLECTOR = Collector.of(
      () -> null,
      (a, v) -> {
      },
      (a, b) -> null,
      a -> null);

  private PGConnection connection;
  private Logger logger = Logger.getLogger(PGConnection.class.getName());
  protected Consumer<Throwable> errorHandler = null;
  private boolean held = true;

  private Collector collector = DEFAULT_COLLECTOR;

  private GroupSubmission<T> groupSubmission;

  public PGOperationGroup() {

  }

  public PGOperationGroup(PGConnection connection) {
    this.connection = connection;
  }

  public void setConnection(PGConnection connection) {
    this.connection = connection;
  }

  @Override
  public OperationGroup<S, T> parallel() {
    //this is a no-op since the postgresql wire format doesn't allow parallel queries
    return this;
  }

  @Override
  public OperationGroup<S, T> independent() {
    return null;
  }

  @Override
  public OperationGroup<S, T> conditional(CompletionStage<Boolean> condition) {
    return null;
  }

  @Override
  public Submission<T> submitHoldingForMoreMembers() {
    GroupSubmission<T> sub = new GroupSubmission<>(this::cancel, errorHandler);
    sub.setCollector(collector);

    groupSubmission = sub;

    return sub;
  }

  @Override
  public OperationGroup<S, T> releaseProhibitingMoreMembers() {
    held = false;
    connection.addSubmissionOnQue(groupSubmission);
    return this;
  }

  @Override
  public OperationGroup<S, T> collect(Collector<S, ?, T> c) {
    collector = c;
    return this;
  }

  @Override
  public PrimitiveOperation<S> catchOperation() {
    if (!connection.getConnectionLifecycle().isOpen() || !connection.getConnectionLifecycle().isActive()) {
      throw new IllegalStateException("connection lifecycle in state: " + connection.getConnectionLifecycle() + " and not open for new work");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "CatchOperation created for connection " + this);
    }

    return new PGCatchOperation<>(connection);
  }

  @Override
  public <R extends S> ArrayCountOperation<R> arrayCountOperation(String sql) {
    if (!connection.getConnectionLifecycle().isOpen() || !connection.getConnectionLifecycle().isActive()) {
      throw new IllegalStateException("connection lifecycle in state: " + connection.getConnectionLifecycle() + " and not open for new work");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "ArrayCountOperation created for connection " + this);
    }

    return new PGArrayCountOperation<>(connection, sql);
  }

  @Override
  public <R extends S> ParameterizedCountOperation<R> countOperation(String sql) {
    if (!connection.getConnectionLifecycle().isOpen() || !connection.getConnectionLifecycle().isActive()) {
      throw new IllegalStateException("connection lifecycle in state: " + connection.getConnectionLifecycle() + " and not open for new work");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "CountOperation created for connection " + this);
    }

    return new PGCountOperation<>(connection, sql, groupSubmission);
  }

  @Override
  public Operation<S> operation(String sql) {
    if (!connection.getConnectionLifecycle().isOpen() || !connection.getConnectionLifecycle().isActive()) {
      throw new IllegalStateException("connection lifecycle in state: " + connection.getConnectionLifecycle() + " and not open for new work");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "Operation created for connection " + this);
    }

    return new PGOperation<>(connection, sql);
  }

  @Override
  public <R extends S> OutOperation<R> outOperation(String sql) {
    if (!connection.getConnectionLifecycle().isOpen() || !connection.getConnectionLifecycle().isActive()) {
      throw new IllegalStateException("connection lifecycle in state: " + connection.getConnectionLifecycle() + " and not open for new work");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "OutOperation created for connection " + this);
    }

    return new PGOutOperation<>(connection, sql, groupSubmission);
  }

  @Override
  public <R extends S> ParameterizedRowOperation<R> rowOperation(String sql) {
    if (!connection.getConnectionLifecycle().isOpen() || !connection.getConnectionLifecycle().isActive()) {
      throw new IllegalStateException("connection lifecycle in state: " + connection.getConnectionLifecycle() + " and not open for new work");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "RowOperation created for connection " + this);
    }

    return new PGParameterizedRowOperation<>(connection, sql, groupSubmission);
  }

  @Override
  public <R extends S> RowProcessorOperation<R> rowProcessorOperation(String sql) {
    if (!connection.getConnectionLifecycle().isOpen() || !connection.getConnectionLifecycle().isActive()) {
      throw new IllegalStateException("connection lifecycle in state: " + connection.getConnectionLifecycle() + " and not open for new work");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "RowProcessorOperation created for connection " + this);
    }

    return new PGRowProcessorOperation<>(connection, sql, groupSubmission);
  }

  @Override
  public <R extends S> MultiOperation<R> multiOperation(String sql) {
    return null;
  }

  @Override
  public Operation<TransactionOutcome> endTransactionOperation(Transaction trans) {
    if (!connection.getConnectionLifecycle().isOpen() || !connection.getConnectionLifecycle().isActive()) {
      throw new IllegalStateException("connection lifecycle in state: " + connection.getConnectionLifecycle() + " and not open for new work");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "EndTransactionOperation created for connection " + this);
    }

    return new PGTransactionOperation(trans, connection);
  }

  @Override
  public <R extends S> LocalOperation<R> localOperation() {
    if (!connection.getConnectionLifecycle().isOpen() || !connection.getConnectionLifecycle().isActive()) {
      throw new IllegalStateException("connection lifecycle in state: " + connection.getConnectionLifecycle() + " and not open for new work");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "LocalOperation created for connection " + this);
    }

    return new PGLocalOperation<>(connection, groupSubmission);
  }

  @Override
  public <R extends S> Flow.Processor<Operation<R>, Submission<R>> operationProcessor() {
    return null;
  }

  @Override
  public OperationGroup<S, T> logger(Logger logger) {
    if (logger == null) {
      return this;
    }
    this.logger = logger;

    if (logger.isLoggable(Level.INFO)) {
      logger.log(Level.INFO, "logger for connection " + this + " updated to " + logger);
    }

    return this;
  }

  @Override
  public OperationGroup<S, T> timeout(Duration minTime) {
    return null;
  }

  @Override
  public OperationGroup<S, T> onError(Consumer<Throwable> errorHandler) {
    if (this.errorHandler != null) {
      throw new IllegalStateException("you are not allowed to call onError multiple times");
    }

    this.errorHandler = errorHandler;

    return this;
  }

  @Override
  public Submission<T> submit() {
    return null;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
