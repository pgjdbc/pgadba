/*
 * Copyright (c) 2018, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */
package org.postgresql.sql2;


import jdk.incubator.sql2.ArrayCountOperation;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.ConnectionProperty;
import jdk.incubator.sql2.CountOperation;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DynamicMultiOperation;
import jdk.incubator.sql2.LocalOperation;
import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.OperationGroup;
import jdk.incubator.sql2.OutOperation;
import jdk.incubator.sql2.ParameterizedCountOperation;
import jdk.incubator.sql2.ParameterizedRowOperation;
import jdk.incubator.sql2.RowProcessorOperation;
import jdk.incubator.sql2.ShardingKey;
import jdk.incubator.sql2.SqlException;
import jdk.incubator.sql2.SqlSkippedException;
import jdk.incubator.sql2.StaticMultiOperation;
import jdk.incubator.sql2.Submission;
import jdk.incubator.sql2.Transaction;
import jdk.incubator.sql2.TransactionOutcome;
import org.postgresql.sql2.communication.FEFrame;
import org.postgresql.sql2.communication.ProtocolV3;
import org.postgresql.sql2.operations.PGConnectOperation;
import org.postgresql.sql2.operations.PGCloseOperation;
import org.postgresql.sql2.operations.PGCountOperation;
import org.postgresql.sql2.operations.PGParameterizedRowOperation;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collector;

public class PGConnection implements Connection {
  protected static final CompletionStage<Object> ROOT = CompletableFuture.completedFuture(null);

  static final Collector DEFAULT_COLLECTOR = Collector.of(
      () -> null,
      (a, v) -> {},
      (a, b) -> null,
      a -> null);

  private Executor executor;
  private Map<ConnectionProperty, Object> properties;

  private boolean heldForMoreMember;
  private ProtocolV3 protocol;

  private Object accumulator;
  private Collector collector = DEFAULT_COLLECTOR;
  protected Consumer<Throwable> errorHandler = null;

  /**
   * completed when this OperationGroup is no longer held. Completion of this
   * OperationGroup depends on held.
   */
  private final CompletableFuture<Object> held = new CompletableFuture<>();;

  /**
   * predecessor of all member Operations and the OperationGroup itself
   */
  private final CompletableFuture head = new CompletableFuture();;

  /**
   * The last CompletionStage of any submitted member Operation. Mutable until
   * not isHeld().
   */
  private CompletionStage<Object> memberTail = head;



  public PGConnection(Executor executor, Map<ConnectionProperty, Object> properties) {
    this.executor = executor;
    this.properties = properties;
    this.protocol = new ProtocolV3(properties);
  }

  /**
   * Returns an {@link Operation} that connects this {@link Connection} to a
   * server. If the Operation completes successfully and the lifecycle is
   * {@link Lifecycle#NEW} -&gt; {@link Lifecycle#OPEN}. If lifecycle is
   * {@link Lifecycle#NEW_INACTIVE} -&gt; {@link Lifecycle#INACTIVE}. If the
   * {@link Operation} completes exceptionally the lifecycle -&gt;
   * {@link Lifecycle#CLOSED}. The lifecycle must be {@link Lifecycle#NEW} or
   * {@link Lifecycle#NEW_INACTIVE} when the {@link Operation} is executed.
   * Otherwise the {@link Operation} will complete exceptionally with
   * {@link SqlException}.
   *
   * Note: It is highly recommended to use the {@link Connection#connect()} convenience
   * method or to use {@link DataSource#getConnection} which itself calls
   * {@link Connection#connect()}. Unless there is a specific need, do not call this method
   * directly.
   *
   * This method exists partially to clearly explain that while creating a
   * {@link Connection} is non-blocking, the act of connecting to the server may
   * block and so is executed asynchronously. We could write a bunch of text
   * saying this but defining this method is more explicit. Given the
   * {@link Connection#connect()} convenience methods there's probably not much reason to
   * use this method, but on the other hand, who knows, so here it is.
   *
   * @return an {@link Operation} that connects this {@link Connection} to a
   * server.
   * @throws IllegalStateException if this {@link Connection} is in a lifecycle
   * state other than {@link Lifecycle#NEW}.
   */
  @Override
  public Operation<Void> connectOperation() {
    return new PGConnectOperation((CompletionStage) memberTail, this);
  }

