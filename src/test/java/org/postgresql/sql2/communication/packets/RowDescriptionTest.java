package org.postgresql.sql2.communication.packets;

import org.junit.jupiter.api.Test;
import org.postgresql.sql2.communication.packets.parts.ColumnTypes;
import org.postgresql.sql2.communication.packets.parts.FormatCodeTypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RowDescriptionTest {

  @Test
  public void describeSelect1() {
    byte[] bytes = new byte[] {0x00, 0x01, 0x74, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x17, 0x00,
        0x04, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 0x00, 0x00};

    RowDescription instance = new RowDescription(bytes);

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