package org.postgresql.adba.buffer;

import java.nio.ByteBuffer;

/**
 * Pooled {@link ByteBuffer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface PooledByteBuffer {

  /**
   * Obtains the {@link ByteBuffer}.
   * 
   * @return {@link ByteBuffer}.
   */
  ByteBuffer getByteBuffer();

  /**
   * Releases this back to the pool.
   */
  void release();

}