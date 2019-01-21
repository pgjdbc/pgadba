package org.postgresql.adba;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.OperationGroup;
import jdk.incubator.sql2.Session;
import jdk.incubator.sql2.SessionProperty;
import jdk.incubator.sql2.ShardingKey;
import jdk.incubator.sql2.SqlException;
import jdk.incubator.sql2.SqlSkippedException;
import jdk.incubator.sql2.TransactionCompletion;
import org.postgresql.adba.buffer.ByteBufferPool;
import org.postgresql.adba.buffer.PooledByteBuffer;
import org.postgresql.adba.communication.NetworkConnection;
import org.postgresql.adba.communication.network.ImmediateComplete;
import org.postgresql.adba.communication.network.ParseRequest;
import org.postgresql.adba.communication.network.Portal;
import org.postgresql.adba.execution.NioLoop;
import org.postgresql.adba.operations.PgCloseOperation;
import org.postgresql.adba.operations.PgConnectOperation;
import org.postgresql.adba.operations.PgOperationGroup;
import org.postgresql.adba.operations.PgValidationOperation;
import org.postgresql.adba.operations.helpers.PgTransaction;
import org.postgresql.adba.util.PropertyHolder;

public class PgSession extends PgOperationGroup<Object, Object> implements Session {

  private Logger logger = Logger.getLogger(PgSession.class.getName());

  private final PropertyHolder properties;

  private final PgDataSource dataSource;

  private final NetworkConnection protocol;

  protected Consumer<Throwable> errorHandler = null;
  private Lifecycle lifecycle = Lifecycle.NEW;
  private ConcurrentLinkedQueue<SessionLifecycleListener> lifecycleListeners = new ConcurrentLinkedQueue<>();
  private PgSubmission<?> lastSubmission;

  /**
   * Predecessor of all member Operations and the OperationGroup itself.
   */
  private final CompletableFuture head = new CompletableFuture();

  /**
   * Meant for internal usage, use the connection builder instead.
   *
   * @param properties connection properties
   * @param dataSource datasource that this connection is a part of
   * @param loop the nioLoop is what transports data
   * @param bufferPool Pool of {@link PooledByteBuffer} instances.
   * @throws IOException if there is problems with opening a socket channel
   */
  public PgSession(PropertyHolder properties, PgDataSource dataSource,
      NioLoop loop,
      ByteBufferPool bufferPool) throws IOException {
    this.properties = properties;
    this.dataSource = dataSource;
    SocketChannel channel = SocketChannel.open();
    channel.configureBlocking(false);
    this.protocol = new NetworkConnection(this.properties, this, loop, bufferPool);
    this.setConnection(this);
  }

  /**
   * Returns an {@link Operation} that attaches this {@code Session} to a data source. If the Operation completes successfully and
   * the lifecycle is {@link Lifecycle#NEW} -&gt; {@link Lifecycle#ATTACHED}. If the {@link Operation} completes exceptionally the
   * lifecycle -&gt; {@link Lifecycle#CLOSED}. The lifecycle must be {@link Lifecycle#NEW}  when the {@link Operation} is
   * executed. Otherwise the {@link Operation} will complete exceptionally with {@link SqlException}.
   *
   * <p>Note: It is highly recommended to use the {@link Session#attach()} convenience
   * method or to use {@link DataSource#getSession} which itself calls {@link Session#attach()} . Unless there is a specific need,
   * do not call this method directly.</p>
   *
   * @return an {@link Operation} that attaches this {@code Session} to a server.
   * @throws IllegalStateException if this {@code Session} is in a lifecycle state other than {@link Lifecycle#NEW}.
   */
  @Override
  public Operation<Void> attachOperation() {
    if (lifecycle != Lifecycle.NEW) {
      throw new IllegalStateException(
          "only connections in state NEW are allowed to start connecting");
    }

    OperationGroup<Object, Object> group = operationGroup();
    boolean anyAdded = false;
    for (Map.Entry<SessionProperty, Object> entry : getProperties().entrySet()) {
      anyAdded |= entry.getKey().configureOperation(group, entry.getValue());
    }
    if (anyAdded) {
      group.submit();
    }

    return new PgConnectOperation(this, groupSubmission, protocol);
  }

