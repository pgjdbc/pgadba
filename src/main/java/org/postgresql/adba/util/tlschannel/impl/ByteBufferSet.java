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

package org.postgresql.adba.util.tlschannel.impl;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ByteBufferSet {

  public final ByteBuffer[] array;
  public final int offset;
  public final int length;

  /**
   * represents a group of ByteBuffers.
   * @param array the buffers
   * @param offset offset
   * @param length length
   */
  public ByteBufferSet(ByteBuffer[] array, int offset, int length) {
    if (array == null) {
      throw new NullPointerException();
    }
    if (array.length < offset) {
      throw new IndexOutOfBoundsException();
    }
    if (array.length < offset + length) {
      throw new IndexOutOfBoundsException();
    }
    for (int i = offset; i < offset + length; i++) {
      if (array[i] == null) {
        throw new NullPointerException();
      }
    }
    this.array = array;
    this.offset = offset;
    this.length = length;
  }

  public ByteBufferSet(ByteBuffer[] array) {
    this(array, 0, array.length);
  }

  public ByteBufferSet(ByteBuffer buffer) {
    this(new ByteBuffer[]{buffer});
  }

  /**
   * get the bytes remaining.
   * @return number of bytes
   */
  public long remaining() {
    long ret = 0;
    for (int i = offset; i < offset + length; i++) {
      ret += array[i].remaining();
    }
    return ret;
  }

  /**
   * copies bytes from supplied ByteBuffer into this one.
   * @param from to copy from.
   * @return number of bytes copied
   */
  public int putRemaining(ByteBuffer from) {
    int totalBytes = 0;
    for (int i = offset; i < offset + length; i++) {
      if (!from.hasRemaining()) {
        break;
      }
      ByteBuffer dstBuffer = array[i];
      int bytes = Math.min(from.remaining(), dstBuffer.remaining());
      ByteBufferUtil.copy(from, dstBuffer, bytes);
      totalBytes += bytes;
    }
    return totalBytes;
  }

  /**
   * copies bytes from supplied buffer.
   * @param from buffer to copy from.
   * @param length nr of bytes
   * @return nr of bytes
   */
  public ByteBufferSet put(ByteBuffer from, int length) {
    if (from.remaining() < length) {
      throw new IllegalArgumentException();
    }
    if (remaining() < length) {
      throw new IllegalArgumentException();
    }
    int totalBytes = 0;
    for (int i = offset; i < offset + this.length; i++) {
      int pending = length - totalBytes;
      if (pending == 0) {
        break;
      }
      int bytes = Math.min(pending, (int) remaining());
      ByteBuffer dstBuffer = array[i];
      ByteBufferUtil.copy(from, dstBuffer, bytes);
      totalBytes += bytes;
    }
    return this;
  }

  /**
   * fetches remaining bytes.
   * @param dst to copy into
   * @return nr of bytes copied
   */
  public int getRemaining(ByteBuffer dst) {
    int totalBytes = 0;
    for (int i = offset; i < offset + length; i++) {
      if (!dst.hasRemaining()) {
        break;
      }
      ByteBuffer srcBuffer = array[i];
      int bytes = Math.min(dst.remaining(), srcBuffer.remaining());
      ByteBufferUtil.copy(srcBuffer, dst, bytes);
      totalBytes += bytes;
    }
    return totalBytes;
  }

  /**
   * fetches remaining bytes.
   * @param dst to copy into
   * @param length length
   * @return nr of bytes copied
   */
  public ByteBufferSet get(ByteBuffer dst, int length) {
    if (remaining() < length) {
      throw new IllegalArgumentException();
    }
    if (dst.remaining() < length) {
      throw new IllegalArgumentException();
    }
    int totalBytes = 0;
    for (int i = offset; i < offset + this.length; i++) {
      int pending = length - totalBytes;
      if (pending == 0) {
        break;
      }
      ByteBuffer srcBuffer = array[i];
      int bytes = Math.min(pending, srcBuffer.remaining());
      ByteBufferUtil.copy(srcBuffer, dst, bytes);
      totalBytes += bytes;
    }
    return this;
  }

  public boolean hasRemaining() {
    return remaining() > 0;
  }

  /**
   * If all elements are readonly, return true.
   * @return if all elements are readonly, return true
   */
  public boolean isReadOnly() {
    for (int i = offset; i < offset + length; i++) {
      if (array[i].isReadOnly()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "ByteBufferSet[array=" + Arrays.toString(array) + ", offset=" + offset + ", length=" + length + "]";
  }

}
