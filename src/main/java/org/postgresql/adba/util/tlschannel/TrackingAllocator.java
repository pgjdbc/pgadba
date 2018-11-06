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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * A decorating {@link BufferAllocator} that keeps statistics.
 */
public class TrackingAllocator implements BufferAllocator {

  private BufferAllocator impl;

  private LongAdder bytesAllocatedAdder = new LongAdder();
  private LongAdder bytesDeallocatedAdder = new LongAdder();
  private AtomicLong currentAllocationSize = new AtomicLong();
  private LongAccumulator maxAllocationSizeAcc = new LongAccumulator(Math::max, 0);

  private LongAdder buffersAllocatedAdder = new LongAdder();
  private LongAdder buffersDeallocatedAdder = new LongAdder();

  public TrackingAllocator(BufferAllocator impl) {
    this.impl = impl;
  }

  /**
   * Allocates a buffer.
   * @param size size to allocate
   * @return the buffer
   */
  public ByteBuffer allocate(int size) {
    bytesAllocatedAdder.add(size);
    currentAllocationSize.addAndGet(size);
    buffersAllocatedAdder.increment();
    return impl.allocate(size);
  }

  /**
   * Frees the allocated buffer.
   * @param buffer the buffer to deallocate, that should have been allocated using the same {@link BufferAllocator} instance
   */
  public void free(ByteBuffer buffer) {
    int size = buffer.capacity();
    bytesDeallocatedAdder.add(size);
    maxAllocationSizeAcc.accumulate(currentAllocationSize.longValue());
    currentAllocationSize.addAndGet(-size);
    buffersDeallocatedAdder.increment();
    impl.free(buffer);
  }

  public long bytesAllocated() {
    return bytesAllocatedAdder.longValue();
  }

  public long bytesDeallocated() {
    return bytesDeallocatedAdder.longValue();
  }

  public long currentAllocation() {
    return currentAllocationSize.longValue();
  }

  public long maxAllocation() {
    return maxAllocationSizeAcc.longValue();
  }

  public long buffersAllocated() {
    return buffersAllocatedAdder.longValue();
  }

  public long buffersDeallocated() {
    return buffersDeallocatedAdder.longValue();
  }
}