  /**
   * Returns an {@link Operation} that verifies that the resources are available and operational. Successful completion of that
   * {@link Operation} implies that at some point between the beginning and end of the {@link Operation} the Connection was
   * working properly to the extent specified by {@code depth}. There is no guarantee that the {@link Session} is still working
   * after completion.
   *
   * @param depth how completely to check that resources are available and operational. Not {@code null}.
   * @return an {@link Operation} that will validate this {@link Session}
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public Operation<Void> validationOperation(Session.Validation depth) {
    if (lifecycle != Lifecycle.NEW && lifecycle != Lifecycle.ATTACHED) {
      throw new IllegalStateException(
          "session lifecycle in state: " + lifecycle + " and not open for new work");
    }

    return new PgValidationOperation(this, depth);
  }

  /**
   * Create an {@link Operation} to close this {@link Session}. When the {@link Operation} is executed, if this {@link Session} is
   * open -&gt; {@link Lifecycle#CLOSING}. If this {@link Session} is closed executing the returned {@link Operation} is a noop.
   * When the queue is empty and all resources released -&gt; {@link Lifecycle#CLOSED}.
   *
   * <p>A close {@link Operation} is never skipped. Even when the {@link Session} is dependent, the
   * default, and an {@link Operation} completes exceptionally, a close {@link Operation} is still executed. If the {@link
   * Session} is parallel, a close {@link Operation} is not executed so long as there are other {@link Operation}s or the {@link
   * Session} is held; for more {@link Operation}s.
   *
   * <p>Note: It is highly recommended to use try with resources or the {@link Session#close()}
   * convenience method. Unless there is a specific need, do not call this method directly.
   *
   * @return an {@link Operation} that will close this {@link Session}.
   * @throws IllegalStateException if the Connection is not active
   */
  @Override
  public Operation<Void> closeOperation() {
    Lifecycle oldLifecycle = lifecycle;
    lifecycle = lifecycle.close();

    for (SessionLifecycleListener listener : lifecycleListeners) {
      listener.lifecycleEvent(this, oldLifecycle, lifecycle);
    }

    return new PgCloseOperation(this, protocol);
  }

  /**
   * Create a new {@link OperationGroup} for this {@link Session}.
   *
   * @param <S> the result type of the member {@link Operation}s of the returned {@link OperationGroup}
   * @param <T> the result type of the collected results of the member {@link Operation}s
   * @return a new {@link OperationGroup}.
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public <S, T> OperationGroup<S, T> operationGroup() {
    if (lifecycle != Lifecycle.NEW && lifecycle != Lifecycle.ATTACHED) {
      throw new IllegalStateException(
          "session lifecycle in state: " + lifecycle + " and not open for new work");
    }

    if (logger.isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, "OperationGroup created for connection " + this);
    }

    return new PgOperationGroup<>(this);
  }

  /**
   * Returns a new {@link TransactionCompletion} that can be used as an argument to a commit Operation.
   *
   * <p>It is most likely an error to call this within an error handler, or any handler as it is very
   * likely that when the handler is executed the next submitted endTransaction {@link Operation} will have been created with a
   * different Transaction.
   *
   * @return a new {@link TransactionCompletion}. Not retained.
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public TransactionCompletion transactionCompletion() {
    return new PgTransaction();
  }

  /**
   * Register a listener that will be called whenever there is a change in the lifecycle of this {@link Session}.
   *
   * @param listener Can be {@code null}.
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public Session registerLifecycleListener(Session.SessionLifecycleListener listener) {
    if (lifecycle != Lifecycle.NEW && lifecycle != Lifecycle.ATTACHED) {
      throw new IllegalStateException("connection not active");
    }

    if (listener != null) {
      lifecycleListeners.add(listener);
    }

    return this;
  }

  /**
   * Removes a listener that was registered by calling registerLifecycleListener.Sometime after this method is called the listener
   * will stop receiving lifecycle events. If the listener is not registered, this is a no-op.
   *
   * @param listener Not {@code null}.
   * @return this Connection
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public Session deregisterLifecycleListener(SessionLifecycleListener listener) {
    if (lifecycle != Lifecycle.NEW && lifecycle != Lifecycle.ATTACHED) {
      throw new IllegalStateException("connection not active");
    }

    if (listener != null) {
      lifecycleListeners.remove(listener);
    }

    return this;
  }

  /**
   * Return the current lifecycle of this {@link Session}.
   *
   * @return the current lifecycle of this {@link Session}.
   */
  @Override
  public Lifecycle getSessionLifecycle() {
    return lifecycle;
  }

