package org.postgresql.sql2.buffer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.postgresql.sql2.communication.NetworkOutputStream;

/**
 * {@link OutputStream} that writes to {@link PooledByteBuffer} instances from a
 * {@link ByteBufferPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class ByteBufferPoolOutputStream extends NetworkOutputStream {

  /**
   * Place holder bytes written for the packet length.
   */
  private static final byte[] PACKET_LENGTH_PLACE_HOLDER = new byte[] { 0, 0, 0, 0 };

  /**
   * {@link ByteBufferPool}.
   */
  private final ByteBufferPool bufferPool;

  /**
   * {@link Writer} to write text.
   */
  private final Writer writer;

  /**
   * Written {@link PooledByteBuffer} instances.
   */
  private final BufferList writtenByteBuffers = new BufferList();

  /**
   * Index of the {@link PooledByteBuffer} containing the packet length.
   */
  private int packetStartBuffer = 0;

  /**
   * Position within the {@link PooledByteBuffer} for the packet length.
   */
  private int packetStartPosition = 0;

  /**
   * Size of the packet.
   */
  private int packetSize = 0;

  private volatile boolean closed = false;

  /**
   * Instantiate.
   * 
   * @param bufferPool {@link ByteBufferPool}.
   */
  public ByteBufferPoolOutputStream(ByteBufferPool bufferPool) {
    this.bufferPool = bufferPool;

    // TODO possibly make charset configurable (if this is required)
    this.writer = new OutputStreamWriter(this, StandardCharsets.UTF_8);
  }

  /**
   * Obtains the list of written {@link PooledByteBuffer} instances.
   * 
   * @return List of written {@link PooledByteBuffer} instances.
   */
  public PooledByteBuffer getNextWrittenBuffer() {
    synchronized (writtenByteBuffers) {
      if (writtenByteBuffers.isEmpty()) {
        return null;
      }

      PooledByteBuffer pbb = writtenByteBuffers.get(0);
      writtenByteBuffers.remove(0);
      return pbb;
    }
  }

  public boolean hasMoreToWrite() {
    return !writtenByteBuffers.isEmpty();
  }

  /**
   * Convenience method to add a {@link PooledByteBuffer}.
   * 
   * @return {@link PooledByteBuffer}.
   */
  private PooledByteBuffer addWriteBuffer() {
    synchronized (writtenByteBuffers) {
      PooledByteBuffer buffer = this.bufferPool.getPooledByteBuffer();
      buffer.getByteBuffer().clear();
      this.writtenByteBuffers.add(buffer);
      return buffer;
    }
  }

  /**
   * Obtains the current {@link PooledByteBuffer} to write further data.
   * 
   * @return Current {@link PooledByteBuffer} to write further data.
   */
  private PooledByteBuffer getCurrentBuffer() {
    synchronized (writtenByteBuffers) {

      // Ensure have current pooled buffer
      PooledByteBuffer buffer;
      if (this.writtenByteBuffers.size() == 0) {
        // First byte buffer
        buffer = this.addWriteBuffer();
      } else {
        // Obtain the last buffer
        buffer = this.writtenByteBuffers.get(this.writtenByteBuffers.size() - 1);

        // Ensure space in last buffer
        if (!buffer.getByteBuffer().hasRemaining()) {
          // No space, so add another buffer
          buffer = this.addWriteBuffer();
        }
      }

      // Return the buffer
      return buffer;
    }
  }

  /**
   * Writes the {@link ByteBuffer}.
   * 
   * @param byteBuffer {@link ByteBuffer}.
   */
  public void write(ByteBuffer byteBuffer) {
    synchronized (writtenByteBuffers) {
      this.getCurrentBuffer().getByteBuffer().put(byteBuffer);
    }
  }

  /*
   * ===================== NetworkOutputStream ======================
   */

  @Override
  public void initPacket() throws IOException {
    synchronized (writtenByteBuffers) {

      // Obtain the buffer to write packet length
      PooledByteBuffer pooledBuffer = this.getCurrentBuffer();
      ByteBuffer buffer = pooledBuffer.getByteBuffer();

      // Obtain the position of packet length
      this.packetStartBuffer = this.writtenByteBuffers.size() - 1;
      this.packetStartPosition = buffer.position();
      this.packetSize = 0;

      // Make space for place holder packet length bytes
      this.write(PACKET_LENGTH_PLACE_HOLDER);
    }
  }

  @Override
  public void write(int b) {
    synchronized (writtenByteBuffers) {
      this.getCurrentBuffer().getByteBuffer().put((byte) b);
      this.packetSize++;
    }
  }

  @Override
  public void write(byte[] bytes, int off, int len) {
    synchronized (writtenByteBuffers) {

      // Increasing packet size
      this.packetSize += (len - off);

      // Ensure have current pooled buffer
      PooledByteBuffer buffer = this.getCurrentBuffer();

      // Keep writing to buffers until complete
      do {

        // Determine bytes to write to buffer
        int available = buffer.getByteBuffer().remaining();
        int bytesToWrite = available < len ? available : len;

        // Write the bytes to buffer
        buffer.getByteBuffer().put(bytes, off, bytesToWrite);

        // Determine number of bytes remaining
        len -= bytesToWrite;

        // Adjust for potential another write
        if (len > 0) {
          off += bytesToWrite;
          buffer = this.addWriteBuffer();
        }

      } while (len > 0);
    }
  }

  @Override
  public void write(String text) throws IOException {
    synchronized (writtenByteBuffers) {
      this.writer.write(text);
      this.writer.flush();
      this.writeTerminator();
    }
  }

  @Override
  public void completePacket() {
    synchronized (writtenByteBuffers) {
      this.doCompletePacket(0, this.packetSize);
    }
  }

  @Override
  public void close() {
    closed = true;
  }

  /**
   * Recursively writes packet length to avoid object creation.
   * 
   * @param depth  Current depth.
   * @param length Current length to write.
   */
  private void doCompletePacket(int depth, int length) {
    synchronized (writtenByteBuffers) {

      // Drop out once written all bytes
      if (depth >= 4) {
        return;
      }

      // Obtain the byte length and remove for next length byte
      byte byteLengthValue = (byte) (length & 0xFF);
      length >>= 8;

      // Undertake recursion (so writes top bytes first)
      this.doCompletePacket(depth + 1, length);

      // Write the byte
      PooledByteBuffer pooledByteBuffer = this.writtenByteBuffers.get(this.packetStartBuffer);
      if (this.packetStartPosition >= pooledByteBuffer.getByteBuffer().capacity()) {
        // Writing past buffer end, so start with next buffer
        this.packetStartBuffer++;
        this.packetStartPosition = 0;
        pooledByteBuffer = this.writtenByteBuffers.get(this.packetStartBuffer);
      }
      pooledByteBuffer.getByteBuffer().put(this.packetStartPosition++, byteLengthValue);
    }
  }

  public boolean isClosed() {
    return closed;
  }

  /**
   * {@link PooledByteBuffer} {@link List}.
   */
  private static class BufferList extends ArrayList<PooledByteBuffer> {

    @Override
    public void removeRange(int fromIndex, int toIndex) {
      super.removeRange(fromIndex, toIndex);
    }
  }

}