  /**
   * Returns an {@link Operation} that verifies that the resources are available
   * and operational. Successful completion of that {@link Operation} implies
   * that at some point between the beginning and end of the {@link Operation}
   * the Connection was working properly to the extent specified by {@code depth}.
   * There is no guarantee that the {@link Connection} is still working after
   * completion.
   *
   * @param depth how completely to check that resources are available and
   * operational. Not {@code null}.
   * @return an {@link Operation} that will validate this {@link Connection}
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public Operation<Void> validationOperation(Connection.Validation depth) {
    return null;
  }

  /**
   * Create an {@link Operation} to close this {@link Connection}. When the
   * {@link Operation} is executed, if this {@link Connection} is open -&gt;
   * {@link Lifecycle#CLOSING}. If this {@link Connection} is closed executing
   * the returned {@link Operation} is a noop. When the queue is empty and all
   * resources released -&gt; {@link Lifecycle#CLOSED}.
   *
   * A close {@link Operation} is never skipped. Even when the
   * {@link Connection} is dependent, the default, and an {@link Operation}
   * completes exceptionally, a close {@link Operation} is still executed. If
   * the {@link Connection} is parallel, a close {@link Operation} is not
   * executed so long as there are other {@link Operation}s or the
   * {@link Connection} is held; for more {@link Operation}s.
   *
   * Note: It is highly recommended to use try with resources or the
   * {@link Connection#close()} convenience method. Unless there is a specific need, do not
   * call this method directly.
   *
   * @return an {@link Operation} that will close this {@link Connection}.
   * @throws IllegalStateException if the Connection is not active
   */
  @Override
  public Operation<Void> closeOperation() {
    return new PGCloseOperation();
  }

  /**
   * Create a new {@link OperationGroup} for this {@link Connection}.
   *
   * @param <S> the result type of the member {@link Operation}s of the returned
   * {@link OperationGroup}
   * @param <T> the result type of the collected results of the member
   * {@link Operation}s
   * @return a new {@link OperationGroup}.
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public <S, T> OperationGroup<S, T> operationGroup() {
    return null;
  }

  /**
   * Returns a new {@link Transaction} that can be used as an argument to a
   * commit Operation.
   *
   * It is most likely an error to call this within an error handler, or any
   * handler as it is very likely that when the handler is executed the next
   * submitted endTransaction {@link Operation} will have been created with a different
   * Transaction.
   *
   * @return a new {@link Transaction}. Not retained.
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public Transaction transaction() {
    return null;
  }

  /**
   * Register a listener that will be called whenever there is a change in the
   * lifecycle of this {@link Connection}.
   *
   * @param listener Can be {@code null}.
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public Connection registerLifecycleListener(Connection.ConnectionLifecycleListener listener) {
    return null;
  }

  /**
   * Removes a listener that was registered by calling
   * registerLifecycleListener.Sometime after this method is called the listener
   * will stop receiving lifecycle events. If the listener is not registered,
   * this is a no-op.
   *
   * @param listener Not {@code null}.
   * @return this Connection
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public Connection deregisterLifecycleListener(ConnectionLifecycleListener listener) {
    return null;
  }

  /**
   * Return the current lifecycle of this {@link Connection}.
   *
   * @return the current lifecycle of this {@link Connection}.
   */
  @Override
  public Lifecycle getConnectionLifecycle() {
    return null;
  }