  /**
   * Terminate this {@code Session}. If lifecycle is {@link Lifecycle#NEW}, {@link Lifecycle#ATTACHED} or {@link
   * Lifecycle#CLOSING} -&gt; {@link Lifecycle#ABORTING} If lifecycle is {@link Lifecycle#ABORTING} or {@link Lifecycle#CLOSED}
   * this is a no-op. If an {@link Operation} is currently executing, terminate it immediately. Remove all remaining {@link
   * Operation}s from the queue. {@link Operation}s are not skipped. They are just removed from the queue.
   *
   * @return this {@code Session}
   */
  @Override
  public Session abort() {
    Lifecycle oldLifecycle = lifecycle;
    lifecycle = lifecycle.abort();

    for (SessionLifecycleListener listener : lifecycleListeners) {
      listener.lifecycleEvent(this, oldLifecycle, lifecycle);
    }

    return this;
  }

  /**
   * Return the set of properties configured on this {@link Session} excepting any sensitive properties. Neither the key nor the
   * value for sensitive properties are included in the result. Properties (other than sensitive properties) that have default
   * values are included even when not explicitly set. Properties that have no default value and are not set explicitly are not
   * included.
   *
   * @return a {@link Map} of property, value. Not modifiable. May be retained. Not {@code null}.
   * @throws IllegalStateException if this Connection is not active
   */
  @Override
  public Map<SessionProperty, Object> getProperties() {
    return properties.getAll();
  }

  /**
   * Returns a {@link ShardingKey.Builder} that is valid for this {@link Session}.
   *
   * @return a {@link ShardingKey.Builder} for this {@link Session}
   */
  @Override
  public ShardingKey.Builder shardingKeyBuilder() {
    throw new RuntimeException("not implemented yet");
  }

  @Override
  public Session requestHook(LongConsumer request) {
    return this;
  }

  @Override
  public void close() {
    this.closeOperation()
        .submit();
  }

  protected CompletionStage<Object> attachErrorHandler(CompletionStage<Object> result) {
    if (errorHandler != null) {
      return result.exceptionally(t -> {
        Throwable ex = unwrapException(t);
        errorHandler.accept(ex);
        if (ex instanceof SqlSkippedException) {
          throw (SqlSkippedException) ex;
        } else {
          throw new SqlSkippedException("TODO", ex, null, -1, null, -1);
        }
      });
    } else {
      return result;
    }
  }

  static Throwable unwrapException(Throwable ex) {
    return ex instanceof CompletionException ? ex.getCause() : ex;
  }

  /**
   * Send a new submission over the connection.
   *
   * @param submission object to send
   */
  public void submit(PgSubmission<?> submission) {
    switch (submission.getCompletionType()) {
      case LOCAL:
      case CATCH:
        protocol.sendNetworkRequest(new ImmediateComplete(submission));
        break;
      case GROUP:
        if (lastSubmission != null) {
          ((CompletableFuture<?>) lastSubmission.getCompletionStage()).thenApply(a ->
              submission.finish(null));
        }
        break;

      default:
        Portal portal = new Portal(submission);
        protocol.sendNetworkRequest(new ParseRequest<>(portal));
    }
    lastSubmission = submission;
  }

  public void unregister() {
    this.dataSource.unregisterConnection(this);
  }

  public boolean isConnectionClosed() {
    return protocol.isConnectionClosed();
  }

  /**
   * sets the lifecycle of this object to open and notifies listeners.
   */
  public void setLifeCycleOpen() {
    Lifecycle oldLifecycle = lifecycle;
    this.lifecycle = lifecycle.attach();

    for (SessionLifecycleListener listener : lifecycleListeners) {
      listener.lifecycleEvent(this, oldLifecycle, lifecycle);
    }

  }

  /**
   * sets the lifecycle of this object to open and notifies listeners.
   */
  public void setLifeCycleClosed() {
    Lifecycle oldLifecycle = lifecycle;
    this.lifecycle = lifecycle.closed();

    for (SessionLifecycleListener listener : lifecycleListeners) {
      listener.lifecycleEvent(this, oldLifecycle, lifecycle);
    }

  }
}
