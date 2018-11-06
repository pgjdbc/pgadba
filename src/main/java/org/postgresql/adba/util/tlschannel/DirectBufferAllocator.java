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

package org.postgresql.adba.util.tlschannel;

import java.nio.ByteBuffer;

/**
 * Allocator that creates direct buffers. The {@link #free(ByteBuffer)} method, if called, deallocates the buffer immediately,
 * without having to wait for GC (and the finalizer) to run. Calling {@link #free(ByteBuffer)} is actually optional, but should
 * result in reduced memory consumption.
 *
 * <p>Direct buffers are generally preferred for using with I/O, to avoid an extra user-space copy, or to reduce garbage
 * collection overhead.</p>
 */
public class DirectBufferAllocator implements BufferAllocator {

  //private DeallocationHelper deallocationHelper = new DeallocationHelper();

  @Override
  public ByteBuffer allocate(int size) {
    return ByteBuffer.allocateDirect(size);
  }

  @Override
  public void free(ByteBuffer buffer) {
    // do not wait for GC (and finalizer) to run
    //deallocationHelper.deallocate(buffer);
  }

}
