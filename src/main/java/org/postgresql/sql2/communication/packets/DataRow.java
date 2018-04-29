package org.postgresql.sql2.communication.packets;

import jdk.incubator.sql2.Result;
import org.postgresql.sql2.communication.packets.parts.ColumnDescription;
import org.postgresql.sql2.util.BinaryHelper;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DataRow implements Result.Row {
  private Map<String, Object> columns;
  private long rowNumber;

  public DataRow(byte[] bytes, ColumnDescription[] description, long rowNumber) {
    this.rowNumber = rowNumber;

    short numOfColumns = BinaryHelper.readShort(bytes[0], bytes[1]);
    int pos = 2;
    columns = new HashMap<>(numOfColumns);
    for(int i = 0; i < numOfColumns; i++) {
      int length = BinaryHelper.readInt(bytes[pos], bytes[pos + 1], bytes[pos + 2], bytes[pos + 3]);
      pos += 4;
      switch (description[i].getFormatCode()){

        case TEXT:
          String data = new String(BinaryHelper.subBytes(bytes, pos, pos + length), StandardCharsets.UTF_8);
          columns.put(description[i].getName(), description[i].getColumnType().getTextParser().apply(data));
          break;
        case BINARY:
          columns.put(description[i].getName(), description[i].getColumnType().getBinaryParser().apply(bytes, pos, pos + length));
          break;
      }
      pos += length;
    }
  }

  @Override
  public <T> T get(String id, Class<T> type) {
    return (T) columns.get(id);
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
