package org.postgresql.sql2.buffer;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jdk.incubator.sql2.ConnectionProperty;

/**
 * Default {@link ByteBufferPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultByteBufferPool implements ByteBufferPool {

  /**
   * Pool of {@link PooledByteBuffer} instances.
   */
  private final Queue<PooledByteBuffer> pool = new ConcurrentLinkedQueue<>();

  /**
   * Size of buffers.
   */
  private final int bufferSize;

  /**
   * Instantiate.
   * 
   * @param properties Map of properties to configure this pool.
   */
  public DefaultByteBufferPool(Map<ConnectionProperty, Object> properties) {
    // TODO consider specifying buffer size from properties
    this.bufferSize = 8192; // largest 2 based size fitting jumbo ethernet packet
  }

  /*
   * ================= ByteBufferPool ======================
   */

  @Override
  public PooledByteBuffer getPooledByteBuffer() {

    // Obtain the next pooled buffer
    PooledByteBuffer buffer = pool.poll();
    if (buffer != null) {
      return buffer;
    }

    // No pooled, so create buffer
    // TODO consider blocking thread if too many active buffers to keep memory down
    return new PooledByteBufferImpl();
  }

  private class PooledByteBufferImpl implements PooledByteBuffer {

    private final ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);

    /*
     * =============== PooledByteBuffer ====================
     */

    @Override
    public ByteBuffer getByteBuffer() {
      return this.buffer;
    }

    @Override
    public void release() {
      pool.add(this);
    }
  }

}