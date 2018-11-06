package org.postgresql.adba.operations;

import jdk.incubator.sql2.ArrayRowCountOperation;
import jdk.incubator.sql2.LocalOperation;
import jdk.incubator.sql2.MultiOperation;
import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.OperationGroup;
import jdk.incubator.sql2.OutOperation;
import jdk.incubator.sql2.ParameterizedRowCountOperation;
import jdk.incubator.sql2.ParameterizedRowOperation;
import jdk.incubator.sql2.ParameterizedRowPublisherOperation;
import jdk.incubator.sql2.PrimitiveOperation;
import jdk.incubator.sql2.Session.Lifecycle;
import jdk.incubator.sql2.Submission;
import jdk.incubator.sql2.TransactionCompletion;
import jdk.incubator.sql2.TransactionOutcome;
import org.postgresql.adba.PgSession;
import org.postgresql.adba.submissions.GroupSubmission;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;

public class PgOperationGroup<S, T> implements OperationGroup<S, T> {

  static final Collector DEFAULT_COLLECTOR = Collector.of(
      () -> null,
      (a, v) -> {
      },
      (a, b) -> null,
      a -> null);

  private PgSession connection;
  private Logger logger = Logger.getLogger(PgSession.class.getName());
  protected Consumer<Throwable> errorHandler = null;
  private boolean held = true;

  private Collector collector = DEFAULT_COLLECTOR;

  protected GroupSubmission<T> groupSubmission;

  public PgOperationGroup() {

  }

  public PgOperationGroup(PgSession connection) {
    this.connection = connection;
  }

  public void setConnection(PgSession connection) {
    this.connection = connection;
  }

  @Override
  public OperationGroup<S, T> parallel() {
    throw new RuntimeException("not implemented yet");
  }

  @Override
  public OperationGroup<S, T> independent() {
    throw new RuntimeException("not implemented yet");
  }

  @Override
  public OperationGroup<S, T> conditional(CompletionStage<Boolean> condition) {
    throw new RuntimeException("not implemented yet");
  }

  @Override
  public Submission<T> submit() {
    GroupSubmission<T> sub = new GroupSubmission<>(this::cancel, errorHandler);
    sub.setCollector(collector);

    groupSubmission = sub;

    return sub;
  }

  @Override
  public void close() {
    held = false;
    
    connection.submit(groupSubmission);
  }

  @Override
  public OperationGroup<S, T> collect(Collector<S, ?, T> c) {
    collector = c;
    return this;
  }

  @Override
  public PrimitiveOperation<S> catchOperation() {
    if (connection.getSessionLifecycle() != Lifecycle.NEW && connection.getSessionLifecycle() != Lifecycle.ATTACHED) {
      throw new IllegalStateException("session lifecycle in state: " + connection.getSessionLifecycle()
          + " and not open for new work");
    }

    if (!held) {
      throw new IllegalStateException("It's not permitted to add more operations after an OperationGroup has been released");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "CatchOperation created for connection " + this);
    }

