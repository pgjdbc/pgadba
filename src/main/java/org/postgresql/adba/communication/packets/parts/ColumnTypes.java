package org.postgresql.adba.communication.packets.parts;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.function.BiFunction;
import org.postgresql.adba.communication.packets.parsers.BinaryParser;
import org.postgresql.adba.communication.packets.parsers.TextParser;
import org.postgresql.adba.pgdatatypes.Box;
import org.postgresql.adba.pgdatatypes.Circle;
import org.postgresql.adba.pgdatatypes.IntegerRange;
import org.postgresql.adba.pgdatatypes.Line;
import org.postgresql.adba.pgdatatypes.LineSegment;
import org.postgresql.adba.pgdatatypes.LocalDateRange;
import org.postgresql.adba.pgdatatypes.LocalDateTimeRange;
import org.postgresql.adba.pgdatatypes.LongRange;
import org.postgresql.adba.pgdatatypes.NumericRange;
import org.postgresql.adba.pgdatatypes.OffsetDateTimeRange;
import org.postgresql.adba.pgdatatypes.Path;
import org.postgresql.adba.pgdatatypes.Point;
import org.postgresql.adba.pgdatatypes.Polygon;
import org.postgresql.adba.util.QuadFunction;

public enum ColumnTypes {
  BOOL(16, TextParser::boolOut, BinaryParser::boolsend, boolean.class, PgAdbaType.BOOLEAN),
  BYTEA(17, TextParser::byteaOut, BinaryParser::byteasend, byte[].class, PgAdbaType.BLOB),
  CHAR(18, TextParser::charOut, BinaryParser::charsend, Character.class, PgAdbaType.CHAR),
  NAME(19, TextParser::nameout, BinaryParser::namesend, String.class, PgAdbaType.VARCHAR),
  INT8(20, TextParser::int8Out, BinaryParser::int8send, long.class, PgAdbaType.BIGINT),
  INT2(21, TextParser::int2Out, BinaryParser::int2send, short.class, PgAdbaType.SMALLINT),
  INT2VECTOR(22, TextParser::int2vectorout, BinaryParser::int2vectorsend, int[].class, PgAdbaType.ARRAY),
  INT4(23, TextParser::int4Out, BinaryParser::int4send, int.class, PgAdbaType.INTEGER),
  REGPROC(24, TextParser::regprocout, BinaryParser::regprocsend, null, null),
  TEXT(25, TextParser::textOut, BinaryParser::textsend, String.class, PgAdbaType.LONGVARCHAR),
  OID(26, TextParser::oidOut, BinaryParser::oidsend, null, null),
  TID(27, TextParser::tidout, BinaryParser::tidsend, null, null),
  XID(28, TextParser::xidout, BinaryParser::xidsend, null, null),
  CID(29, TextParser::cidout, BinaryParser::cidsend, null, null),
  OIDVECTOR(30, TextParser::oidvectorout, BinaryParser::oidvectorsend, null, null),
  PG_DDL_COMMAND(32, TextParser::pg_ddl_command_out, BinaryParser::pg_ddl_command_send, null, null),
  PG_TYPE(71, TextParser::record_out, BinaryParser::record_send, null, null),
  PG_ATTRIBUTE(75, TextParser::record_out, BinaryParser::record_send, null, null),
  PG_PROC(81, TextParser::record_out, BinaryParser::record_send, null, null),
  PG_CLASS(83, TextParser::record_out, BinaryParser::record_send, null, null),
  JSON(114, TextParser::jsonOut, BinaryParser::json_send, null, null),
  XML(142, TextParser::textOut, BinaryParser::xml_send, null, null),
  _XML(143, TextParser::textArrayOut, BinaryParser::array_send, null, null),
  PG_NODE_TREE(194, TextParser::pg_node_tree_out, BinaryParser::pg_node_tree_send, null, null),
  _JSON(199, TextParser::textArrayOut, BinaryParser::array_send, null, null),
  SMGR(210, TextParser::smgrout, null, null, null),
  POINT(600, TextParser::pointOut, BinaryParser::point_send, Point.class, PgAdbaType.POINT),
  LSEG(601, TextParser::lineSegmentOut, BinaryParser::lseg_send, LineSegment.class, PgAdbaType.LINE_SEGMENT),
  PATH(602, TextParser::pathOut, BinaryParser::path_send, Path.class, PgAdbaType.PATH),
  BOX(603, TextParser::boxOut, BinaryParser::box_send, Box.class, PgAdbaType.BOX),
  POLYGON(604, TextParser::polyOut, BinaryParser::poly_send, Polygon.class, PgAdbaType.POLYGON),
  LINE(628, TextParser::lineOut, BinaryParser::line_send, Line.class, PgAdbaType.LINE),
  _LINE(629, TextParser::lineOutArray, BinaryParser::array_send, Line[].class, PgAdbaType.LINE_ARRAY),
  CIDR(650, TextParser::cidrOut, BinaryParser::cidr_send, InetAddress.class, PgAdbaType.CIDR),
  _CIDR(651, TextParser::cidrOutArray, BinaryParser::array_send, InetAddress[].class, PgAdbaType.CIDR_ARRAY),
  FLOAT4(700, TextParser::float4Out, BinaryParser::float4send, float.class, PgAdbaType.FLOAT),
  FLOAT8(701, TextParser::float8Out, BinaryParser::float8send, double.class, PgAdbaType.DOUBLE),
  ABSTIME(702, TextParser::abstimeout, BinaryParser::abstimesend, null, null),
  RELTIME(703, TextParser::reltimeout, BinaryParser::reltimesend, null, null),
  TINTERVAL(704, TextParser::tintervalout, BinaryParser::tintervalsend, null, null),
  UNKNOWN(705, TextParser::unknownOut, BinaryParser::unknownsend, null, null),
  CIRCLE(718, TextParser::circleOut, BinaryParser::circle_send, Circle.class, PgAdbaType.CIRCLE),
  _CIRCLE(719, TextParser::circleOutArray, BinaryParser::array_send, Circle[].class, PgAdbaType.CIRCLE_ARRAY),
  MONEY(790, TextParser::cash_out, BinaryParser::cash_send, null, null),
  _MONEY(791, TextParser::array_out, BinaryParser::array_send, null, null),
  MACADDR(829, TextParser::macaddr_out, BinaryParser::macaddr_send, null, null),
  INET(869, TextParser::inet_out, BinaryParser::inet_send, null, null),
  _BOOL(1000, TextParser::booleanArrayOut, BinaryParser::array_send, boolean[].class, PgAdbaType.BOOLEAN_ARRAY),
  _BYTEA(1001, TextParser::byteaArrayOut, BinaryParser::array_send, byte[][].class, PgAdbaType.BYTEA_ARRAY),
  _CHAR(1002, TextParser::array_out, BinaryParser::array_send, char[].class, PgAdbaType.ARRAY),
  _NAME(1003, TextParser::array_out, BinaryParser::array_send, String[].class, PgAdbaType.ARRAY),
  _INT2(1005, TextParser::int2ArrayOut, BinaryParser::array_send, short[].class, PgAdbaType.SHORT_ARRAY),
  _INT2VECTOR(1006, TextParser::array_out, BinaryParser::array_send, short[].class, PgAdbaType.ARRAY),
  _INT4(1007, TextParser::int4ArrayOut, BinaryParser::array_send, int[].class, PgAdbaType.INTEGER_ARRAY),
  _REGPROC(1008, TextParser::array_out, BinaryParser::array_send, null, null),
  _TEXT(1009, TextParser::textArrayOut, BinaryParser::array_send, String.class, PgAdbaType.STRING_ARRAY),
  _TID(1010, TextParser::array_out, BinaryParser::array_send, null, null),
  _XID(1011, TextParser::array_out, BinaryParser::array_send, null, null),
  _CID(1012, TextParser::array_out, BinaryParser::array_send, null, null),
  _OIDVECTOR(1013, TextParser::array_out, BinaryParser::array_send, null, null),
  _BPCHAR(1014, TextParser::bpCharOutArray, BinaryParser::array_send, char[].class, PgAdbaType.CHAR_ARRAY),
  _VARCHAR(1015, TextParser::textArrayOut, BinaryParser::array_send, String[].class, PgAdbaType.STRING_ARRAY),
  _INT8(1016, TextParser::int8ArrayOut, BinaryParser::array_send, long[].class, PgAdbaType.LONG_ARRAY),
  _POINT(1017, TextParser::pointOutArray, BinaryParser::array_send, Point[].class, PgAdbaType.POINT_ARRAY),
  _LSEG(1018, TextParser::lineSegmentOutArray, BinaryParser::array_send, LineSegment[].class, PgAdbaType.LINE_SEGMENT_ARRAY),
  _PATH(1019, TextParser::pathOutArray, BinaryParser::array_send, Path[].class, PgAdbaType.PATH_ARRAY),
  _BOX(1020, TextParser::boxOutArray, BinaryParser::array_send, Box[].class, PgAdbaType.BOX_ARRAY),
  _FLOAT4(1021, TextParser::floatArrayOut, BinaryParser::array_send, float[].class, PgAdbaType.FLOAT_ARRAY),
  _FLOAT8(1022, TextParser::doubleArrayOut, BinaryParser::array_send, double[].class, PgAdbaType.DOUBLE_ARRAY),
  _ABSTIME(1023, TextParser::array_out, BinaryParser::array_send, null, null),
  _RELTIME(1024, TextParser::array_out, BinaryParser::array_send, null, null),
  _TINTERVAL(1025, TextParser::array_out, BinaryParser::array_send, null, null),
  _POLYGON(1027, TextParser::polyOutArray, BinaryParser::array_send, Polygon[].class, PgAdbaType.POLYGON_ARRAY),
  _OID(1028, TextParser::oidOutArray, BinaryParser::array_send, null, null),
  ACLITEM(1033, TextParser::aclitemout, null, null, null),
  _ACLITEM(1034, TextParser::array_out, BinaryParser::array_send, null, null),
  _MACADDR(1040, TextParser::array_out, BinaryParser::array_send, null, null),
  _INET(1041, TextParser::array_out, BinaryParser::array_send, InetAddress[].class, null),
  BPCHAR(1042, TextParser::bpCharOut, BinaryParser::bpcharsend, char.class, PgAdbaType.CHAR),
  VARCHAR(1043, TextParser::varcharout, BinaryParser::varcharsend, String.class, PgAdbaType.VARCHAR),
  DATE(1082, TextParser::dateOut, BinaryParser::date_send, LocalDate.class, PgAdbaType.DATE),
  TIME(1083, TextParser::timeOut, BinaryParser::time_send, LocalTime.class, PgAdbaType.TIME),
  TIMESTAMP(1114, TextParser::timestampOut, BinaryParser::timestamp_send, LocalDateTime.class, PgAdbaType.TIMESTAMP),
  _TIMESTAMP(1115, TextParser::timestampOutArray, BinaryParser::array_send, LocalDateTime[].class, PgAdbaType.TIMESTAMP_ARRAY),
  _DATE(1182, TextParser::dateOutArray, BinaryParser::array_send, LocalDate[].class, PgAdbaType.DATE_ARRAY),
  _TIME(1183, TextParser::timeOutArray, BinaryParser::array_send, LocalTime[].class, PgAdbaType.TIME_ARRAY),
  TIMESTAMPTZ(1184, TextParser::timestampTimeZoneOut, BinaryParser::timestamptz_send, OffsetDateTime.class,
      PgAdbaType.TIMESTAMP_WITH_TIME_ZONE),
  _TIMESTAMPTZ(1185, TextParser::timestampTimeZoneOutArray, BinaryParser::array_send, OffsetDateTime[].class,
      PgAdbaType.TIMESTAMP_WITH_TIME_ZONE_ARRAY),
  INTERVAL(1186, TextParser::intervalOut, BinaryParser::interval_send, Duration.class, PgAdbaType.INTERVAL),
  _INTERVAL(1187, TextParser::intervalOutArray, BinaryParser::array_send, Duration[].class, PgAdbaType.INTERVAL_ARRAY),
  _NUMERIC(1231, TextParser::numericOutArray, BinaryParser::array_send, BigDecimal[].class, PgAdbaType.NUMERIC_ARRAY),
  PG_DATABASE(1248, TextParser::record_out, BinaryParser::record_send, null, null),
  _CSTRING(1263, TextParser::array_out, BinaryParser::array_send, null, null),
  TIMETZ(1266, TextParser::timetzOut, BinaryParser::timetz_send, OffsetTime.class, PgAdbaType.TIME_WITH_TIME_ZONE),
  _TIMETZ(1270, TextParser::timetzOutArray, BinaryParser::array_send, OffsetTime[].class, PgAdbaType.TIME_WITH_TIME_ZONE_ARRAY),
  BIT(1560, TextParser::bitOut, BinaryParser::bit_send, Boolean.class, PgAdbaType.BIT),
  _BIT(1561, TextParser::bitOutArray, BinaryParser::array_send, byte[][].class, PgAdbaType.BIT_ARRAY),
  VARBIT(1562, TextParser::varBitOut, BinaryParser::varbit_send, byte[].class, PgAdbaType.BIT),
  _VARBIT(1563, TextParser::bitOutArray, BinaryParser::array_send, byte[][].class, PgAdbaType.BIT_ARRAY),
  NUMERIC(1700, TextParser::numericOut, BinaryParser::numeric_send, BigDecimal.class, PgAdbaType.NUMERIC),
  REFCURSOR(1790, TextParser::textOut, BinaryParser::textsend, null, null),
  _REFCURSOR(2201, TextParser::array_out, BinaryParser::array_send, null, null),
  REGPROCEDURE(2202, TextParser::regprocedureout, BinaryParser::regproceduresend, null, null),
  REGOPER(2203, TextParser::regoperout, BinaryParser::regopersend, null, null),
  REGOPERATOR(2204, TextParser::regoperatorout, BinaryParser::regoperatorsend, null, null),
  REGCLASS(2205, TextParser::regclassout, BinaryParser::regclasssend, null, null),
  REGTYPE(2206, TextParser::regtypeout, BinaryParser::regtypesend, null, null),
  _REGPROCEDURE(2207, TextParser::array_out, BinaryParser::array_send, null, null),
  _REGOPER(2208, TextParser::array_out, BinaryParser::array_send, null, null),
  _REGOPERATOR(2209, TextParser::array_out, BinaryParser::array_send, null, null),
  _REGCLASS(2210, TextParser::array_out, BinaryParser::array_send, null, null),
  _REGTYPE(2211, TextParser::array_out, BinaryParser::array_send, null, null),
  RECORD(2249, TextParser::record_out, BinaryParser::record_send, null, null),
  CSTRING(2275, TextParser::cstring_out, BinaryParser::cstring_send, null, null),
  ANY(2276, TextParser::any_out, null, null, null),
  ANYARRAY(2277, TextParser::anyarray_out, BinaryParser::anyarray_send, null, null),
  VOID(2278, TextParser::void_out, BinaryParser::void_send, null, null),
  TRIGGER(2279, TextParser::trigger_out, null, null, null),
  LANGUAGE_HANDLER(2280, TextParser::language_handler_out, null, null, null),
  INTERNAL(2281, TextParser::internal_out, null, null, null),
  OPAQUE(2282, TextParser::opaque_out, null, null, null),
  ANYELEMENT(2283, TextParser::anyelement_out, null, null, null),
  _RECORD(2287, TextParser::array_out, BinaryParser::array_send, null, null),
  ANYNONARRAY(2776, TextParser::anynonarray_out, null, null, null),
  PG_AUTHID(2842, TextParser::record_out, BinaryParser::record_send, null, null),
  PG_AUTH_MEMBERS(2843, TextParser::record_out, BinaryParser::record_send, null, null),
  _TXID_SNAPSHOT(2949, TextParser::array_out, BinaryParser::array_send, null, null),
  UUID(2950, TextParser::uuidOut, BinaryParser::uuid_send, java.util.UUID.class, PgAdbaType.UUID),
  _UUID(2951, TextParser::uuidOutArray, BinaryParser::array_send, java.util.UUID[].class, PgAdbaType.UUID_ARRAY),
  TXID_SNAPSHOT(2970, TextParser::txid_snapshot_out, BinaryParser::txid_snapshot_send, null, null),
  FDW_HANDLER(3115, TextParser::fdw_handler_out, null, null, null),
  PG_LSN(3220, TextParser::pg_lsn_out, BinaryParser::pg_lsn_send, null, null),
  _PG_LSN(3221, TextParser::array_out, BinaryParser::array_send, null, null),
  TSM_HANDLER(3310, TextParser::tsm_handler_out, null, null, null),
  ANYENUM(3500, TextParser::anyenum_out, null, null, null),
  TSVECTOR(3614, TextParser::tsvectorout, BinaryParser::tsvectorsend, null, null),
  TSQUERY(3615, TextParser::tsqueryout, BinaryParser::tsquerysend, null, null),
  GTSVECTOR(3642, TextParser::gtsvectorout, null, null, null),
  _TSVECTOR(3643, TextParser::array_out, BinaryParser::array_send, null, null),
  _GTSVECTOR(3644, TextParser::array_out, BinaryParser::array_send, null, null),
  _TSQUERY(3645, TextParser::array_out, BinaryParser::array_send, null, null),
  REGCONFIG(3734, TextParser::regconfigout, BinaryParser::regconfigsend, null, null),
  _REGCONFIG(3735, TextParser::array_out, BinaryParser::array_send, null, null),
  REGDICTIONARY(3769, TextParser::regdictionaryout, BinaryParser::regdictionarysend, null, null),
  _REGDICTIONARY(3770, TextParser::array_out, BinaryParser::array_send, null, null),
  JSONB(3802, TextParser::textOut, BinaryParser::jsonb_send, null, null),
  _JSONB(3807, TextParser::textArrayOut, BinaryParser::array_send, null, null),
  ANYRANGE(3831, TextParser::anyrange_out, null, null, null),
  EVENT_TRIGGER(3838, TextParser::event_trigger_out, null, null, null),
  INT4RANGE(3904, TextParser::integerRangeOut, BinaryParser::range_send, IntegerRange.class, PgAdbaType.INTEGER_RANGE),
  _INT4RANGE(3905, TextParser::integerRangeOutArray, BinaryParser::array_send, IntegerRange[].class,
      PgAdbaType.INTEGER_RANGE_ARRAY),
  NUMRANGE(3906, TextParser::numericRangeOut, BinaryParser::range_send, NumericRange.class, PgAdbaType.NUMERIC_RANGE),
  _NUMRANGE(3907, TextParser::numericRangeOutArray, BinaryParser::array_send, NumericRange[].class,
      PgAdbaType.NUMERIC_RANGE_ARRAY),
  TSRANGE(3908, TextParser::localDateTimeRangeOut, BinaryParser::range_send, LocalDateTimeRange.class,
      PgAdbaType.LOCAL_DATE_TIME_RANGE),
  _TSRANGE(3909, TextParser::localDateTimeRangeArrayOut, BinaryParser::array_send, LocalDateTimeRange[].class,
      PgAdbaType.LOCAL_DATE_TIME_RANGE_ARRAY),
  TSTZRANGE(3910, TextParser::offsetDateTimeRangeOut, BinaryParser::range_send, OffsetDateTimeRange.class,
      PgAdbaType.OFFSET_DATE_TIME_RANGE),
  _TSTZRANGE(3911, TextParser::offsetDateTimeRangeArrayOut, BinaryParser::array_send, OffsetDateTimeRange[].class,
      PgAdbaType.OFFSET_DATE_TIME_RANGE_ARRAY),
  DATERANGE(3912, TextParser::localDateRangeOut, BinaryParser::range_send, LocalDateRange.class, PgAdbaType.LOCAL_DATE_RANGE),
  _DATERANGE(3913, TextParser::localDateRangeArrayOut, BinaryParser::array_send, LocalDateRange[].class,
      PgAdbaType.LOCAL_DATE_RANGE_ARRAY),
  INT8RANGE(3926, TextParser::longRangeOut, BinaryParser::range_send, LongRange.class, PgAdbaType.LONG_RANGE),
  _INT8RANGE(3927, TextParser::longRangeOutArray, BinaryParser::array_send, LongRange[].class, PgAdbaType.LONG_RANGE_ARRAY),
  REGNAMESPACE(4089, TextParser::regnamespaceout, BinaryParser::regnamespacesend, null, null),
  _REGNAMESPACE(4090, TextParser::array_out, BinaryParser::array_send, null, null),
  REGROLE(4096, TextParser::regroleout, BinaryParser::regrolesend, null, null),
  _REGROLE(4097, TextParser::array_out, BinaryParser::array_send, null, null),
  OTHER(0, TextParser::passthrough, null, null, null);

  private final int oid;
  private final BiFunction<String, Class<?>, Object> textParser;
  private final QuadFunction<byte[], Integer, Integer, Class<?>, Object> binaryParser;
  private final Class clazz;
  private final PgAdbaType type;

  ColumnTypes(int oid, BiFunction<String, Class<?>, Object> textParser,
      QuadFunction<byte[], Integer, Integer, Class<?>, Object> binaryParser,
      Class c, PgAdbaType type) {
    this.oid = oid;
    this.textParser = textParser;
    this.binaryParser = binaryParser;
    this.clazz = c;
    this.type = type;
  }

  /**
   * Finds the correct type from it's oid.
   * @param oid oid to search from
   * @return the ColumnTypes object
   */
  public static ColumnTypes lookup(int oid) {
    for (ColumnTypes ct : values()) {
      if (ct.oid == oid) {
        return ct;
      }
    }

    return OTHER;
  }

  public BiFunction<String, Class<?>, Object> getTextParser() {
    return textParser;
  }

  public QuadFunction<byte[], Integer, Integer, Class<?>, Object> getBinaryParser() {
    return binaryParser;
  }

  public <T> Class<T> javaType() {
    return clazz;
  }

  public PgAdbaType sqlType() {
    return type;
  }
}
