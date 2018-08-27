package org.postgresql.sql2.communication;

import org.postgresql.sql2.communication.packets.parts.ColumnDescription;

public class TableCell {
  private byte[] bytes;
  private ColumnDescription columnDescription;

  public TableCell(byte[] bytes, ColumnDescription columnDescription) {
    this.bytes = bytes;
    this.columnDescription = columnDescription;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public ColumnDescription getColumnDescription() {
    return columnDescription;
  }
}
