package org.postgresql.sql2.communication.packets.parsers;

import org.postgresql.sql2.util.BinaryHelper;

public class BinaryGenerator {
  public static byte[] fromBit(Object input) {
    return null;
  }

  public static byte[] fromTinyInt(Object input) {
    return null;
  }

  public static byte[] fromSmallInt(Object input) {
    if(input == null)
      return new byte[]{};

    return BinaryHelper.writeShort(((Number)input).shortValue());
  }

  public static byte[] fromInt(Object input) {
    if(input == null)
      return new byte[]{};

    return BinaryHelper.writeInt(((Number)input).intValue());
  }

  public static byte[] fromBigInt(Object input) {
    return null;
  }

  public static byte[] fromFloat(Object input) {
    return null;
  }

  public static byte[] fromDouble(Object input) {
    return null;
  }

  public static byte[] fromBigDecimal(Object input) {
    return null;
  }

  public static byte[] fromChar(Object input) {
    return null;
  }

  public static byte[] fromString(Object input) {
    return null;
  }

  public static byte[] fromLocalDate(Object input) {
    return null;
  }

  public static byte[] fromLocalTime(Object input) {
    return null;
  }

  public static byte[] fromLocalDateTime(Object input) {
    return null;
  }

  public static byte[] fromByteArray(Object input) {
    return null;
  }

  public static byte[] fromNull(Object input) {
    return null;
  }

  public static byte[] fromJavaObject(Object input) {
    return null;
  }

  public static byte[] fromBoolean(Object input) {
    return null;
  }

  public static byte[] fromXml(Object input) {
    return null;
  }

  public static byte[] fromOffsetTime(Object input) {
    return null;
  }

  public static byte[] fromOffsetDateTime(Object input) {
    return null;
  }
}
