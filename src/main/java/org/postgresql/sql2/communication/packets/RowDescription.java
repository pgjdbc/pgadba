package org.postgresql.sql2.communication.packets;

import org.postgresql.sql2.communication.packets.parts.ColumnDescription;
import org.postgresql.sql2.util.BinaryHelper;

import java.nio.charset.StandardCharsets;

public class RowDescription {
  private ColumnDescription[] descriptions;

  public RowDescription(byte[] bytes) {
    short numOfColumns = BinaryHelper.readShort(bytes[0], bytes[1]);
    int pos = 2;
    descriptions = new ColumnDescription[numOfColumns];
    for(int i = 0; i < numOfColumns; i++) {
      int nameEnd = BinaryHelper.nextNullBytePos(bytes, pos);
      String name = new String(BinaryHelper.subBytes(bytes, pos, nameEnd), StandardCharsets.UTF_8);
      pos = nameEnd + 1;
      int objectIdOfTable = BinaryHelper.readInt(bytes[pos], bytes[pos + 1], bytes[pos + 2], bytes[pos + 3]);
      pos += 4;
      short attributeNumberOfColumn = BinaryHelper.readShort(bytes[pos], bytes[pos + 1]);
      pos += 2;
      int fieldOId = BinaryHelper.readInt(bytes[pos], bytes[pos + 1], bytes[pos + 2], bytes[pos + 3]);
      pos += 4;
      short dataTypeSize = BinaryHelper.readShort(bytes[pos], bytes[pos + 1]);
      pos += 2;
      int typeModifier = BinaryHelper.readInt(bytes[pos], bytes[pos + 1], bytes[pos + 2], bytes[pos + 3]);
      pos += 4;
      short formatCode = BinaryHelper.readShort(bytes[pos], bytes[pos + 1]);
      pos += 2;

      descriptions[i] = new ColumnDescription(name, objectIdOfTable, attributeNumberOfColumn, fieldOId, dataTypeSize, typeModifier, formatCode);
    }
  }

  public ColumnDescription[] getDescriptions() {
    return descriptions;
  }
}