  /**
   * Terminate this {@link Connection}. If lifecycle is
   * {@link Lifecycle#NEW}, {@link Lifecycle#OPEN}, {@link Lifecycle#INACTIVE}
   * or {@link Lifecycle#CLOSING} -&gt; {@link Lifecycle#ABORTING} If lifecycle
   * is {@link Lifecycle#ABORTING} or {@link Lifecycle#CLOSED} this is a noop.
   * If an {@link Operation} is currently executing, terminate it immediately.
   * Remove all remaining {@link Operation}s from the queue. {@link Operation}s
   * are not skipped. They are just removed from the queue.
   *
   * @return this {@link Connection}
   */
  @Override
  public Connection abort() {
    return null;
  }

  /**
   * Return the set of properties configured on this {@link Connection}
   * excepting any sensitive properties. Neither the key nor the value for
   * sensitive properties are included in the result. Properties (other than
   * sensitive properties) that have default values are included even when not
   * explicitly set. Properties that have no default value and are not set
   * explicitly are not included.
   *
   * @return a {@link Map} of property, value. Not modifiable. May be retained.
   * Not {@code null}.
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public Map<ConnectionProperty, Object> getProperties() {
    return null;
  }

  /**
   *
   * @return a {@link ShardingKey.Builder} for this {@link Connection}
   */
  @Override
  public ShardingKey.Builder shardingKeyBuilder() {
    return null;
  }

  /**
   * Make this {@link Connection} ready for use. A newly created
   * {@link Connection} is active. Calling this method on a {@link Connection}
   * that is active is a no-op. If the lifecycle is {@link Lifecycle#INACTIVE}
   * -&gt; {@link Lifecycle#OPEN}. If the lifecycle is
   * {@link Lifecycle#NEW_INACTIVE} -&gt; {@link Lifecycle#NEW}.
   *
   * @return this {@link Connection}
   * @throws IllegalStateException if this {@link Connection} is closed.
   */
  @Override
  public Connection activate() {
    return null;
  }

  /**
   * Makes this {@link Connection} inactive. After a call to this method
   * previously submitted Operations will be executed normally. If the lifecycle
   * is {@link Lifecycle#NEW} -&gt; {@link Lifecycle#NEW_INACTIVE}. if the
   * lifecycle is {@link Lifecycle#OPEN} -&gt; {@link Lifecycle#INACTIVE}. If
   * the lifecycle is {@link Lifecycle#INACTIVE} or
   * {@link Lifecycle#NEW_INACTIVE} this method is a no-op. After calling this
   * method calling any method other than {@link Connection#deactivate}, {@link Connection#activate},
   * {@link Connection#abort}, or {@link Connection#getConnectionLifecycle} or submitting any member
   * {@link Operation} will throw {@link IllegalStateException}. Local
   * {@link Connection} state not created by {@link Connection.Builder} may not
   * be preserved.
   *
   * Any implementation of a {@link Connection} pool is by default required to
   * call {@code deactivate} when putting a {@link Connection} into a pool. The
   * implementation is required to call {@code activate} when removing a
   * {@link Connection} from a pool so the {@link Connection} can be used. An
   * implementation of a {@link Connection} pool may have an optional mode where
   * it does not call {@code deactivate}/{@code activate} as required above. The
   * behavior of the pool and {@link Connection}s cached in the pool in such a
   * mode is entirely implementation dependent.
   *
   * @return this {@link Connection}
   * @throws IllegalStateException if this {@link Connection} is closed
   */
  @Override
  public Connection deactivate() {
    return null;
  }

  /**
   * Mark this {@link OperationGroup} as parallel. If this method is not called
   * the {@link OperationGroup} is sequential. If an {@link OperationGroup} is
   * parallel, member {@link Operation}s may be executed in any order including
   * in parallel. If an {@link OperationGroup} is sequential, the default,
   * member {@link Operation}s are executed strictly in the order they are
   * submitted.
   *
   * Note: There is no covariant override of this method in {@link Connection}
   * as there is only a small likelihood of needing it.
   *
   * @return this {@link OperationGroup}
   * @throws IllegalStateException if this method has been submitted or any
   * member {@link Operation}s have been created.
   */
  @Override
  public OperationGroup<Object, Object> parallel() {
    return null;
  }

