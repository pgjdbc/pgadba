package org.postgresql.sql2.communication;

import org.postgresql.sql2.communication.packets.parts.ColumnDescription;

public class TableCell {
  private byte[] bytes;
  private int start;
  private int stop;
  private ColumnDescription columnDescription;

  public TableCell(byte[] bytes, int start, int stop, ColumnDescription columnDescription) {
    this.bytes = bytes;
    this.start = start;
    this.stop = stop;
    this.columnDescription = columnDescription;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public int getStart() {
    return start;
  }

  public int getStop() {
    return stop;
  }

  public ColumnDescription getColumnDescription() {
    return columnDescription;
  }
}
