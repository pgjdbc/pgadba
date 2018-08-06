/*
 * Copyright (c) 2018, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */
package org.postgresql.sql2;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;

import org.postgresql.sql2.buffer.ByteBufferPool;
import org.postgresql.sql2.communication.NetworkRequest;
import org.postgresql.sql2.communication.NetworkConnect;
import org.postgresql.sql2.communication.NetworkConnection;
import org.postgresql.sql2.execution.NioLoop;
import org.postgresql.sql2.operations.PGCloseOperation;
import org.postgresql.sql2.operations.PGConnectOperation;
import org.postgresql.sql2.operations.PGOperationGroup;
import org.postgresql.sql2.operations.PGValidationOperation;
import org.postgresql.sql2.operations.helpers.PGTransaction;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.ConnectionProperty;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.OperationGroup;
import jdk.incubator.sql2.ShardingKey;
import jdk.incubator.sql2.SqlException;
import jdk.incubator.sql2.SqlSkippedException;
import jdk.incubator.sql2.Transaction;

public class PGConnection extends PGOperationGroup<Object, Object> implements Connection {
  protected static final CompletionStage<Object> ROOT = CompletableFuture.completedFuture(null);

  static final Collector DEFAULT_COLLECTOR = Collector.of(() -> null, (a, v) -> {
  }, (a, b) -> null, a -> null);

  private Logger logger = Logger.getLogger(PGConnection.class.getName());

  private final Map<ConnectionProperty, Object> properties;

  private final NetworkConnection protocol;

  private Object accumulator;
  private Collector collector = DEFAULT_COLLECTOR;
  protected Consumer<Throwable> errorHandler = null;
  private Lifecycle lifecycle = Lifecycle.NEW;
  private ConcurrentLinkedQueue<ConnectionLifecycleListener> lifecycleListeners = new ConcurrentLinkedQueue<>();

  /**
   * predecessor of all member Operations and the OperationGroup itself
   */
  private final CompletableFuture head = new CompletableFuture();

