package org.postgresql.sql2.communication.packets;

import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.SqlType;

import org.postgresql.sql2.communication.NetworkInputStream;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.TableCell;
import org.postgresql.sql2.communication.packets.parts.ColumnDescription;
import org.postgresql.sql2.util.BinaryHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DataRow implements Result.RowColumn, Result.OutColumn {
  private Map<String, Integer> columnNames;
  private Map<Integer, TableCell> columns;
  private long rowNumber;
  private int currentPos = 0;

  public DataRow(NetworkReadContext context, ColumnDescription[] description, long rowNumber) throws IOException {
    this.rowNumber = rowNumber;
    NetworkInputStream input = context.getPayload();

    short numOfColumns = input.readShort();
    columns = new HashMap<>(numOfColumns);
    columnNames = new HashMap<>(numOfColumns);
    for (int i = 0; i < numOfColumns; i++) {
      int length = input.readInteger();
      columnNames.put(description[i].getName().toLowerCase(), i);
      byte[] cellBytes = new byte[length];
      input.read(cellBytes);
      columns.put(i, new TableCell(cellBytes, description[i]));
    }
  }

  @Override
  public long rowNumber() {
    return rowNumber;
  }

  @Override
  public void cancel() {

  }

  @Override
  public <T> T get(Class<T> type) {
    TableCell tc = columns.get(currentPos);

    if (tc == null) {
      throw new IllegalArgumentException("no column with position " + currentPos);
    }

    // handle the null special case
    if (tc.getBytes().length == 0) {
      return null;
    }

    switch (tc.getColumnDescription().getFormatCode()) {
    case TEXT:
      String data = new String(tc.getBytes(), StandardCharsets.UTF_8);
      return (T) tc.getColumnDescription().getColumnType().getTextParser().apply(data);
    case BINARY:
      return (T) tc.getColumnDescription().getColumnType().getBinaryParser().apply(tc.getBytes());
    }

    return null;
  }

  @Override
  public String identifier() {
    return columns.get(currentPos).getColumnDescription().getName();
  }

  @Override
  public int index() {
    return currentPos;
  }

  @Override
  public int absoluteIndex() {
    return currentPos;
  }

  @Override
  public SqlType sqlType() {
    if (!columns.containsKey(currentPos)) {
      throw new IllegalArgumentException("no column with id " + currentPos);
    }

    return columns.get(currentPos).getColumnDescription().getColumnType().sqlType();
  }

  @Override
  public <T> Class<T> javaType() {
    if (!columns.containsKey(currentPos)) {
      throw new IllegalArgumentException("no column with id " + currentPos);
    }

    return columns.get(currentPos).getColumnDescription().getColumnType().javaType();
  }

  @Override
  public long length() {
    TableCell tc = columns.get(currentPos);
    return tc.getBytes().length;
  }

  @Override
  public int numberOfValuesRemaining() {
    return columns.size() - currentPos;
  }

  @Override
  public Column at(String id) {
    Integer newPos = columnNames.get(id.toLowerCase());

    if (newPos == null) {
      throw new IllegalArgumentException("no column with id " + id);
    }

    currentPos = newPos;
    return this;
  }

  @Override
  public Column at(int index) {
    if (!columns.containsKey(index)) {
      throw new IllegalArgumentException("no column with index " + index);
    }

    currentPos = index;
    return this;
  }

  @Override
  public Column slice(int numValues) {
    return null;
  }

  @Override
  public Column clone() {
    DataRow row;

    try {
      row = (DataRow) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new Error();
    }

    row.columnNames = new HashMap<>(columnNames);
    row.columns = new HashMap<>(columns);
    row.rowNumber = rowNumber;
    row.currentPos = currentPos;

    return row;
  }

}