    return new PgCatchOperation<>(connection);
  }

  @Override
  public <R extends S> ArrayRowCountOperation<R> arrayRowCountOperation(String sql) {
    if (connection.getSessionLifecycle() != Lifecycle.NEW && connection.getSessionLifecycle() != Lifecycle.ATTACHED) {
      throw new IllegalStateException("session lifecycle in state: " + connection.getSessionLifecycle()
          + " and not open for new work");
    }

    if (!held) {
      throw new IllegalStateException("It's not permitted to add more operations after an OperationGroup has been released");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "ArrayCountOperation created for connection " + this);
    }

    return new PgArrayRowCountOperation<>(connection, sql, groupSubmission);
  }

  @Override
  public <R extends S> ParameterizedRowCountOperation<R> rowCountOperation(String sql) {
    if (connection.getSessionLifecycle() != Lifecycle.NEW && connection.getSessionLifecycle() != Lifecycle.ATTACHED) {
      throw new IllegalStateException("session lifecycle in state: " + connection.getSessionLifecycle()
          + " and not open for new work");
    }

    if (!held) {
      throw new IllegalStateException("It's not permitted to add more operations after an OperationGroup has been released");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "CountOperation created for connection " + this);
    }

    return new PgRowCountOperation<>(connection, sql, groupSubmission);
  }

  @Override
  public Operation<S> operation(String sql) {
    if (connection.getSessionLifecycle() != Lifecycle.NEW && connection.getSessionLifecycle() != Lifecycle.ATTACHED) {
      throw new IllegalStateException("session lifecycle in state: " + connection.getSessionLifecycle()
          + " and not open for new work");
    }

    if (!held) {
      throw new IllegalStateException("It's not permitted to add more operations after an OperationGroup has been released");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "Operation created for connection " + this);
    }

    return new PgOperation<>(connection, sql);
  }

  @Override
  public <R extends S> OutOperation<R> outOperation(String sql) {
    if (connection.getSessionLifecycle() != Lifecycle.NEW && connection.getSessionLifecycle() != Lifecycle.ATTACHED) {
      throw new IllegalStateException("session lifecycle in state: " + connection.getSessionLifecycle()
          + " and not open for new work");
    }

    if (!held) {
      throw new IllegalStateException("It's not permitted to add more operations after an OperationGroup has been released");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "OutOperation created for connection " + this);
    }

    return new PgOutOperation<>(connection, sql, groupSubmission);
  }

  @Override
  public <R extends S> ParameterizedRowOperation<R> rowOperation(String sql) {
    if (connection.getSessionLifecycle() != Lifecycle.NEW && connection.getSessionLifecycle() != Lifecycle.ATTACHED) {
      throw new IllegalStateException("session lifecycle in state: " + connection.getSessionLifecycle()
          + " and not open for new work");
    }

    if (!held) {
      throw new IllegalStateException("It's not permitted to add more operations after an OperationGroup has been released");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "RowOperation created for connection " + this);
    }

    return new PgParameterizedRowOperation<>(connection, sql, groupSubmission);
  }

  @Override
  public <R extends S> ParameterizedRowPublisherOperation<R> rowPublisherOperation(String sql) {
    if (connection.getSessionLifecycle() != Lifecycle.NEW && connection.getSessionLifecycle() != Lifecycle.ATTACHED) {
      throw new IllegalStateException("session lifecycle in state: " + connection.getSessionLifecycle()
          + " and not open for new work");
    }

    if (!held) {
      throw new IllegalStateException("It's not permitted to add more operations after an OperationGroup has been released");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "ParameterizedRowPublisherOperation created for connection " + this);
    }

    return new PgRowPublisherOperation<>(connection, sql, groupSubmission);
  }

  @Override
  public <R extends S> MultiOperation<R> multiOperation(String sql) {
    throw new RuntimeException("not implemented yet");
  }

  @Override
  public Operation<TransactionOutcome> endTransactionOperation(TransactionCompletion trans) {
    if (connection.getSessionLifecycle() != Lifecycle.NEW && connection.getSessionLifecycle() != Lifecycle.ATTACHED) {
      throw new IllegalStateException("session lifecycle in state: " + connection.getSessionLifecycle()
          + " and not open for new work");
    }

    if (!held) {
      throw new IllegalStateException("It's not permitted to add more operations after an OperationGroup has been released");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "EndTransactionOperation created for connection " + this);
    }

    return new PgTransactionOperation(trans, connection);
  }

  @Override
  public <R extends S> LocalOperation<R> localOperation() {
    if (connection.getSessionLifecycle() != Lifecycle.NEW && connection.getSessionLifecycle() != Lifecycle.ATTACHED) {
      throw new IllegalStateException("session lifecycle in state: " + connection.getSessionLifecycle()
          + " and not open for new work");
    }

    if (!held) {
      throw new IllegalStateException("It's not permitted to add more operations after an OperationGroup has been released");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "LocalOperation created for connection " + this);
    }

    return new PgLocalOperation<>(connection, groupSubmission);
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
    throw new RuntimeException("not implemented yet");
  }

  @Override
  public OperationGroup<S, T> onError(Consumer<Throwable> errorHandler) {
    if (this.errorHandler != null) {
      throw new IllegalStateException("you are not allowed to call onError multiple times");
    }

    this.errorHandler = errorHandler;

    return this;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
