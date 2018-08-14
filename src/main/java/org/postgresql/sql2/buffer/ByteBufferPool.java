package org.postgresql.sql2.buffer;

/**
 * Pool of {@link PooledByteBuffer} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ByteBufferPool {

  /**
   * Obtains the {@link PooledByteBuffer}.
   * 
   * @return {@link PooledByteBuffer}.
   */
  PooledByteBuffer getPooledByteBuffer();

}