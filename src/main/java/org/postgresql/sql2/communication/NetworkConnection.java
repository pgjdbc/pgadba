package org.postgresql.sql2.communication;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.postgresql.sql2.buffer.ByteBufferPool;
import org.postgresql.sql2.buffer.ByteBufferPoolOutputStream;
import org.postgresql.sql2.buffer.PooledByteBuffer;
import org.postgresql.sql2.execution.NioService;
import org.postgresql.sql2.execution.NioServiceContext;

import jdk.incubator.sql2.ConnectionProperty;

public class NetworkConnection implements NioService, NetworkConnectContext, NetworkWriteContext, NetworkReadContext {

  private final Map<ConnectionProperty, Object> properties;

  private final NioServiceContext context;

  private final ByteBufferPoolOutputStream outputStream;

  private final SocketChannel socketChannel;

  private final Queue<NetworkAction> priorityActionQueue = new LinkedList<>();

  private final Queue<NetworkAction> actionQueue = new ConcurrentLinkedQueue<>();

  private final Queue<NetworkAction> awaitingResults = new LinkedList<>();

  private final BEFrameParser parser = new BEFrameParser();

  private NetworkConnect connect = null;

  private boolean isWritingActive = false;

  public NetworkConnection(Map<ConnectionProperty, Object> properties, NioServiceContext context,
      ByteBufferPool bufferPool) {
    this.properties = properties;
    this.context = context;
    this.outputStream = new ByteBufferPoolOutputStream(bufferPool);
    this.socketChannel = (SocketChannel) context.getChannel();
  }

  public synchronized void networkConnect(NetworkConnect networkConnect) {

    // Ensure only one connect
    if (this.connect != null) {
      throw new IllegalStateException("Connection already being established");
    }
    this.connect = networkConnect;

    // Initialise the network request
    try {
      networkConnect.connect(this);
    } catch (IOException ex) {
      networkConnect.handleException(ex);
    }
  }

  public void addNetworkAction(NetworkAction networkAction) {

    // Ready network request for writing
    this.actionQueue.add(networkAction);
    this.context.writeRequired();
  }

  public boolean isConnectionClosed() {
    return !socketChannel.isConnected();
  }

  /*
   * =============== NioService =====================
   */

  @Override
  public synchronized void handleConnect() throws IOException {

    // Specify to write immediately
    NetworkAction initialAction = this.connect.finishConnect(this);

    // Load initial action to be undertaken first
    if (initialAction != null) {

      // Now able to write
      this.isWritingActive = true;

      // Run initial action
      Queue<NetworkAction> queue = new LinkedList<>();
      queue.add(initialAction);
      this.handleWrite(queue);
    }
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
    this.handleWrite(this.actionQueue);
  }

  /**
   * Flushes the {@link NetworkAction} instances to {@link PooledByteBuffer}
   * instances.
   * 
   * @param actions {@link Queue} of {@link NetworkAction} instances.
   * @return <code>true</code> if to block.
   * @throws IOException If fails to flush {@link NetworkAction} instances.
   */
  private boolean flushActions(Queue<NetworkAction> actions) throws IOException {

    // Flush out the actions
    NetworkAction action;
    while ((action = actions.peek()) != null) {

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
        return true; // can not send further requests
      }

      // Request flushed, so attempt next request
      actions.poll();
    }

    // As here, all flushed
    return false;
  }

  private void handleWrite(Queue<NetworkAction> requestActions) throws IOException {

    // Do no write unless active
    if (!this.isWritingActive) {
      return;
    }

    // Write in the incomplete write buffer (should always have space)
    if (this.incompleteWriteBuffer != null) {
      this.outputStream.write(this.incompleteWriteBuffer.getByteBuffer());
      if (this.incompleteWriteBuffer.getByteBuffer().hasRemaining()) {
        // Further writes required
        this.context.setInterestedOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        return;
      }
      this.incompleteWriteBuffer.release();
      this.incompleteWriteBuffer = null;
    }

    // Flush out the actions
    if (!this.flushActions(this.priorityActionQueue)) {
      this.flushActions(requestActions);
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

  private BEFrame beFrame = null;

  @Override
  public void handleRead() throws IOException {

    // TODO use pooled byte buffers
    ByteBuffer readBuffer = ByteBuffer.allocate(1024);

    int bytesRead = -1;
    boolean isWriteRequired = false;
    try {

      // Consume data on the socket
      while ((bytesRead = socketChannel.read(readBuffer)) > 0) {

        // Setup for consuming parts
        readBuffer.flip();
        int position = 0;

        // Service the BE frames
        BEFrame frame;
        while ((frame = this.parser.parseBEFrame(readBuffer, position, bytesRead)) != null) {
          position += this.parser.getConsumedBytes();

          // Obtain the awaiting request
          NetworkAction awaitingRequest = this.awaitingResults.poll();

          // Provide frame to awaiting request
          this.beFrame = frame;
          NetworkAction nextAction = awaitingRequest.read(this);

          // Include as next action
          if (nextAction != null) {
            this.priorityActionQueue.add(nextAction);
            isWriteRequired = true;
          }

          // Remove if blocking writing
          if (awaitingRequest == this.actionQueue.peek()) {
            this.actionQueue.poll();

            // Flag to write (as very likely have writes)
            isWriteRequired = true;
          }
        }

        // Clear buffer for re-use
        readBuffer.clear();
      }
    } catch (NotYetConnectedException | ClosedChannelException ignore) {
      ignore.printStackTrace();
      throw ignore;
    } finally {
      if (isWriteRequired) {
        this.context.writeRequired();
      }
    }
    if (bytesRead < 0) {
      throw new ClosedChannelException();
    }
  }

  @Override
  public void writeRequired() {
    this.context.writeRequired();
  }

  @Override
  public void handleException(Throwable ex) {
    // TODO consider how to handle exception
    ex.printStackTrace();
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
  public NetworkOutputStream getOutputStream() {
    return this.outputStream;
  }

}