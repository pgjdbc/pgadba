package org.postgresql.sql2.communication;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import jdk.incubator.sql2.ConnectionProperty;
import org.postgresql.sql2.PgConnection;
import org.postgresql.sql2.buffer.ByteBufferPool;
import org.postgresql.sql2.buffer.ByteBufferPoolOutputStream;
import org.postgresql.sql2.buffer.PooledByteBuffer;
import org.postgresql.sql2.communication.packets.ErrorPacket;
import org.postgresql.sql2.execution.NioLoop;
import org.postgresql.sql2.execution.NioService;
import org.postgresql.sql2.execution.NioServiceContext;

public class NetworkConnection implements NioService, NetworkConnectContext, NetworkWriteContext, NetworkReadContext {

  private static ClosedChannelException CLOSE_EXCEPTION = new ClosedChannelException();

  private final Map<ConnectionProperty, Object> properties;

  private final PgConnection connection;

  private final NioLoop loop;

  private final ByteBufferPoolOutputStream outputStream;

  private final Queue<NetworkRequest> priorityRequestQueue = new LinkedList<>();

  private final Queue<NetworkRequest> requestQueue = new ConcurrentLinkedQueue<>();

  private final Queue<NetworkResponse> awaitingResponses = new LinkedList<>();

  private final BeFrameParser parser = new BeFrameParser();

  private final PreparedStatementCache preparedStatementCache = new PreparedStatementCache();

  private NetworkConnect connect = null;

  private SocketChannel socketChannel;

  private NioServiceContext context = null;

  /**
   * Possible blocking {@link NetworkResponse}.
   */
  private NetworkResponse blockingResponse = new NetworkResponse() {
    @Override
    public NetworkResponse read(NetworkReadContext context) throws IOException {
      throw new IllegalStateException("Should not read until connected");
    }

    @Override
    public NetworkResponse handleException(Throwable ex) {
      throw new IllegalStateException("Should not read until connected");
    }
  };

  /**
   * Instantiate.
   * 
   * @param properties Connection properties.
   * @param connection {@link PgConnection}.
   * @param loop       {@link NioLoop}.
   * @param bufferPool {@link ByteBufferPool}.
   */
  public NetworkConnection(Map<ConnectionProperty, Object> properties, PgConnection connection, NioLoop loop,
      ByteBufferPool bufferPool) {
    this.properties = properties;
    this.connection = connection;
    this.loop = loop;
    this.outputStream = new ByteBufferPoolOutputStream(bufferPool);
  }

  /**
   * Sends the {@link NetworkConnect}.
   * 
   * @param networkConnect {@link NetworkConnect}.
   */
  public synchronized void sendNetworkConnect(NetworkConnect networkConnect) {

    // Synchronizes with handleConnect so service thread has correct state
    // (Connections should be long running so low impact)

    // Ensure only one connect
    if (this.connect != null) {
      throw new IllegalStateException("Connection already being established");
    }
    this.connect = networkConnect;

    // Initialise the network request
    try {

      // Register the connection
      this.socketChannel = SocketChannel.open();
      this.socketChannel.configureBlocking(false);
      this.loop.registerNioService(this.socketChannel, (context) -> {
        this.context = context;
        return this;
      });

      // Undertake connect
      networkConnect.connect(this);

    } catch (IOException ex) {
      networkConnect.handleException(ex);
    }
  }

  /**
   * Sends the {@link NetworkRequest}.
   * 
   * @param request {@link NetworkRequest}.
   */
  public void sendNetworkRequest(NetworkRequest request) {

    // Ready network request for writing
    this.requestQueue.add(request);
    this.context.writeRequired();
  }

  /**
   * Indicates if the connection is closed.
   * 
   * @return <code>true</code> if the connection is closed.
   */
  public boolean isConnectionClosed() {
    return !socketChannel.isConnected();
  }

  /*
   * =============== NioService =====================
   */

  @Override
  public synchronized void handleConnect() throws Exception {

    if (this.connect == null) {
      throw new IllegalStateException("No " + NetworkConnect.class.getSimpleName() + " to handle connect");
    }

    // Specify to write immediately
    NetworkRequest initialRequest = this.connect.finishConnect(this);

    // As connected, may now start writing
    this.blockingResponse = null;

    // Load initial action to be undertaken first
    if (initialRequest != null) {

      // Run initial request
      Queue<NetworkRequest> queue = new LinkedList<>();
      queue.add(initialRequest);
      this.handleWrite(queue);
    }
  }

  @Override
  public void handleWrite() throws Exception {
    this.handleWrite(this.requestQueue);
  }

  /**
   * Flushes the {@link NetworkRequest} instances to {@link PooledByteBuffer}
   * instances.
   * 
   * @param requests {@link Queue} of {@link NetworkRequest} instances.
   * @return <code>true</code> if to block.
   * @throws Exception If fails to flush {@link NetworkRequest} instances.
   */
  private boolean flushRequests(Queue<NetworkRequest> requests) throws Exception {

    // Flush out the request
    NetworkRequest request;
    while ((request = requests.poll()) != null) {

      // Flush the request
      NetworkRequest nextRequest;
      do {
        nextRequest = request.write(this);

        // Determine if requires response
        NetworkResponse response = request.getRequiredResponse();
        if (response != null) {
          this.awaitingResponses.add(response);
        }

        // Determine if request blocks for further interaction
        if (request.isBlocking()) {
          this.blockingResponse = response;
          return true; // can not send further requests
        }

        // Loop until all next requests flushed
        request = nextRequest;
      } while (request != null);
    }

    // As here, all flushed with no blocking
    return false;
  }

