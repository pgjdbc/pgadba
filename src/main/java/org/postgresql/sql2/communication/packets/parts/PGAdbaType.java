package org.postgresql.sql2.communication.packets.parts;

import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.SqlType;
import org.postgresql.sql2.communication.packets.parsers.BinaryGenerator;

import java.util.function.Function;

public enum PGAdbaType implements SqlType {
  /**
   * Identifies the generic SQL type {@code BIT}.
   */
  BIT("bit", 1560, AdbaType.BIT, BinaryGenerator::fromBit, FormatCodeTypes.BINARY),
  /**
   * Identifies the generic SQL type {@code TINYINT}.
   */
  TINYINT("tinyint", 21, AdbaType.TINYINT, BinaryGenerator::fromTinyInt, FormatCodeTypes.BINARY),
  /**
   * Identifies the generic SQL type {@code SMALLINT}.
   */
  SMALLINT("smallint", 21, AdbaType.SMALLINT, BinaryGenerator::fromSmallInt, FormatCodeTypes.BINARY),
  /**
   * Identifies the generic SQL type {@code INTEGER}.
   */
  INTEGER("integer", 23, AdbaType.INTEGER, BinaryGenerator::fromInt, FormatCodeTypes.BINARY),
  /**
   * Identifies the generic SQL type {@code BIGINT}.
   */
  BIGINT("bigint", 20, AdbaType.BIGINT, BinaryGenerator::fromBigInt, FormatCodeTypes.BINARY),
  /**
   * Identifies the generic SQL type {@code FLOAT}.
   */
  FLOAT("float", 700, AdbaType.FLOAT, BinaryGenerator::fromFloat, FormatCodeTypes.BINARY),
  /**
   * Identifies the generic SQL type {@code REAL}.
   */
  REAL("float", 700, AdbaType.REAL, BinaryGenerator::fromFloat, FormatCodeTypes.BINARY),
  /**
   * Identifies the generic SQL type {@code DOUBLE}.
   */
  DOUBLE("double", 701, AdbaType.DOUBLE, BinaryGenerator::fromDouble, FormatCodeTypes.BINARY),
  /**
   * Identifies the generic SQL type {@code NUMERIC}.
   */
  NUMERIC("numeric", 1700, AdbaType.NUMERIC, BinaryGenerator::fromBigDecimal, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code DECIMAL}.
   */
  DECIMAL("numeric", 1700, AdbaType.DECIMAL, BinaryGenerator::fromBigDecimal, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code CHAR}.
   */
  CHAR("char", 1042, AdbaType.CHAR, BinaryGenerator::fromChar, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code VARCHAR}.
   */
  VARCHAR("varchar", 1043, AdbaType.VARCHAR, BinaryGenerator::fromString, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code LONGVARCHAR}.
   */
  LONGVARCHAR("text", 25, AdbaType.LONGVARCHAR, BinaryGenerator::fromString, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code DATE}.
   */
  DATE("date", 1082, AdbaType.DATE, BinaryGenerator::fromLocalDate, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code TIME}.
   */
  TIME("time", 1083, AdbaType.TIME, BinaryGenerator::fromLocalTime, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code TIMESTAMP}.
   */
  TIMESTAMP("timestamp", 1114, AdbaType.TIMESTAMP, BinaryGenerator::fromLocalDateTime, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code BINARY}.
   */
  BINARY("bytea", 17, AdbaType.BINARY, BinaryGenerator::fromByteArray, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code VARBINARY}.
   */
  VARBINARY("bytea", 17, AdbaType.VARBINARY, BinaryGenerator::fromByteArray, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code LONGVARBINARY}.
   */
  LONGVARBINARY("bytea", 17, AdbaType.LONGVARBINARY, BinaryGenerator::fromByteArray, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL value {@code NULL}.
   */
  NULL("void", 2278, AdbaType.NULL, BinaryGenerator::fromNull, FormatCodeTypes.BINARY),
  /**
   * Indicates that the SQL type
   * is database-specific and gets mapped to a Java object that can be
   * accessed via the methods getObject and setObject.
   */
  OTHER("null", 0, AdbaType.OTHER, BinaryGenerator::fromString, FormatCodeTypes.BINARY),
  /**
   * Indicates that the SQL type
   * is database-specific and gets mapped to a Java object that can be
   * accessed via the methods getObject and setObject.
   */
  JAVA_OBJECT("java_object", 0, AdbaType.JAVA_OBJECT, BinaryGenerator::fromJavaObject, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code DISTINCT}.
   */
  DISTINCT("distinct", 0, AdbaType.DISTINCT, BinaryGenerator::fromString, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code STRUCT}.
   */
  STRUCT("struct", 0, AdbaType.STRUCT, BinaryGenerator::fromString, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code ARRAY}.
   */
  ARRAY("anyarray", 2277, AdbaType.ARRAY, BinaryGenerator::fromString, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code BLOB}.
   */
  BLOB("bytea", 17, AdbaType.BLOB, BinaryGenerator::fromByteArray, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code CLOB}.
   */
  CLOB("text", 25, AdbaType.CLOB, BinaryGenerator::fromString, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code REF}.
   */
  REF("ref", 0, AdbaType.REF, BinaryGenerator::fromNull, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code DATALINK}.
   */
  DATALINK("datalink", 0, AdbaType.DATALINK, BinaryGenerator::fromNull, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code BOOLEAN}.
   */
  BOOLEAN("boolean", 16, AdbaType.BOOLEAN, BinaryGenerator::fromBoolean, FormatCodeTypes.BINARY),

  /**
   * Identifies the SQL type {@code ROWID}.
   */
  ROWID("rowid", 0, AdbaType.ROWID, BinaryGenerator::fromNull, FormatCodeTypes.BINARY),
  /**
   * Identifies the generic SQL type {@code NCHAR}.
   */
  NCHAR("char", 18, AdbaType.NCHAR, BinaryGenerator::fromString, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code NVARCHAR}.
   */
  NVARCHAR("varchar", 1043, AdbaType.NVARCHAR, BinaryGenerator::fromString, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code LONGNVARCHAR}.
   */
  LONGNVARCHAR("text", 25, AdbaType.LONGNVARCHAR, BinaryGenerator::fromString, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code NCLOB}.
   */
  NCLOB("text", 25, AdbaType.NCLOB, BinaryGenerator::fromString, FormatCodeTypes.TEXT),
  /**
   * Identifies the generic SQL type {@code SQLXML}.
   */
  SQLXML("xml", 142, AdbaType.SQLXML, BinaryGenerator::fromXml, FormatCodeTypes.TEXT),

  /**
   * Identifies the generic SQL type {@code REF CURSOR}.
   */
  REF_CURSOR("refcursor", 1790, AdbaType.REF_CURSOR, BinaryGenerator::fromNull, FormatCodeTypes.BINARY),

  /**
   * Identifies the generic SQL type {@code TIME WITH TIME ZONE}.
   */
  TIME_WITH_TIME_ZONE("time with timezone", 1266, AdbaType.TIME_WITH_TIME_ZONE, BinaryGenerator::fromOffsetTime, FormatCodeTypes.TEXT),

  /**
   * Identifies the generic SQL type {@code TIMESTAMP WITH TIME ZONE}.
   */
  TIMESTAMP_WITH_TIME_ZONE("timestamp with timezone", 1184, AdbaType.TIMESTAMP_WITH_TIME_ZONE, BinaryGenerator::fromOffsetDateTime, FormatCodeTypes.TEXT);

  private String name;
  private Integer oid;
  private AdbaType adbaType;
  private Function<Object, byte[]> byteGenerator;
  private FormatCodeTypes formatCodeTypes;

  PGAdbaType(String name, Integer oid, AdbaType adbaType, Function<Object, byte[]> byteGenerator, FormatCodeTypes formatCodeTypes) {
    this.name = name;
    this.oid = oid;
    this.adbaType = adbaType;
    this.byteGenerator = byteGenerator;
    this.formatCodeTypes = formatCodeTypes;
  }

  public static PGAdbaType convert(SqlType type) {
    if(type instanceof  PGAdbaType) {
      return (PGAdbaType) type;
    }
    else if (type instanceof AdbaType) {
      for (PGAdbaType t : values()) {
        if(t.adbaType == type) {
          return t;
        }
      }
    }

    throw new IllegalArgumentException("unknown type " + type);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getVendor() {
    return "postgresql";
  }

  @Override
  public Integer getVendorTypeNumber() {
    return oid;
  }

  public Function<Object, byte[]> getByteGenerator() {
    return byteGenerator;
  }

  public FormatCodeTypes getFormatCodeTypes() {
    return formatCodeTypes;
  }
}
