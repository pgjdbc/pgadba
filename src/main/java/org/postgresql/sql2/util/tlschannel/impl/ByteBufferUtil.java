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

public class ByteBufferUtil {

  /**
   * copies bytebuffers.
   * @param src source
   * @param dst destination
   * @param length bytes to copy
   */
  public static void copy(ByteBuffer src, ByteBuffer dst, int length) {
    if (length < 0) {
      throw new IllegalArgumentException("negative length");
    }
    if (src.remaining() < length) {
      throw new IllegalArgumentException(
          String.format("source buffer does not have enough remaining capacity (%d < %d)", src.remaining(), length));
    }
    if (dst.remaining() < length) {
      throw new IllegalArgumentException(
          String.format("destination buffer does not have enough remaining capacity (%d < %d)", dst.remaining(), length));
    }
    if (length == 0) {
      return;
    }
    ByteBuffer tmp = src.duplicate();
    tmp.limit(src.position() + length);
    dst.put(tmp);
    src.position(src.position() + length);
  }

}