  /**
   * Mark this {@link OperationGroup} as independent. If this method is not
   * called the {@link OperationGroup} is dependent, the default. If an
   * {@link OperationGroup} is independent then failure of one member
   * {@link Operation} does not affect the execution of other member
   * {@link Operation}s. If an {@link OperationGroup} is dependent then failure
   * of one member {@link Operation} will cause all member {@link Operation}s
   * remaining in the queue to be completed exceptionally with a
   * {@link SqlSkippedException} with the cause set to the original exception.
   *
   * Note: There is no covariant override of this method in {@link Connection}
   * as there is only a small likelihood of needing it.
   *
   * @return this {@link OperationGroup}
   * @throws IllegalStateException if this {@link OperationGroup} has been
   * submitted or any member {@link Operation}s have been created
   */
  @Override
  public OperationGroup<Object, Object> independent() {
    return null;
  }

  /**
   * Define a condition that determines whether the member {@link Operation}s of
   * this {@link OperationGroup} are executed or not. If and when this
   * {@link OperationGroup} is executed then if the condition argument is
   * completed with {@link Boolean#TRUE} the member {@link Operation}s are
   * executed. If {@link Boolean#FALSE} or if it is completed exceptionally the
   * member {@link Operation}s are not executed but are removed from the queue.
   * After all member {@link Operation}s have been removed from the queue this
   * {@link OperationGroup} is completed with {@code null}.
   *
   * Note: There is no covariant override of this method in Connection as there
   * is only a small likelihood of needing it.
   *
   * ISSUE: Should the member Operations be skipped or otherwise completed
   * exceptionally?
   *
   * @param condition a {@link CompletionStage} the value of which determines whether
   * this {@link OperationGroup} is executed or not
   * @return this OperationGroup
   * @throws IllegalStateException if this {@link OperationGroup} has been
   * submitted or any member {@link Operation}s have been created
   */
  @Override
  public OperationGroup<Object, Object> conditional(CompletionStage<Boolean> condition) {
    return null;
  }

  /**
   * Mark this {@link OperationGroup} as held. It can be executed but cannot be
   * completed. A {@link OperationGroup} that is held remains in the queue even
   * if all of its current member {@link Operation}s have completed. So long as
   * the {@link OperationGroup} is held new member {@link Operation}s can be
   * submitted. A {@link OperationGroup} that is held must be released before it
   * can be completed and removed from the queue.
   *
   * Note: There is no covariant override of this method in Connection as there
   * is only a small likelihood of needing it.
   *
   * ISSUE: Need a better name.
   *
   * @return a Submission
   * @throws IllegalStateException if this {@link OperationGroup} has been
   * submitted
   */
  @Override
  public Submission<Object> submitHoldingForMoreMembers() {
    this.heldForMoreMember = true;
    return null;
  }

  /**
   * Allow this {@link OperationGroup} to be completed and removed from the
   * queue once all of its member {@link Operation}s have been completed. After
   * this method is called no additional member {@link Operation}s can be
   * submitted. Once all member {@link Operation}s have been removed from the
   * queue this {@link OperationGroup} will be completed and removed from the
   * queue.
   *
   * Calling this method when this {@link OperationGroup} is not held is a noop.
   *
   * Note: There is no covariant override of this method in Connection as there
   * is only a small likelihood of needing it.
   *
   * ISSUE: Need a better name.
   *
   * @return this OperationGroup
   * @throws IllegalStateException if this {@link OperationGroup} has been
   * completed
   */
  @Override
  public OperationGroup<Object, Object> releaseProhibitingMoreMembers() {
    return null;
  }