  /**
   * Possible previous incomplete {@link PooledByteBuffer} not completely written.
   */
  private PooledByteBuffer incompleteWriteBuffer = null;

  /**
   * Handles writing the {@link NetworkRequest} instances.
   * 
   * @param requests {@link Queue} of {@link NetworkRequest} instances.
   * @throws Exception If fails to write the {@link NetworkRequest} instances.
   */
  private void handleWrite(Queue<NetworkRequest> requests) throws Exception {

    // Only flush further requests if no blocking response
    if (this.blockingResponse == null) {

      // Flush out the requests (doing priority queue first)
      if (!this.flushRequests(this.priorityRequestQueue)) {
        this.flushRequests(requests);
      }
    }

    // Write the previous incomplete write buffer
    if (this.incompleteWriteBuffer != null) {
      this.socketChannel.write(this.incompleteWriteBuffer.getByteBuffer());
      if (this.incompleteWriteBuffer.getByteBuffer().hasRemaining()) {
        // Further writes required
        this.context.setInterestedOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        return;
      }
      this.incompleteWriteBuffer.release();
      this.incompleteWriteBuffer = null;
    }

    // Write data to network
    PooledByteBuffer pooledBuffer = this.outputStream.getNextWrittenBuffer();
    if (pooledBuffer == null) {
      //System.out.println("pooledBuffer = " + null);
      this.context.setInterestedOps(SelectionKey.OP_READ);
      return;
    }
    ByteBuffer byteBuffer = pooledBuffer.getByteBuffer();

    // Write the buffer
    byteBuffer.flip();
    this.socketChannel.write(byteBuffer);
    if (byteBuffer.hasRemaining()) {
      // Socket buffer full (clear written buffers)
      this.incompleteWriteBuffer = pooledBuffer;
      this.context.setInterestedOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
      return;
    }

    // Buffer written so release
    pooledBuffer.release();

    // As here all data written
    if (outputStream.hasMoreToWrite()) {
      this.context.setInterestedOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    } else {
      this.context.setInterestedOps(SelectionKey.OP_READ);
    }
  }

  /**
   * {@link BeFrame} for {@link NetworkReadContext}.
   */
  private BeFrame beFrame = null;

  /**
   * Allows {@link NetworkReadContext} to specify if write required.
   */
  private boolean isWriteRequired = false;

  /**
   * Immediate {@link NetworkResponse}.
   */
  private NetworkResponse immediateResponse = null;

  /**
   * Obtains the awaiting {@link NetworkResponse}.
   * 
   * @return Awaiting {@link NetworkResponse}.
   */
  private NetworkResponse getAwaitingResponse() {
    NetworkResponse awaitingResponse;
    if (immediateResponse != null) {
      awaitingResponse = immediateResponse;
      immediateResponse = null;
    } else {
      awaitingResponse = awaitingResponses.poll();
    }
    return awaitingResponse;
  }

  @Override
  public void handleRead() throws IOException {

    // TODO use pooled byte buffers
    ByteBuffer readBuffer = ByteBuffer.allocate(1024);

    // Reset for reads
    int bytesRead = -1;
    this.isWriteRequired = false;
    try {

      // Consume data on the socket
      while ((bytesRead = this.socketChannel.read(readBuffer)) > 0) {

        // Setup for consuming parts
        readBuffer.flip();
        int position = 0;

        // Service the BE frames
        BeFrame frame;
        while ((frame = this.parser.parseBeFrame(readBuffer, position, bytesRead)) != null) {
          position += this.parser.getConsumedBytes();

          // Obtain the awaiting response
          NetworkResponse awaitingResponse = this.getAwaitingResponse();

          // Ensure have awaiting response
          if (awaitingResponse == null) {
            throw new IllegalStateException(
                "No awaiting " + NetworkResponse.class.getSimpleName() + " for tag " + frame.getTag());
          }

          // Handle frame
          switch (frame.getTag()) {
            case ERROR_RESPONSE:
              // Handle error
              this.immediateResponse = awaitingResponse.handleException(new ErrorPacket(frame.getPayload()));
              break;

            default:
              // Provide frame to awaiting response
              this.beFrame = frame;
              this.immediateResponse = awaitingResponse.read(this);
          }

          // Remove if blocking writing
          if (awaitingResponse == this.blockingResponse) {
            this.blockingResponse = null;

            // Flag to write (as very likely have writes)
            this.isWriteRequired = true;
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
      throw CLOSE_EXCEPTION;
    }
  }

  @Override
  public void handleException(Throwable ex) {

    // Unregister the connection (as closed)
    this.connection.unregister();

    // Ignore close exception
    if (ex != CLOSE_EXCEPTION) {
      // TODO consider how to handle exception
      ex.printStackTrace();
    }

    // Close the connection (if open)
    if (this.socketChannel.isOpen()) {
      try {
        this.socketChannel.close();
        this.context.unregister();
      } catch (IOException closeEx) {

        // TODO consider handle close exception
        closeEx.printStackTrace();
      }
    }
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
  public BeFrame getBeFrame() {
    return this.beFrame;
  }

  @Override
  public void write(NetworkRequest request) {
    this.priorityRequestQueue.add(request);
    this.isWriteRequired = true;
  }

  @Override
  public void writeRequired() {
    this.isWriteRequired = true;
  }

  /*
   * ============ NetworkRequestWriteContext ==========================
   */

  @Override
  public NetworkOutputStream getOutputStream() {
    return this.outputStream;
  }

  @Override
  public PreparedStatementCache getPreparedStatementCache() {
    return this.preparedStatementCache;
  }

  @Override
  public void setProperty(ConnectionProperty property, Object value) {
    this.properties.put(property, value);
  }

}