  public PGConnection(Map<ConnectionProperty, Object> properties, NioLoop loop, ByteBufferPool bufferPool) throws IOException {
    this.properties = properties;
    SocketChannel channel = SocketChannel.open();
    channel.configureBlocking(false);
    this.protocol = (NetworkConnection) loop.registerNioService(channel, (context) -> {
      return new NetworkConnection(this.properties, context, bufferPool);
    });
    this.setConnection(this);
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
   * <p>
   * Note: It is highly recommended to use the {@link Connection#connect()}
   * convenience method or to use {@link DataSource#getConnection} which itself
   * calls {@link Connection#connect()}. Unless there is a specific need, do not
   * call this method directly.
   * <p>
   * This method exists partially to clearly explain that while creating a
   * {@link Connection} is non-blocking, the act of connecting to the server may
   * block and so is executed asynchronously. We could write a bunch of text
   * saying this but defining this method is more explicit. Given the
   * {@link Connection#connect()} convenience methods there's probably not much
   * reason to use this method, but on the other hand, who knows, so here it is.
   *
   * @return an {@link Operation} that connects this {@link Connection} to a
   *         server.
   * @throws IllegalStateException if this {@link Connection} is in a lifecycle
   *                               state other than {@link Lifecycle#NEW}.
   */
  @Override
  public Operation<Void> connectOperation() {
    if (lifecycle != Lifecycle.NEW) {
      throw new IllegalStateException("only connections in state NEW are allowed to start connecting");
    }

    return new PGConnectOperation(this, groupSubmission);
  }

  /**
   * Returns an {@link Operation} that verifies that the resources are available
   * and operational. Successful completion of that {@link Operation} implies that
   * at some point between the beginning and end of the {@link Operation} the
   * Connection was working properly to the extent specified by {@code depth}.
   * There is no guarantee that the {@link Connection} is still working after
   * completion.
   *
   * @param depth how completely to check that resources are available and
   *              operational. Not {@code null}.
   * @return an {@link Operation} that will validate this {@link Connection}
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public Operation<Void> validationOperation(Connection.Validation depth) {
    if (!lifecycle.isOpen() || !lifecycle.isActive()) {
      throw new IllegalStateException("connection lifecycle in state: " + lifecycle + " and not open for new work");
    }

    return new PGValidationOperation(this, depth);
  }

  /**
   * Create an {@link Operation} to close this {@link Connection}. When the
   * {@link Operation} is executed, if this {@link Connection} is open -&gt;
   * {@link Lifecycle#CLOSING}. If this {@link Connection} is closed executing the
   * returned {@link Operation} is a noop. When the queue is empty and all
   * resources released -&gt; {@link Lifecycle#CLOSED}.
   * <p>
   * A close {@link Operation} is never skipped. Even when the {@link Connection}
   * is dependent, the default, and an {@link Operation} completes exceptionally,
   * a close {@link Operation} is still executed. If the {@link Connection} is
   * parallel, a close {@link Operation} is not executed so long as there are
   * other {@link Operation}s or the {@link Connection} is held; for more
   * {@link Operation}s.
   * <p>
   * Note: It is highly recommended to use try with resources or the
   * {@link Connection#close()} convenience method. Unless there is a specific
   * need, do not call this method directly.
   *
   * @return an {@link Operation} that will close this {@link Connection}.
   * @throws IllegalStateException if the Connection is not active
   */
  @Override
  public Operation<Void> closeOperation() {
    Lifecycle oldLifecycle = lifecycle;
    lifecycle = lifecycle.close();

    for (ConnectionLifecycleListener listener : lifecycleListeners) {
      listener.lifecycleEvent(this, oldLifecycle, lifecycle);
    }

    return new PGCloseOperation(this);
  }

  /**
   * Create a new {@link OperationGroup} for this {@link Connection}.
   *
   * @param <S> the result type of the member {@link Operation}s of the returned
   *        {@link OperationGroup}
   * @param <T> the result type of the collected results of the member
   *        {@link Operation}s
   * @return a new {@link OperationGroup}.
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public <S, T> OperationGroup<S, T> operationGroup() {
    if (!lifecycle.isOpen() || !lifecycle.isActive()) {
      throw new IllegalStateException("connection lifecycle in state: " + lifecycle + " and not open for new work");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "OperationGroup created for connection " + this);
    }

    return new PGOperationGroup<>(this);
  }

  /**
   * Returns a new {@link Transaction} that can be used as an argument to a commit
   * Operation.
   * <p>
   * It is most likely an error to call this within an error handler, or any
   * handler as it is very likely that when the handler is executed the next
   * submitted endTransaction {@link Operation} will have been created with a
   * different Transaction.
   *
   * @return a new {@link Transaction}. Not retained.
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public Transaction transaction() {
    return new PGTransaction();
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
    if (!lifecycle.isActive()) {
      throw new IllegalStateException("connection not active");
    }

    if (listener != null) {
      lifecycleListeners.add(listener);
    }

    return this;
  }

  /**
   * Removes a listener that was registered by calling
   * registerLifecycleListener.Sometime after this method is called the listener
   * will stop receiving lifecycle events. If the listener is not registered, this
   * is a no-op.
   *
   * @param listener Not {@code null}.
   * @return this Connection
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public Connection deregisterLifecycleListener(ConnectionLifecycleListener listener) {
    if (!lifecycle.isActive()) {
      throw new IllegalStateException("connection not active");
    }

    if (listener != null) {
      lifecycleListeners.remove(listener);
    }

    return null;
  }

  /**
   * Return the current lifecycle of this {@link Connection}.
   *
   * @return the current lifecycle of this {@link Connection}.
   */
  @Override
  public Lifecycle getConnectionLifecycle() {
    return lifecycle;
  }

  /**
   * Terminate this {@link Connection}. If lifecycle is {@link Lifecycle#NEW},
   * {@link Lifecycle#OPEN}, {@link Lifecycle#INACTIVE} or
   * {@link Lifecycle#CLOSING} -&gt; {@link Lifecycle#ABORTING} If lifecycle is
   * {@link Lifecycle#ABORTING} or {@link Lifecycle#CLOSED} this is a noop. If an
   * {@link Operation} is currently executing, terminate it immediately. Remove
   * all remaining {@link Operation}s from the queue. {@link Operation}s are not
   * skipped. They are just removed from the queue.
   *
   * @return this {@link Connection}
   */
  @Override
  public Connection abort() {
    Lifecycle oldLifecycle = lifecycle;
    lifecycle = lifecycle.abort();

    for (ConnectionLifecycleListener listener : lifecycleListeners) {
      listener.lifecycleEvent(this, oldLifecycle, lifecycle);
    }

    return null;
  }

  /**
   * Return the set of properties configured on this {@link Connection} excepting
   * any sensitive properties. Neither the key nor the value for sensitive
   * properties are included in the result. Properties (other than sensitive
   * properties) that have default values are included even when not explicitly
   * set. Properties that have no default value and are not set explicitly are not
   * included.
   *
   * @return a {@link Map} of property, value. Not modifiable. May be retained.
   *         Not {@code null}.
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public Map<ConnectionProperty, Object> getProperties() {
    return null;
  }

  /**
   * @return a {@link ShardingKey.Builder} for this {@link Connection}
   */
  @Override
  public ShardingKey.Builder shardingKeyBuilder() {
    return null;
  }

  @Override
  public Connection requestHook(Consumer<Long> request) {
    return this;
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
    Lifecycle oldLifecycle = lifecycle;
    this.lifecycle = lifecycle.activate();

    for (ConnectionLifecycleListener listener : lifecycleListeners) {
      listener.lifecycleEvent(this, oldLifecycle, lifecycle);
    }

    return this;
  }

  /**
   * Makes this {@link Connection} inactive. After a call to this method
   * previously submitted Operations will be executed normally. If the lifecycle
   * is {@link Lifecycle#NEW} -&gt; {@link Lifecycle#NEW_INACTIVE}. if the
   * lifecycle is {@link Lifecycle#OPEN} -&gt; {@link Lifecycle#INACTIVE}. If the
   * lifecycle is {@link Lifecycle#INACTIVE} or {@link Lifecycle#NEW_INACTIVE}
   * this method is a no-op. After calling this method calling any method other
   * than {@link Connection#deactivate}, {@link Connection#activate},
   * {@link Connection#abort}, or {@link Connection#getConnectionLifecycle} or
   * submitting any member {@link Operation} will throw
   * {@link IllegalStateException}. Local {@link Connection} state not created by
   * {@link Connection.Builder} may not be preserved.
   * <p>
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
    Lifecycle oldLifecycle = lifecycle;
    this.lifecycle = lifecycle.deactivate();

    for (ConnectionLifecycleListener listener : lifecycleListeners) {
      listener.lifecycleEvent(this, oldLifecycle, lifecycle);
    }

    return this;
  }

  protected CompletionStage<Object> attachErrorHandler(CompletionStage<Object> result) {
    if (errorHandler != null) {
      return result.exceptionally(t -> {
        Throwable ex = unwrapException(t);
        errorHandler.accept(ex);
        if (ex instanceof SqlSkippedException)
          throw (SqlSkippedException) ex;
        else
          throw new SqlSkippedException("TODO", ex, null, -1, null, -1);
      });
    } else {
      return result;
    }
  }

  static Throwable unwrapException(Throwable ex) {
    return ex instanceof CompletionException ? ex.getCause() : ex;
  }
  
  public void networkConnect(NetworkConnect connect) {
    protocol.sendNetworkConnect(connect);
  }

  public void addNetworkAction(NetworkRequest action) {
    protocol.sendNetworkRequest(action);
  }

  public boolean isConnectionClosed() {
    return protocol.isConnectionClosed();
  }

  public void setLifeCycleOpen() {
    Lifecycle oldLifecycle = lifecycle;
    this.lifecycle = lifecycle.connect();

    for (ConnectionLifecycleListener listener : lifecycleListeners) {
      listener.lifecycleEvent(this, oldLifecycle, lifecycle);
    }

  }
}
