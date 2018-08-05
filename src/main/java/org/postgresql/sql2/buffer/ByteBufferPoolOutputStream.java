/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.postgresql.sql2.buffer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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

  /**
   * Instantiate.
   * 
   * @param bufferPool {@link ByteBufferPool}.
   * @param charset    {@link Charset} encoding for text.
   */
  public ByteBufferPoolOutputStream(ByteBufferPool bufferPool) {
    this.bufferPool = bufferPool;

    // TODO possibly make charset configurable (if this is required)
    this.writer = new OutputStreamWriter(this, Charset.forName("UTF-8"));
  }

  /**
   * Obtains the list of written {@link PooledByteBuffer} instances.
   * 
   * @return List of written {@link PooledByteBuffer} instances.
   */
  public List<PooledByteBuffer> getWrittenBuffers() {
    return this.writtenByteBuffers;
  }

  /**
   * Removes specified number of {@link PooledByteBuffer} instances.
   * 
   * @param numberOfBuffers Number of {@link PooledByteBuffer} instances to remove
   *                        from front of list.
   */
  public void removeBuffers(int numberOfBuffers) {
    this.writtenByteBuffers.removeRange(0, numberOfBuffers);
  }

  /**
   * Convenience method to add a {@link PooledByteBuffer}.
   * 
   * @return {@link PooledByteBuffer}.
   */
  private PooledByteBuffer addWriteBuffer() {
    PooledByteBuffer buffer = this.bufferPool.getPooledByteBuffer();
    buffer.getByteBuffer().clear();
    this.writtenByteBuffers.add(buffer);
    return buffer;
  }

  /**
   * Obtains the current {@link PooledByteBuffer} to write further data.
   * 
   * @return Current {@link PooledByteBuffer} to write further data.
   */
  private PooledByteBuffer getCurrentBuffer() {

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

  /**
   * Writes the {@link ByteBuffer}.
   * 
   * @param byteBuffer {@link ByteBuffer}.
   */
  public void write(ByteBuffer byteBuffer) {
    this.getCurrentBuffer().getByteBuffer().put(byteBuffer);
  }

  /*
   * ===================== NetworkOutputStream ======================
   */

  @Override
  public void initPacket() throws IOException {

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

  @Override
  public void write(int b) throws IOException {
    this.getCurrentBuffer().getByteBuffer().put((byte) b);
    this.packetSize++;
  }

  @Override
  public void write(byte[] bytes, int off, int len) throws IOException {

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

  @Override
  public void write(String text) throws IOException {
    this.writer.write(text);
    this.writer.flush();
    this.writeTerminator();
  }

  @Override
  public void completePacket() {
    this.doCompletePacket(0, this.packetSize);
  }

  /**
   * Recursively writes packet length to avoid object creation.
   * 
   * @param depth  Current depth.
   * @param length Current length to write.
   */
  private void doCompletePacket(int depth, int length) {

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