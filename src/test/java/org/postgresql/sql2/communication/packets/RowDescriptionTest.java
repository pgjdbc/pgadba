package org.postgresql.sql2.communication.packets;

import org.junit.jupiter.api.Test;
import org.postgresql.sql2.buffer.PooledByteBuffer;
import org.postgresql.sql2.communication.NetworkInputStream;
import org.postgresql.sql2.communication.packets.parts.ColumnTypes;
import org.postgresql.sql2.communication.packets.parts.FormatCodeTypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

public class RowDescriptionTest {

  @Test
  public void describeSelect1() throws IOException {
    byte[] bytes = new byte[] {0x00, 0x01, 0x74, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x17, 0x00,
        0x04, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 0x00, 0x00};

    // Load the data
    ByteBuffer bb = ByteBuffer.allocate(1024);
    bb.put(bytes);
    NetworkInputStream input = new NetworkInputStream();
    input.appendBuffer(new PooledByteBuffer() {      
      @Override
      public ByteBuffer getByteBuffer() {
        return bb;
      }
      
      @Override
      public void release() {
      }

    }, 0, bytes.length, false);

    RowDescription instance = new RowDescription(input);

    assertEquals(1, instance.getDescriptions().length);
    assertEquals("t", instance.getDescriptions()[0].getName());
    assertEquals(0, instance.getDescriptions()[0].getObjectIdOfTable());
    assertEquals(0, instance.getDescriptions()[0].getAttributeNumberOfColumn());
    assertEquals(ColumnTypes.INT4, instance.getDescriptions()[0].getColumnType());
    assertEquals(4, instance.getDescriptions()[0].getDataTypeSize());
    assertEquals(-1, instance.getDescriptions()[0].getTypeModifier());
    assertEquals(FormatCodeTypes.TEXT, instance.getDescriptions()[0].getFormatCode());
  }
}