  /**
   * Provides a {@link Collector} to reduce the results of the member
   * {@link Operation}s. The result of this {@link OperationGroup} is the result
   * of calling finisher on the final accumulated result.If the
   * {@link Collector} is {@link Collector.Characteristics#UNORDERED} the member
   * {@link Operation} results may be accumulated out of order.If the
   * {@link Collector} is {@link Collector.Characteristics#CONCURRENT} then the
   * member {@link Operation} results may be split into subsets that are reduced
   * separately and then combined. If this {@link OperationGroup} is sequential,
   * the characteristics of the {@link Collector} only affect how the results of
   * the member {@link Operation}s are collected; the member {@link Operation}s
   * are executed sequentially regardless. If this {@link OperationGroup} is
   * parallel the characteristics of the {@link Collector} may influence the
   * execution order of the member {@link Operation}s.
   *
   * The default value is
   * {@code Collector.of(()->null, (a,t)->{}, (l,r)->null, a->null)}.
   *
   * @param c the Collector. Not null.
   * @return This OperationGroup
   * @throws IllegalStateException if called more than once or if this
   * {@link OperationGroup} has been submitted
   */
  @Override
  public OperationGroup<Object, Object> collect(Collector<Object, ?, Object> c) {
    return null;
  }

  /**
   * Returns an Operation that is never skipped. Skipping stops with a catchOperation
   * and the subsequent Operation is executed normally. The value of a
   * catchOperation is always null.
   *
   * @return an unskippable Operation;
   */
  @Override
  public Operation<Object> catchOperation() {
    return null;
  }

  /**
   * Return a new {@link ArrayCountOperation}.
   * <p>
   * Usage Note: Frequently use of this method will require a type witness
   * to enable correct type inferencing.
   * <pre><code>
   *   conn.<b>&lt;List&lt;Integer&gt;&gt;</b>arrayCountOperation(sql)
   *     .set ...
   *     .collect ...
   *     .submit ...
   * </code></pre>
   *
   * @param <R> the result type of the returned {@link ArrayCountOperation}
   * @param sql SQL to be executed. Must return an update count.
   * @return a new {@link ArrayCountOperation} that is a member of this
   * {@link OperationGroup}
   */
  @Override
  public <R> ArrayCountOperation<R> arrayCountOperation(String sql) {
    return null;
  }

  /**
   * Return a new {@link CountOperation}.
   *
   * @param <R> the result type of the returned {@link CountOperation}
   * @param sql SQL to be executed. Must return an update count.
   * @return an new {@link CountOperation} that is a member of this
   * {@link OperationGroup}
   *
   */
  @Override
  public <R> ParameterizedCountOperation<R> countOperation(String sql) {
    return new PGCountOperation<R>(this, sql);
  }

  /**
   * Return a new {@link Operation} for a SQL that doesn't return any result,
   * for example DDL.
   *
   * @param sql SQL for the {@link Operation}.
   * @return a new {@link Operation} that is a member of this
   * {@link OperationGroup}
   */
  @Override
  public Operation<Object> operation(String sql) {
    return null;
  }

  /**
   * Return a new {@link OutOperation}. The SQL must return a set of zero or
   * more out parameters or function results.
   *
   * @param <R> the result type of the returned {@link OutOperation}
   * @param sql SQL for the {@link Operation}. Must return zero or more out
   * parameters or function results.
   * @return a new {@link OutOperation} that is a member of this
   * {@link OperationGroup}
   */
  @Override
  public <R> OutOperation<R> outOperation(String sql) {
    return null;
  }

  /**
   * Return a {@link ParameterizedRowOperation}.
   *
   * @param <R> the type of the result of the returned {@link ParameterizedRowOperation}
   * @param sql SQL for the {@link Operation}. Must return a row sequence.
   * @return a new {@link ParameterizedRowOperation} that is a member of this
   * {@link OperationGroup}
   */
  @Override
  public <R> ParameterizedRowOperation<R> rowOperation(String sql) {
    return new PGParameterizedRowOperation<>(this, sql);
  }

  @Override
  public <R> RowProcessorOperation<R> rowProcessorOperation(String sql) {
    return null;
  }

  /**
   * Return a {@link StaticMultiOperation}.
   *
   * @param <R> the type of the result of the returned
   * {@link StaticMultiOperation}
   * @param sql SQL for the {@link Operation}
   * @return a new {@link StaticMultiOperation} that is a member of this
   * {@link OperationGroup}
   */
  @Override
  public <R> StaticMultiOperation<R> staticMultiOperation(String sql) {
    return null;
  }

