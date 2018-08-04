package org.postgresql.sql2.communication;

import static org.postgresql.sql2.communication.packets.parts.ErrorResponseField.Types.DETAIL;
import static org.postgresql.sql2.communication.packets.parts.ErrorResponseField.Types.HINT;
import static org.postgresql.sql2.communication.packets.parts.ErrorResponseField.Types.MESSAGE;
import static org.postgresql.sql2.communication.packets.parts.ErrorResponseField.Types.SEVERITY;
import static org.postgresql.sql2.communication.packets.parts.ErrorResponseField.Types.SQLSTATE_CODE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.postgresql.sql2.PGConnectionProperties;
import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.buffer.ByteBufferPool;
import org.postgresql.sql2.buffer.ByteBufferPoolOutputStream;
import org.postgresql.sql2.buffer.PooledByteBuffer;
import org.postgresql.sql2.communication.packets.AuthenticationRequest;
import org.postgresql.sql2.communication.packets.CommandComplete;
import org.postgresql.sql2.communication.packets.DataRow;
import org.postgresql.sql2.communication.packets.ErrorResponse;
import org.postgresql.sql2.communication.packets.ParameterStatus;
import org.postgresql.sql2.communication.packets.ReadyForQuery;
import org.postgresql.sql2.communication.packets.RowDescription;
import org.postgresql.sql2.execution.NioService;
import org.postgresql.sql2.execution.NioServiceContext;
import org.postgresql.sql2.util.BinaryHelper;
import org.postgresql.sql2.util.PGCount;
import org.postgresql.sql2.util.PreparedStatementCache;

import jdk.incubator.sql2.ConnectionProperty;
import jdk.incubator.sql2.SqlException;

