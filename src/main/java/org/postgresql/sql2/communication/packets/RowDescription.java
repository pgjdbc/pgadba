package org.postgresql.sql2.communication.packets;

import java.io.IOException;

import org.postgresql.sql2.communication.NetworkInputStream;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.packets.parts.ColumnDescription;

public class RowDescription {
  private ColumnDescription[] descriptions;

  public RowDescription(NetworkInputStream input) throws IOException {
    short numOfColumns = input.readShort();
    descriptions = new ColumnDescription[numOfColumns];
    for (int i = 0; i < numOfColumns; i++) {
      String name = input.readString();
      int objectIdOfTable = input.readInteger();
      short attributeNumberOfColumn = input.readShort();
      int fieldOId = input.readInteger();
      short dataTypeSize = input.readShort();
      int typeModifier = input.readInteger();
      short formatCode = input.readShort();

      descriptions[i] = new ColumnDescription(name, objectIdOfTable, attributeNumberOfColumn, fieldOId, dataTypeSize,
          typeModifier, formatCode);
    }
  }

  public ColumnDescription[] getDescriptions() {
    return descriptions;
  }
}
