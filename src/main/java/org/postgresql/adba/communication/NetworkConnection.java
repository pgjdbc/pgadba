package org.postgresql.adba.communication;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.net.ssl.SSLContext;
import jdk.incubator.sql2.SessionProperty;
import org.postgresql.adba.PgSession;
import org.postgresql.adba.PgSessionProperty;
import org.postgresql.adba.buffer.ByteBufferPool;
import org.postgresql.adba.buffer.ByteBufferPoolOutputStream;
import org.postgresql.adba.buffer.PooledByteBuffer;
import org.postgresql.adba.communication.network.CloseResponse;
import org.postgresql.adba.communication.packets.ErrorPacket;
import org.postgresql.adba.execution.NioLoop;
import org.postgresql.adba.execution.NioService;
import org.postgresql.adba.execution.NioServiceContext;
import org.postgresql.adba.util.tlschannel.ClientTlsChannel;
import org.postgresql.adba.util.tlschannel.NeedsReadException;
import org.postgresql.adba.util.tlschannel.NeedsWriteException;
import org.postgresql.adba.util.tlschannel.TlsChannel;

public class NetworkConnection implements NioService, NetworkConnectContext, NetworkWriteContext, NetworkReadContext {

  private final Map<SessionProperty, Object> properties;

  private final PgSession connection;

  private final NioLoop loop;

  private final ByteBufferPoolOutputStream outputStream;

  private final Queue<NetworkRequest> priorityRequestQueue = new LinkedList<>();

  private final Queue<NetworkRequest> requestQueue = new ConcurrentLinkedQueue<>();

  private final Queue<NetworkResponse> awaitingResponses = new LinkedList<>();

  private final BeFrameParser parser = new BeFrameParser();

  private final PreparedStatementCache preparedStatementCache = new PreparedStatementCache();

  private NetworkConnect connect = null;

  private SocketChannel socketChannel;

