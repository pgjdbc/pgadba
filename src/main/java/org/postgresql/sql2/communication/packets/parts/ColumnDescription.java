package org.postgresql.sql2.communication.packets.parts;

public class ColumnDescription {
  private String name;
  private int objectIdOfTable;
  private short attributeNumberOfColumn;
  private ColumnTypes columnType;
  private short dataTypeSize;
  private int typeModifier;
  private FormatCodeTypes formatCode;

  public ColumnDescription(String name, int objectIdOfTable, short attributeNumberOfColumn, int fieldOId,
                           short dataTypeSize, int typeModifier, short formatCode) {
    this.name = name;
    this.objectIdOfTable = objectIdOfTable;
    this.attributeNumberOfColumn = attributeNumberOfColumn;
    this.columnType = ColumnTypes.lookup(fieldOId);
    this.dataTypeSize = dataTypeSize;
    this.typeModifier = typeModifier;
    this.formatCode = FormatCodeTypes.lookup(formatCode);
  }

  public String getName() {
    return name;
  }

  public int getObjectIdOfTable() {
    return objectIdOfTable;
  }

  public short getAttributeNumberOfColumn() {
    return attributeNumberOfColumn;
  }

  public ColumnTypes getColumnType() {
    return columnType;
  }

  public short getDataTypeSize() {
    return dataTypeSize;
  }

  public int getTypeModifier() {
    return typeModifier;
  }

  public FormatCodeTypes getFormatCode() {
    return formatCode;
  }
}
