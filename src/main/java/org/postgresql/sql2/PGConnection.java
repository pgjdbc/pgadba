/*
 * Copyright (c) 2018, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */
package org.postgresql.sql2;

import java.sql2.BatchCountOperation;
import java.sql2.Connection;
import java.sql2.ConnectionProperty;
import java.sql2.DynamicMultiOperation;
import java.sql2.LocalOperation;
import java.sql2.Operation;
import java.sql2.OperationGroup;
import java.sql2.OutOperation;
import java.sql2.ParameterizedCountOperation;
import java.sql2.ParameterizedRowOperation;
import java.sql2.PublisherOperation;
import java.sql2.StaticMultiOperation;
import java.sql2.Submission;
import java.sql2.Transaction;
import java.sql2.TransactionOutcome;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;

public class PGConnection implements Connection {
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
   * Note: It is highly recommended to use the {@link connect()} convenience
   * method or to use {@link DataSource#getConnection} which itself calls
   * {@link connect()}. Unless there is a specific need, do not call this method
   * directly.
   *
   * This method exists partially to clearly explain that while creating a
   * {@link Connection} is non-blocking, the act of connecting to the server may
   * block and so is executed asynchronously. We could write a bunch of text
   * saying this but defining this method is more explicit. Given the
   * {@link connect()} convenience methods there's probably not much reason to
   * use this method, but on the other hand, who knows, so here it is.
   *
   * @return an {@link Operation} that connects this {@link Connection} to a
   * server.
   * @throws IllegalStateException if this {@link Connection} is in a lifecycle
   *                               state other than {@link Lifecycle#NEW}.
   */
  @Override
  public Operation<Void> connectOperation() {
    return null;
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
   *              operational. Not {@code null}.
   * @return an {@link Operation} that will validate this {@link Connection}
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public Operation<Void> validationOperation(Validation depth) {
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
   * {@link close()} convenience method. Unless there is a specific need, do not
   * call this method directly.
   *
   * @return an {@link Operation} that will close this {@link Connection}.
   * @throws IllegalStateException if the Connection is not active
   */
  @Override
  public Operation<Void> closeOperation() {
    return null;
  }

  /**
   * Create a new {@link OperationGroup} for this {@link Connection}.
   *
   * @return a new {@link OperationGroup}.
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public <S, T> OperationGroup<S, T> operationGroup() {
    return null;
  }

  /**
   * Returns the current {@link Transaction} relative to the execution of
   * submitted {@link Operation}s. At the moment this method is called any
   * transaction currently in flight on the Connection may be unrelated to the
   * {@link Operation}s submitted immediately before or after the call. The
   * returned {@link Transaction} represents the transaction in flight while the
   * next submitted commit Operation is executed.
   *
   * It is most likely an error to call this within an error handler, or any
   * handler as it is very likely that when the handler is executed the next
   * submitted commit {@link Operation} will not be the one the programmer
   * intends. Even if it is this code would be fragile and difficult to
   * maintain. Instead call {@link getTransaction} before submitting an
   * {@link Operation} and use that {@link Transaction} in any handlers for that
   * {@link Operation}.
   *
   * @return the current {@link Transaction}
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public Transaction getTransaction() {
    return null;
  }

  /**
   * Register a listener that will be called whenever there is a change in the
   * lifecycle of this {@link Connection}.
   *
   * @param listener@throws IllegalStateException if this Connection is not active
   */
  @Override
  public void registerLifecycleListener(ConnectionLifecycleListener listener) {

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
   * Return the current lifecycle of this {@link Connection}.
   *
   * @return the current lifecycle of this {@link Connection}.
   */
  @Override
  public Lifecycle getLifecycle() {
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
   * Enables the {@link Connection} to provide backpressure on the rate at which
   * {@link Operation}s are submitted. Use of this method is optional.
   *
   * If an application may submit a large number of {@link Operation}s it may be
   * that it submits those {@link Operation}s faster than the {@link Connection}
   * can process them. Since {@link Operation}s consume resources submitting a
   * large number of {@link Operation}s may bog down the system. Providing a
   * {@link Flow.Subscription} to the {@link Connection}
   * enables the {@link Connection} to request additional {@link Operation}s as
   * appropriate. Applications are not required to adhere to the number of
   * {@link Operation}s requested by the {@link Connection}. If an application
   * provides a
   * {@link Flow.Subscription}, {@link Connection}s are
   * guaranteed to call {@link Flow.Subscription#request}
   * whenever the number of queued {@link Operation}s drops to zero and the
   * {@link Connection} can accept more {@link Operation}s.
   *
   * Note: {@link Connection} does not use
   * {@link Flow.Publisher} and
   * {@link Flow.Subscriber} as the methods defined by
   * those interfaces do not make an sense. Those methods provide a channel for
   * the {@link Flow.Publisher} to pass objects to the
   * {@link Flow.Subscriber}. Connection has no need for
   * that channel as {@link Operation}s are known to a {@link Connection}
   * because they are constructed by that {@link Connection}.
   *
   * @param subscription not {@code null}.
   * @throws IllegalArgumentException if {@code subscription} is {@code null}
   * @throws IllegalStateException    if this Connection is not active
   */
  @Override
  public void onSubscribe(Flow.Subscription subscription) {

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
   * method calling any method other than {@link deactivate}, {@link activate},
   * {@link abort}, or {@link getLifecycle} or submitting any member
   * {@link Operation} will throw {@link IllegalStateException}. Local
   * {@link Connection} state not created by {@link Builder} may not
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
   *                               member {@link Operation}s have been created.
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
   *                               submitted or any member {@link Operation}s have been created
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
   * @param condition a {@link CompletableFuture} the value of which determines whether
   *                  this {@link OperationGroup} is executed or not
   * @return this OperationGroup
   * @throws IllegalStateException if this {@link OperationGroup} has been
   *                               submitted or any member {@link Operation}s have been created
   */
  @Override
  public OperationGroup<Object, Object> conditional(CompletableFuture<Boolean> condition) {
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
   * @return this OperationGroup
   * @throws IllegalStateException if this {@link OperationGroup} has been
   *                               submitted
   */
  @Override
  public OperationGroup<Object, Object> holdForMoreMembers() {
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
   *                               completed
   */
  @Override
  public OperationGroup<Object, Object> releaseProhibitingMoreMembers() {
    return null;
  }

  /**
   * Supplier of the initial value provided to {@link memberAggregator}. The
   * default value is {@code () -&gt; null}.
   *
   * @param supplier provides the initial value for the {@link memberAggregator}
   * @return this {@link OperationGroup}
   * @throws IllegalStateException if called more than once or if this
   *                               {@link OperationGroup} has been submitted
   */
  @Override
  public OperationGroup<Object, Object> initialValue(Supplier<Object> supplier) {
    return null;
  }

  /**
   * Function that aggregates the results of the member {@link Operation}s.
   * Called once for each member {@link Operation} that completes normally. The
   * first argument of the first call is the value supplied by
   * {@link initialValue} {@code supplier}. For; subsequent calls it is the
   * value returned by the previous call. If this {@link OperationGroup} is
   * sequential the values are passed to the function in the order the member
   * {@link Operation}s complete. If this {@link OperationGroup} is parallel,
   * values may be passed to the function in any order though an approximation
   * of the order in which they complete is recommended. The default value is
   * {@code (a, b) -&gt; null}.
   *
   * @param aggregator a {@link BiFunction} that aggregates the results of the
   *                   member {@link Operation}s
   * @return this {@link OperationGroup}
   * @throws IllegalStateException if called more than once or if this
   *                               {@link OperationGroup} has been submitted
   */
  @Override
  public OperationGroup<Object, Object> memberAggregator(
      BiFunction<Object, Object, Object> aggregator) {
    return null;
  }

  /**
   * Return a new {@link BatchCountOperation}.
   *
   * @param sql SQL to be executed. Must return an update count.
   * @return a new {@link BatchCountOperation} that is a member of this
   * {@link OperationGroup}
   */
  @Override
  public <R> BatchCountOperation<R> batchCountOperation(String sql) {
    return null;
  }

  /**
   * Return a new {@link CountOperation}.
   *
   * @param sql SQL to be executed. Must return an update count.
   * @return an new {@link CountOperation} that is a member of this
   * {@link OperationGroup}
   */
  @Override
  public <R> ParameterizedCountOperation<R> countOperation(String sql) {
    return null;
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
  public Operation<Void> operation(String sql) {
    return null;
  }

  /**
   * Return a new {@link OutOperation}. The SQL must return a set of zero or
   * more out parameters or function results.
   *
   * @param sql SQL for the {@link Operation}. Must return zero or more out
   *            parameters or function results.
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
   * @param sql SQL for the {@link Operation}. Must return a row sequence.
   * @return a new {@link ParameterizedRowOperation} that is a member of this
   * {@link OperationGroup}
   */
  @Override
  public <R> ParameterizedRowOperation<R> rowOperation(String sql) {
    return null;
  }

  /**
   * Return a {@link StaticMultiOperation}.
   *
   * @param sql SQL for the {@link Operation}
   * @return a new {@link StaticMultiOperation} that is a member of this
   * {@link OperationGroup}
   */
  @Override
  public <R> StaticMultiOperation<R> staticMultiOperation(String sql) {
    return null;
  }

  @Override
  public <R> PublisherOperation<R> publisherOperation(String sql) {
    return null;
  }

  /**
   * Return a {@link DynamicMultiOperation}. Use this when the number and type
   * of the results is not knowable.
   *
   * @param sql SQL for the {@link Operation}
   * @return a new {@link DynamicMultiOperation} that is a member of this
   * {@link OperationGroup}
   */
  @Override
  public <R> DynamicMultiOperation<R> dynamicMultiOperation(String sql) {
    return null;
  }

  /**
   * Return an {@link Operation} that ends the current database transaction.
   * After submitting this {@link Operation} there is no current
   * {@link Transaction}. The transaction is ended with a commit unless the
   * {@link Transaction} has been {@link Transaction#setRollbackOnly} in which
   * case the transaction is ended with a rollback.
   *
   * If an {@link OperationGroup} has this as a member, the type argument
   * {@link S} of that {@link OperationGroup} must be a supertype of
   * {@link TransactionOutcome}.
   *
   * @return an Operation that will end the current transaction. This Operation
   * will end the transaction as specified by the {@link Transaction} that was
   * current when this Operation was submitted.
   * @throws IllegalStateException if this {@link OperationGroup} is parallel.
   */
  @Override
  public Operation<TransactionOutcome> commitOperation() {
    return null;
  }

  /**
   * Return an {@link Operation} that rollsback the current database
   * transaction. After submitting this Operation there is no current
   * {@link Transaction}. The transaction is ended with a rollback.
   *
   * If an {@link OperationGroup} has this as a member, the type argument
   * {@link S} of that {@link OperationGroup} must be a supertype of
   * {@link TransactionOutcome}.
   *
   * @return the {@link Submission} for an {@link Operation} that will always
   * rollback the current database transaction.
   * @throws IllegalStateException if this {@link OperationGroup} is parallel.
   */
  @Override
  public Operation<TransactionOutcome> rollbackOperation() {
    return null;
  }

  /**
   * Return a Runnable Operation
   *
   * @return a LocalOperation
   * @throws IllegalStateException if this OperationGroup has been submitted and
   *                               is not held
   */
  @Override
  public LocalOperation<Object> localOperation() {
    return null;
  }

  /**
   * Provide a {@link Flow.Publisher} that will stream {@link Operation}s to this
   * {@link OperationGroup}. Use of this method is optional. Any
   * {@link Operation} passed to {@link Flow.Subscriber#onNext} must be created by
   * this {@link OperationGroup}. If it is not {@link Flow.Subscriber#onNext} throws
   * {@link IllegalArgumentException}. {@link Flow.Subscriber#onNext} submits the
   * {@link Operation} argument, but calling {@link Flow.Subscriber#onNext} is optional. As an
   * alternative the {@link Flow.Publisher} can call {@link Operation#submit}. Since
   * {@link Flow.Subscriber#onNext} submits the {@link Operation} only one of the two can be
   * called otherwise the {@link Operation} is submitted twice. Calling
   * {@link Operation#submit} decrements the request count so far as the
   * {@link Flow.Subscriber} is concerned.
   *
   * ISSUE: This is a hack. The {@code submit} or {@code onNext} alternative is
   * weird but necessary. Other choices include calling only {@code submit} or
   * requiring the {@link Flow.Publisher} to call both, {@code onNext} first then
   * {@code submit}. Neither of those seems better. Calling only {@code onNext}
   * isn't acceptable as then there is no way to get access to the
   * {@link Submission} or the {@link CompletableFuture}.
   *
   * @return this OperationGroup
   */
  @Override
  public OperationGroup<Object, Object> operationPublisher(Flow.Publisher<Operation> publisher) {
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
   * {@link MemoryHandler} with the
   * {@link MemoryHandler#pushLevel} set to
   * {@link Level#WARNING} will result in no log output in
   * normal operation. In the event of an error the actions leading up to the
   * error will be logged.
   *
   * Implementation Note: Implementations are encouraged to log the creation of
   * this {@link OperationGroup} set to {@link Level#INFO}, the
   * creation of member {@link Operation}s at the
   * {@link Level#CONFIG} level, and execution of member
   * {@link Operation}s at the {@link Level#FINE} level.
   * Detailed information about the execution of member {@link Operation}s may
   * be logged at the {@link Level#FINER} and
   * {@link Level#FINEST} levels. Errors in the execution of
   * user code should be logged at the {@link Level#WARNING}
   * Level. Errors in the implementation code should be logged at the
   * {@link Level#SEVERE} Level.
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
   * @return this {@link Operation}
   */
  @Override
  public Operation<Object> onError(Consumer<Throwable> handler) {
    return null;
  }

  @Override
  public OperationGroup<Object, Object> timeout(long milliseconds) {
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
   *                               this operation
   */
  @Override
  public Submission<Object> submit() {
    return null;
  }
}