  /**
   * Return a {@link DynamicMultiOperation}. Use this when the number and type
   * of the results is not knowable.
   *
   * @param <R> the type of the result of the returned
   * {@link DynamicMultiOperation}
   * @param sql SQL for the {@link Operation}
   * @return a new {@link DynamicMultiOperation} that is a member of this
   * {@link OperationGroup}
   */
  @Override
  public <R> DynamicMultiOperation<R> dynamicMultiOperation(String sql) {
    return null;
  }

  /**
   * Return an {@link Operation} that ends the database transaction.
   * The transaction is ended with a commit unless the {@link Transaction} has
   * been {@link Transaction#setRollbackOnly} in which
   * case the transaction is ended with a rollback.
   *
   * The type argument of the containing {@link OperationGroup} must be
   * a supertype of {@link TransactionOutcome}.
   *
   * @param trans the Transaction that determines whether the Operation does a
   * database commit or a database rollback.
   * @return an {@link Operation} that will end the database transaction.
   * @throws IllegalStateException if this {@link OperationGroup} has been submitted and
   * is not held or is parallel.
   */
  @Override
  public Operation<TransactionOutcome> endTransactionOperation(Transaction trans) {
    return null;
  }

  /**
   * Return a {@link LocalOperation}.
   *
   * @return a LocalOperation
   * @throws IllegalStateException if this OperationGroup has been submitted and
   * is not held
   */
  @Override
  public LocalOperation<Object> localOperation() {
    return null;
  }

  /**
   * Returns a Flow.Processor that subscribes to a sequence of Operations and
   * produces a sequence of corresponding Submissions. The Operations must be
   * members of this OperationGroup. Calling Subscription.onNext with any
   * Operation that is not a member of this OperationGroup, that is was not
   * created by calling one of the Operation factory methods on this
   * OperationGroup, will cause the Subscription to be canceled and call
   * Subscriber.onError with IllegalArgumentException. The method
   * Subscription.onNext will call submit on each Operation it is passed and
   * publish the resulting Submission. Since an Operation can only be submitted
   * once, submitting an Operation and calling onNext with that submitted
   * Operation will cause the Subscription to be canceled and Subscriber.onError
   * to be called with IllegalStateException. The Processor does not retain
   * Submissions to produce to a subsequently attached Subscriber.
   *
   * If there is no Subscriber to the Processor, the Processor will request
   * Operations as appropriate. If there is a Subscriber to the Processor, the
   * Processor will request Operations no faster than the Subscriber requests
   * Submissions.
   *
   * Each call to this method returns a new Flow.processor. The Submissions
   * published to each Processor are exactly those generated by calling submit
   * on the Operations passed as arguments to onNext on the same Processor.
   * Calling this method while there is an active Processor will throw
   * IllegalStateException.
   *
   * Note: If any Operation is submitted directly, that is by calling submit
   * rather than passing it to onNext, the Submission returned by the submit
   * call will not be published.
   *
   * @return a Flow.Processor that accepts Operations and generates Submissions
   * @throws IllegalStateException if there is an active Processor
   */
  @Override
  public Flow.Processor<Operation<Object>, Submission<Object>> operationProcessor() {
    return null;
  }