public class NetworkConnection
    implements NioService, NetworkInitialiseContext, NetworkConnectContext, NetworkWriteContext, NetworkReadContext {

  private final Map<ConnectionProperty, Object> properties;

  private final NioServiceContext context;

  private ByteBufferPoolOutputStream outputStream;

  private final SocketChannel socketChannel;

  private final Queue<NetworkAction> requestQueue = new ConcurrentLinkedQueue<>();

  private final Queue<NetworkAction> awaitingResults = new LinkedList<>();

  private BEFrameParser parser = new BEFrameParser();

  // TODO refactor out the below fields
  private ProtocolV3States.States currentState = ProtocolV3States.States.NOT_CONNECTED;
  private PreparedStatementCache preparedStatementCache = new PreparedStatementCache();
  private ConcurrentLinkedQueue<String> descriptionNameQue = new ConcurrentLinkedQueue<>();
  private ConcurrentLinkedQueue<String> sentSqlNameQue = new ConcurrentLinkedQueue<>();

  // Context state
  private BEFrame beFrame = null;

  public NetworkConnection(Map<ConnectionProperty, Object> properties, NioServiceContext context,
      ByteBufferPool bufferPool) {
    this.properties = properties;
    this.context = context;
    this.outputStream = new ByteBufferPoolOutputStream(bufferPool);
    this.socketChannel = (SocketChannel) context.getChannel();
  }

  public void addNetworkAction(NetworkAction networkAction) throws IOException {

    // Initialise the network request
    networkAction.init(this);

    // Ready network request for writing
    this.requestQueue.add(networkAction);
    this.context.writeRequired();
  }

  /*
   * =============== NioService =====================
   */

  @Override
  public void handleConnect() throws IOException {
    this.awaitingResults.peek().connect(this);
  }

  /**
   * Last awaiting {@link NetworkAction} to avoid {@link NetworkAction} being
   * registered twice for waiting.
   */
  private NetworkAction lastAwaitingResult = null;

  /**
   * Possible previous incomplete {@link PooledByteBuffer} not completely written.
   */
  private PooledByteBuffer incompleteWriteBuffer = null;

  @Override
  public void handleWrite() throws IOException {

    // Write in the incomplete write buffer (should always have space)
    if (this.incompleteWriteBuffer != null) {
      this.outputStream.write(this.incompleteWriteBuffer.getByteBuffer());
      this.incompleteWriteBuffer.release();
      this.incompleteWriteBuffer = null;
    }

    // Flush out the actions
    NetworkAction action;
    FLUSH_LOOP: while ((action = this.requestQueue.peek()) != null) {

      // Flush the action
      action.write(this);

      // Determine if requires response
      if (action.isRequireResponse()) {
        // Only wait on once
        if (this.lastAwaitingResult != action) {
          this.awaitingResults.add(action);
          this.lastAwaitingResult = action;
        }
      }

      // Determine if request blocks for further interaction
      if (action.isBlocking()) {
        break FLUSH_LOOP; // can not send further requests
      }

      // Request flushed, so attempt next request
      this.requestQueue.poll();
    }

    // Write data to network
    List<PooledByteBuffer> writtenBuffers = this.outputStream.getWrittenBuffers();
    for (int i = 0; i < writtenBuffers.size(); i++) {
      PooledByteBuffer pooledBuffer = writtenBuffers.get(i);
      ByteBuffer byteBuffer = pooledBuffer.getByteBuffer();

      // Write the buffer
      byteBuffer.flip();
      this.socketChannel.write(byteBuffer);
      if (byteBuffer.hasRemaining()) {
        // Socket buffer full (clear written buffers)
        this.incompleteWriteBuffer = pooledBuffer;
        this.outputStream.removeBuffers(i);
        this.context.setInterestedOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        return;
      }

      // Buffer written so release
      pooledBuffer.release();
    }

    // As here all data written
    writtenBuffers.clear();
    this.context.setInterestedOps(SelectionKey.OP_READ);
  }

  private NetworkAction nextAction = null;

  @Override
  public void handleRead() throws IOException {

    // TODO use pooled byte buffers
    ByteBuffer readBuffer = ByteBuffer.allocate(1024);

    try {

      // Consume data on the socket
      int bytesRead;
      while ((bytesRead = socketChannel.read(readBuffer)) > 0) {

        // Service the BE frames
        BEFrame frame;
        while ((frame = this.parser.parseBEFrame(readBuffer, 0, bytesRead)) != null) {

          // Obtain the awaiting request
          NetworkAction awaitingRequest = this.nextAction != null ? this.nextAction : this.awaitingResults.poll();

          // Provide frame to awaiting request
          this.beFrame = frame;
          this.nextAction = awaitingRequest.read(this);

          // Remove if blocking writing
          if (awaitingRequest == this.requestQueue.peek()) {
            this.requestQueue.poll();

            // Flag to write (as very likely have writes)
            this.context.writeRequired();
          }
        }
      }
      if (bytesRead < 0) {
        throw new ClosedChannelException();
      }
    } catch (NotYetConnectedException | ClosedChannelException ignore) {
      ignore.printStackTrace();
    }
  }

  @Override
  public void writeRequired() {
    this.context.writeRequired();
  }

  @Override
  public void handleException(Throwable ex) {
    // TODO Auto-generated method stub
  }

  /*
   * ========== NetworkRequestInitialiseContext ======================
   */

  @Override
  public SocketChannel getSocketChannel() {
    return this.socketChannel;
  }

  @Override
  public Map<ConnectionProperty, Object> getProperties() {
    return this.properties;
  }

  /*
   * ============ NetworkRequestReadContext ==========================
   */

  @Override
  public BEFrame getBEFrame() {
    return this.beFrame;
  }

  /*
   * ============ NetworkRequestWriteContext ==========================
   */

  @Override
  public OutputStream getOutputStream() {
    return this.outputStream;
  }

  /*
   * ============= Refactor out into NetworkRequests =================
   */

  @Deprecated // except notifications managed within network requests
  public void readPacket(BEFrame packet) {
    switch (packet.getTag()) {
    case AUTHENTICATION:
      doAuthentication(packet);
      break;
    case CANCELLATION_KEY_DATA:
      break;
    case BIND_COMPLETE:
      doBindComplete(packet);
      break;
    case CLOSE_COMPLETE:
      break;
    case COMMAND_COMPLETE:
      doCommandComplete(packet);
      break;
    case COPY_DATA:
      break;
    case COPY_DONE:
      break;
    case COPY_IN_RESPONSE:
      break;
    case COPY_OUT_RESPONSE:
      break;
    case COPY_BOTH_RESPONSE:
      break;
    case DATA_ROW:
      doDataRow(packet);
      break;
    case EMPTY_QUERY_RESPONSE:
      break;
    case ERROR_RESPONSE:
      doError(packet);
      break;
    case FUNCTION_CALL_RESPONSE:
      break;
    case NEGOTIATE_PROTOCOL_VERSION:
      break;
    case NO_DATA:
      break;
    case NOTICE_RESPONSE:
      break;
    case NOTIFICATION_RESPONSE:
      break;
    case PARAM_DESCRIPTION:
      break;
    case PARAM_STATUS:
      doParameterStatus(packet);
      break;
    case PARSE_COMPLETE:
      doParseComplete(packet);
      break;
    case PORTAL_SUSPENDED:
      break;
    case READY_FOR_QUERY:
      doReadyForQuery(packet);
      break;
    case ROW_DESCRIPTION:
      doRowDescription(packet);
      break;
    }
  }

  private void doRowDescription(BEFrame packet) {
    RowDescription rowDescription = new RowDescription(packet.getPayload());
    String portalName = descriptionNameQue.poll();
    preparedStatementCache.addDescriptionToPortal(portalName, rowDescription.getDescriptions());
  }

  private void doDataRow(BEFrame packet) {
    String portalName = sentSqlNameQue.peek();
    DataRow row = new DataRow(packet.getPayload(), preparedStatementCache.getDescription(portalName), rowNumber++);
    PGSubmission sub = awaitingResults.peek();

    if (sub == null) {
      throw new IllegalStateException(
          "Data Row packet arrived without an corresponding submission, internal state corruption");
    }

    sub.addRow(row);
  }

  private void doCommandComplete(BEFrame packet) {
    CommandComplete cc = new CommandComplete(packet.getPayload());

    PGSubmission sub = awaitingResults.peek();
    if (sub == null) {

      // TODO REMOVE
      if (true)
        return;

      throw new IllegalStateException(
          "Command Complete packet arrived without an corresponding submission, internal state corruption");
    }

    switch (sub.getCompletionType()) {
    case COUNT:
      sub.finish(new PGCount(cc.getNumberOfRowsAffected()));
      awaitingResults.poll();
      break;
    case ROW:
      sentSqlNameQue.poll();
      sub.finish(null);
      awaitingResults.poll();
      break;
    case CLOSE:
      sub.finish(socketChannel);
      awaitingResults.poll();
      break;
    case TRANSACTION:
      sub.finish(cc.getType());
      awaitingResults.poll();
      break;
    case ARRAY_COUNT:
      boolean allCollected = (Boolean) sub.finish(cc.getNumberOfRowsAffected());
      if (allCollected) {
        awaitingResults.poll();
      }
      break;
    case VOID:
      ((CompletableFuture) sub.getCompletionStage()).complete(null);
      awaitingResults.poll();
      break;
    case PROCESSOR:
      sub.finish(null);
      awaitingResults.poll();
      break;
    case OUT_PARAMETER:
      sub.finish(null);
      awaitingResults.poll();
      break;
    }

    currentState = ProtocolV3States.lookup(currentState, ProtocolV3States.Events.COMMAND_COMPLETE);
  }

  private void doBindComplete(BEFrame packet) {
    currentState = ProtocolV3States.lookup(currentState, ProtocolV3States.Events.BIND_COMPLETE);
  }

  private void doParseComplete(BEFrame packet) {
    currentState = ProtocolV3States.lookup(currentState, ProtocolV3States.Events.PARSE_COMPLETE);
  }

  private void doError(BEFrame packet) {
    ErrorResponse error = new ErrorResponse(packet.getPayload());

    StringBuilder message = new StringBuilder(
        "Severity: " + error.getField(SEVERITY) + "\nMessage: " + error.getField(MESSAGE));

    if (error.getField(DETAIL) != null)
      message.append("\nDetail: ").append(error.getField(DETAIL));
    if (error.getField(HINT) != null)
      message.append("\nHint: ").append(error.getField(HINT));

    PGSubmission<?> sub = awaitingResults.poll();

    if (sub == null) {
      throw new IllegalStateException("missing submission on queue, internal state corruption");
    }

    SqlException exception = new SqlException(message.toString(), null, error.getField(SQLSTATE_CODE), 0, sub.getSql(),
        0);

    if (sub.getErrorHandler() != null) {
      sub.getErrorHandler().accept(exception);
    }

    ((CompletableFuture) sub.getCompletionStage()).completeExceptionally(exception);
  }

  private void doReadyForQuery(BEFrame packet) {

    ReadyForQuery readyForQuery = new ReadyForQuery(packet.getPayload());

    // todo handle transaction stuff

    currentState = ProtocolV3States.lookup(currentState, ProtocolV3States.Events.READY_FOR_QUERY);
  }

  public void doParameterStatus(BEFrame packet) {
    ParameterStatus parameterStatus = new ParameterStatus(packet.getPayload());

    properties.put(PGConnectionProperties.lookup(parameterStatus.getName()), parameterStatus.getValue());
    currentState = ProtocolV3States.lookup(currentState, ProtocolV3States.Events.PARAMETER_STATUS);
  }

  public boolean isConnectionClosed() {
    return !socketChannel.isConnected();
  }

}
