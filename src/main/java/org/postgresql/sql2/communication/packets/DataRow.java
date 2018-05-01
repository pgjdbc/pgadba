package org.postgresql.sql2.communication.packets;

import jdk.incubator.sql2.Result;
import org.postgresql.sql2.communication.TableCell;
import org.postgresql.sql2.communication.packets.parts.ColumnDescription;
import org.postgresql.sql2.util.BinaryHelper;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DataRow implements Result.Row {
  private Map<String, TableCell> columns;
  private long rowNumber;

  public DataRow(byte[] bytes, ColumnDescription[] description, long rowNumber) {
    this.rowNumber = rowNumber;

    short numOfColumns = BinaryHelper.readShort(bytes[0], bytes[1]);
    int pos = 2;
    columns = new HashMap<>(numOfColumns);
    for(int i = 0; i < numOfColumns; i++) {
      int length = BinaryHelper.readInt(bytes[pos], bytes[pos + 1], bytes[pos + 2], bytes[pos + 3]);
      pos += 4;
      columns.put(description[i].getName().toLowerCase(), new TableCell(bytes, pos, pos + length, description[i]));
      pos += length;
    }
  }

  @Override
  public <T> T get(String id, Class<T> type) {
    TableCell tc = columns.get(id.toLowerCase());

    if(tc == null) {
      throw new IllegalArgumentException("no column with id " + id);
    }

    switch (tc.getColumnDescription().getFormatCode()){

      case TEXT:
        String data = new String(BinaryHelper.subBytes(tc.getBytes(), tc.getStart(), tc.getStop()), StandardCharsets.UTF_8);
        return (T)tc.getColumnDescription().getColumnType().getTextParser().apply(data);
      case BINARY:
        return (T)tc.getColumnDescription().getColumnType().getBinaryParser().apply(tc.getBytes(), tc.getStart(), tc.getStop());
    }

    return null;
  }

  @Override
  public String[] getIdentifiers() {
    return new String[0];
  }

  @Override
  public long rowNumber() {
    return rowNumber;
  }

  @Override
  public void cancel() {

  }
}