  /**
   * Supply a {@link Logger} for the implementation of this
   * {@link OperationGroup} to use to log significant events. Exactly what
   * events are logged, at what Level the events are logged and with what
   * parameters is implementation dependent. All member {@link Operation}s of
   * this {@link OperationGroup} will use the same {@link Logger} except a
   * member {@link OperationGroup} that is supplied with a different
   * {@link Logger} uses that {@link Logger}.
   *
   * Supplying a {@link Logger} configured with a
   * {@link java.util.logging.MemoryHandler} with the
   * {@link java.util.logging.MemoryHandler#pushLevel} set to
   * {@link java.util.logging.Level#WARNING} will result in no log output in
   * normal operation. In the event of an error the actions leading up to the
   * error will be logged.
   *
   * Implementation Note: Implementations are encouraged to log the creation of
   * this {@link OperationGroup} set to {@link java.util.logging.Level#INFO}, the
   * creation of member {@link Operation}s at the
   * {@link java.util.logging.Level#CONFIG} level, and execution of member
   * {@link Operation}s at the {@link java.util.logging.Level#FINE} level.
   * Detailed information about the execution of member {@link Operation}s may
   * be logged at the {@link java.util.logging.Level#FINER} and
   * {@link java.util.logging.Level#FINEST} levels. Errors in the execution of
   * user code should be logged at the {@link java.util.logging.Level#WARNING}
   * Level. Errors in the implementation code should be logged at the
   * {@link java.util.logging.Level#SEVERE} Level.
   *
   * @param logger used by the implementation to log significant events
   * @return this {@link OperationGroup}
   */
  @Override
  public OperationGroup<Object, Object> logger(Logger logger) {
    return null;
  }

  /**
   * Provides an error handler for this {@link Operation}. If execution of this
   * {@link Operation} results in an error, before the Operation is completed,
   * the handler is called with the {@link Throwable} as the argument.
   *
   * @param handler
   * @return this {@link Operation}
   */
  @Override
  public OperationGroup<Object, Object> onError(Consumer<Throwable> handler) {
    return null;
  }

  /**
   * The minimum time before this {@link Operation} might be canceled
   * automatically. The default value is forever. The time is
   * counted from the beginning of Operation execution. The Operation will not
   * be canceled before {@code minTime} after the beginning of execution.
   * Some time at least {@code minTime} after the beginning of execution,
   * an attempt will be made to cancel the {@link Operation} if it has not yet
   * completed. Implementations are encouraged to attempt to cancel within a
   * reasonable time, though what is reasonable is implementation dependent.
   *
   * @param minTime minimum time to wait before attempting to cancel
   * @return this Operation
   * @throws IllegalArgumentException if minTime &lt;= 0 seconds
   * @throws IllegalStateException if this method is called more than once on
   * this operation
   */
  @Override
  public OperationGroup<Object, Object> timeout(Duration minTime) {
    return null;
  }

  /**
   * Add this {@link Operation} to the tail of the {@link Operation} collection
   * of the {@link Connection} that created this {@link Operation}. An
   * {@link Operation} can be submitted only once. Once an {@link Operation} is
   * submitted it is immutable. Any attempt to modify a submitted
   * {@link Operation} will throw {@link IllegalStateException}.
   *
   * @return a {@link Submission} for this {@link Operation}
   * @throws IllegalStateException if this method is called more than once on
   * this operation
   */
  @Override
  public Submission<Object> submit() {
    accumulator = collector.supplier().get();
    memberTail = attachErrorHandler(follows(memberTail, executor));
    return new PGSubmission(this::cancel);
  }


  boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }

  protected CompletionStage<Object> attachErrorHandler(CompletionStage<Object> result) {
    if (errorHandler != null) {
      return result.exceptionally(t -> {
        Throwable ex = unwrapException(t);
        errorHandler.accept(ex);
        if (ex instanceof SqlSkippedException) throw (SqlSkippedException)ex;
        else throw new SqlSkippedException("TODO", ex, null, -1, null, -1);
      });
    }
    else {
      return result;
    }
  }

  static Throwable unwrapException(Throwable ex) {
    return ex instanceof CompletionException ? ex.getCause() : ex;
  }

  protected CompletionStage<Object> follows(CompletionStage<?> predecessor, Executor executor) {
    head.complete(predecessor); // completing head allows members to execute
    return held.thenCompose( h -> // when held completes memberTail holds the last member
        memberTail.thenApplyAsync( t -> collector.finisher().apply(accumulator), executor));
  }

  public void visit() {
    protocol.visit();
  }

  public void queFrame(FEFrame frame) {
    protocol.queFrame(frame);
  }

  public void addSubmissionOnQue(PGSubmission submission) {
    protocol.addSubmission(submission);
  }
}