  private TlsChannel tlsChannel;

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
      throw new IllegalStateException("Should not read until connected", ex);
    }
  };

  /**
   * Instantiate.
   * 
   * @param properties Connection properties.
   * @param connection {@link PgSession}.
   * @param loop       {@link NioLoop}.
   * @param bufferPool {@link ByteBufferPool}.
   */
  public NetworkConnection(Map<SessionProperty, Object> properties, PgSession connection, NioLoop loop,
      ByteBufferPool bufferPool) {
    this.properties = properties;
    this.connection = connection;
    this.loop = loop;
    outputStream = new ByteBufferPoolOutputStream(bufferPool);
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
    if (connect != null) {
      throw new IllegalStateException("Connection already being established");
    }
    connect = networkConnect;

    // Initialise the network request
    try {

      // Register the connection
      socketChannel = SocketChannel.open();
      socketChannel.configureBlocking(false);
      if((boolean) properties.getOrDefault(PgSessionProperty.TCP_KEEP_ALIVE, false)) {
        socketChannel.socket().setKeepAlive(true);
      }
      loop.registerNioService(socketChannel, (context) -> {
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
    requestQueue.add(request);
    context.writeRequired();
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

    if (connect == null) {
      throw new IllegalStateException("No " + NetworkConnect.class.getSimpleName() + " to handle connect");
    }

    // Specify to write immediately
    NetworkRequest initialRequest = connect.finishConnect(this);

    // As connected, may now start writing
    blockingResponse = null;

    // Load initial action to be undertaken first
    if (initialRequest != null) {

      // Run initial request
      Queue<NetworkRequest> queue = new LinkedList<>();
      queue.add(initialRequest);
      handleWrite(queue);
    }
  }

  @Override
  public void handleWrite() throws Exception {
    handleWrite(requestQueue);
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
          awaitingResponses.add(response);
        }

        // Determine if request blocks for further interaction
        if (request.isBlocking()) {
          blockingResponse = response;
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
    if (blockingResponse == null) {

      // Flush out the requests (doing priority queue first)
      if (!flushRequests(priorityRequestQueue)) {
        flushRequests(requests);
      }
    }

    // Write the previous incomplete write buffer
    if (incompleteWriteBuffer != null) {
      if (tlsChannel == null) {
        socketChannel.write(incompleteWriteBuffer.getByteBuffer());
      } else {
        try {
          tlsChannel.write(incompleteWriteBuffer.getByteBuffer());
        } catch (NeedsReadException e) {
          context.setInterestedOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } catch (NeedsWriteException e) {
          isWriteRequired = true;
        }
      }
      if (incompleteWriteBuffer.getByteBuffer().hasRemaining()) {
        // Further writes required
        context.setInterestedOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        return;
      }
      incompleteWriteBuffer.release();
      incompleteWriteBuffer = null;
    }

    // Write data to network
    PooledByteBuffer pooledBuffer = outputStream.getNextWrittenBuffer();
    if (pooledBuffer == null) {
      checkIfCloseAndPerformClose();
      if (requests.size() == 0) {
        context.setInterestedOps(SelectionKey.OP_READ);
      }
      return;
    }
    ByteBuffer byteBuffer = pooledBuffer.getByteBuffer();

    // Write the buffer
    byteBuffer.flip();
    if (tlsChannel == null) {
      socketChannel.write(byteBuffer);
    } else {
      try {
        tlsChannel.write(byteBuffer);
      } catch (NeedsReadException e) {
        context.setInterestedOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
      } catch (NeedsWriteException e) {
        isWriteRequired = true;
      }
    }
    if (byteBuffer.hasRemaining()) {
      // Socket buffer full (clear written buffers)
      incompleteWriteBuffer = pooledBuffer;
      context.setInterestedOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
      return;
    }

    // Buffer written so release
    pooledBuffer.release();

    // As here all data written
    if (outputStream.hasMoreToWrite() || requests.size() != 0) {
      context.setInterestedOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    } else if (outputStream.isClosed()) {
      checkIfCloseAndPerformClose();
    } else {
      context.setInterestedOps(SelectionKey.OP_READ);
    }
  }

  private void checkIfCloseAndPerformClose() throws IOException {
    if (outputStream.isClosed() && awaitingResponses.size() == 1) {
      if (tlsChannel != null) {
        tlsChannel.close();
      } else {
        socketChannel.close();
      }
      if (awaitingResponses.peek() instanceof CloseResponse) {
        NetworkResponse response = awaitingResponses.poll();
        if (response != null) {
          response.read(null);
        }
      }
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
    isWriteRequired = false;
    try {

      // Consume data on the socket
      while (tlsChannel == null ? (bytesRead = socketChannel.read(readBuffer)) > 0
          : (bytesRead = tlsChannel.read(readBuffer)) > 0) {

        // Setup for consuming parts
        readBuffer.flip();
        int position = 0;

        // Service the BE frames
        BeFrame frame;
        while ((frame = parser.parseBeFrame(readBuffer, position, bytesRead)) != null) {
          position += parser.getConsumedBytes();

          // Obtain the awaiting response
          NetworkResponse awaitingResponse = getAwaitingResponse();

          // Ensure have awaiting response
          if (awaitingResponse == null) {
            throw new IllegalStateException(
                "No awaiting " + NetworkResponse.class.getSimpleName() + " for tag " + frame.getTag());
          }

          // Handle frame
          switch (frame.getTag()) {
            case ERROR_RESPONSE:
              // Handle error
              immediateResponse = awaitingResponse.handleException(new ErrorPacket(frame.getPayload()));
              break;

            default:
              // Provide frame to awaiting response
              beFrame = frame;
              immediateResponse = awaitingResponse.read(this);
          }

          // Remove if blocking writing
          if (awaitingResponse == blockingResponse) {
            blockingResponse = null;

            // Flag to write (as very likely have writes)
            isWriteRequired = true;
          }
        }

        // Clear buffer for re-use
        readBuffer.clear();
      }
    } catch (NeedsReadException e) {
      context.setInterestedOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    } catch (NeedsWriteException e) {
      isWriteRequired = true;
    } catch (NotYetConnectedException | ClosedChannelException ignore) {
      ignore.printStackTrace();
      throw ignore;
    } finally {
      if (isWriteRequired) {
        context.writeRequired();
      }
    }
    if (tlsChannel == null && bytesRead < 0) {
      throw new ClosedChannelException();
    }

    checkIfCloseAndPerformClose();
  }

  @Override
  public void handleException(Throwable ex) {

    // Unregister the connection (as closed)
    connection.unregister();

    // Ignore close exception
    if (!(ex instanceof ClosedChannelException)) {
      // TODO consider how to handle exception
      ex.printStackTrace();
    }

    // Close the connection (if open)
    if (socketChannel.isOpen()) {
      try {
        socketChannel.close();
        context.unregister();
      } catch (IOException closeEx) {

        // TODO consider handle close exception
        closeEx.printStackTrace();
      }
    }
    if (tlsChannel != null && tlsChannel.isOpen()) {
      try {
        tlsChannel.close();
        context.unregister();
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
    return socketChannel;
  }

  @Override
  public Map<SessionProperty, Object> getProperties() {
    return properties;
  }

  @Override
  public void startTls() {
    try {
      ClientTlsChannel.Builder builder = ClientTlsChannel.newBuilder(socketChannel, SSLContext.getDefault());
      tlsChannel = builder.build();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }

  /*
   * ============ NetworkRequestReadContext ==========================
   */

  @Override
  public BeFrame getBeFrame() {
    return beFrame;
  }

  @Override
  public void write(NetworkRequest request) {
    priorityRequestQueue.add(request);
    isWriteRequired = true;
  }

  @Override
  public void writeRequired() {
    isWriteRequired = true;
  }

  /*
   * ============ NetworkRequestWriteContext ==========================
   */

  @Override
  public NetworkOutputStream getOutputStream() {
    return outputStream;
  }

  @Override
  public PreparedStatementCache getPreparedStatementCache() {
    return preparedStatementCache;
  }

  @Override
  public void setProperty(SessionProperty property, Object value) {
    properties.put(property, value);
  }

}