package org.postgresql.sql2.util;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BinaryHelperTest {

  @Test
  public void combineBuffers() {
    ByteBuffer bb1 = ByteBuffer.allocate(100);
    ByteBuffer bb2 = ByteBuffer.allocate(100);

    bb1.put((byte) 'a');
    bb2.put((byte) 'b');

    bb1.flip();
    bb2.flip();
    ByteBuffer result = BinaryHelper.combineBuffers(bb1, bb2);

    result.flip();
    byte[] b = new byte[2];
    result.get(b);

    assertEquals((byte) 'a', b[0]);
    assertEquals((byte) 'b', b[1]);
  }
}