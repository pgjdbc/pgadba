/*
MIT License

Copyright (c) [2015-2018] all contributors of https://github.com/marianobarrios/tls-channel, Alexander Kjäll

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package org.postgresql.sql2.util.tlschannel.impl;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.postgresql.sql2.util.tlschannel.BufferAllocator;

public class BufferHolder {

  private static final Logger logger = Logger.getLogger(BufferHolder.class.getName());
  private static final byte[] zeros = new byte[TlsChannelImpl.maxTlsPacketSize];

  public final String name;
  public final BufferAllocator allocator;
  public final boolean plainData;
  public final int maxSize;
  public final boolean opportunisticDispose;

  public ByteBuffer buffer;
  public int lastSize;

  /**
   * Wraps a ByteBuffer.
   * @param name name
   * @param buffer buffer
   * @param allocator allocator
   * @param initialSize initialSize
   * @param maxSize maxSize
   * @param plainData plainData
   * @param opportunisticDispose opportunisticDispose
   */
  public BufferHolder(String name, Optional<ByteBuffer> buffer, BufferAllocator allocator, int initialSize, int maxSize,
      boolean plainData, boolean opportunisticDispose) {
    this.name = name;
    this.allocator = allocator;
    this.buffer = buffer.orElse(null);
    this.maxSize = maxSize;
    this.plainData = plainData;
    this.opportunisticDispose = opportunisticDispose;
    this.lastSize = buffer.map(b -> b.capacity()).orElse(initialSize);
  }

  /**
   * allocates space.
   */
  public void prepare() {
    if (buffer == null) {
      buffer = allocator.allocate(lastSize);
    }
  }

  /**
   * disposes of the buffer if appropriate.
   * @return if it was released
   */
  public boolean release() {
    if (opportunisticDispose && buffer.position() == 0) {
      return dispose();
    } else {
      return false;
    }
  }

  /**
   * disposes of the buffer.
   * @return if it was released
   */
  public boolean dispose() {
    if (buffer != null) {
      allocator.free(buffer);
      buffer = null;
      return true;
    } else {
      return false;
    }
  }

  /**
   * resizes the buffer.
   * @param newCapacity size to resize to
   */
  public void resize(int newCapacity) {
    if (newCapacity > maxSize) {
      throw new IllegalArgumentException(
          String.format("new capacity (%s) bigger than absolute max size (%s)", newCapacity, maxSize));
    }
    logger.log(Level.INFO,
        "resizing buffer " + name + ", increasing from " + buffer.capacity() + " to " + newCapacity + " (manual sizing)");
    resizeImpl(newCapacity);
  }

  /**
   * increase size.
   */
  public void enlarge() {
    if (buffer.capacity() >= maxSize) {
      throw new IllegalStateException(
          String.format("%s buffer insufficient despite having capacity of %d", name, buffer.capacity()));
    }
    int newCapacity = Math.min(buffer.capacity() * 2, maxSize);
    logger.log(Level.INFO,
        "enlarging buffer " + name + ", increasing from " + buffer.capacity() + " to " + newCapacity
            + " (automatic enlarge)");
    resizeImpl(newCapacity);
  }

  private void resizeImpl(int newCapacity) {
    ByteBuffer newBuffer = allocator.allocate(newCapacity);
    buffer.flip();
    newBuffer.put(buffer);
    if (plainData) {
      zero();
    }
    allocator.free(buffer);
    buffer = newBuffer;
    lastSize = newCapacity;
  }

  /**
   * Fill with zeros the remaining of the supplied buffer. This method does not change the buffer position.
   *
   * <p>Typically used for security reasons, with buffers that contains now-unused plaintext.</p>
   */
  public void zeroRemaining() {
    buffer.mark();
    buffer.put(zeros, 0, buffer.remaining());
    buffer.reset();
  }

  /**
   * Fill the buffer with zeros. This method does not change the buffer position.
   *
   * <p>Typically used for security reasons, with buffers that contains now-unused plaintext.</p>
   */
  public void zero() {
    buffer.mark();
    buffer.position(0);
    buffer.put(zeros, 0, buffer.remaining());
    buffer.reset();
  }

  public boolean nullOrEmpty() {
    return buffer == null || buffer.position() == 0;
  }

}
