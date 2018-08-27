package org.postgresql.sql2.communication.packets.parsers;

import org.postgresql.sql2.util.BinaryHelper;

public class BinaryParser {
  public static Object boolsend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object byteasend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object charsend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object namesend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object int8send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object int2send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object int2vectorsend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object int4send(byte[] bytes, Class<?> requestedClass) {
    return BinaryHelper.readInt(bytes[0], bytes[1], bytes[2], bytes[3]);
  }

  public static Object regprocsend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object oidsend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object tidsend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object xidsend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object cidsend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object oidvectorsend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object pg_ddl_command_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object json_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object xml_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object pg_node_tree_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object point_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object lseg_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object path_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object box_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object poly_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object line_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object cidr_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object float4send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object float8send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object abstimesend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object reltimesend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object tintervalsend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object unknownsend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object circle_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object cash_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object macaddr_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object inet_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object bpcharsend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object varcharsend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object date_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object time_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object timestamp_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object timestamptz_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object interval_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object timetz_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object bit_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object varbit_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object numeric_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object textsend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object regproceduresend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object regopersend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object regoperatorsend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object regclasssend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object regtypesend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object cstring_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object anyarray_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object void_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object uuid_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object txid_snapshot_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object pg_lsn_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object tsvectorsend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object tsquerysend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object regconfigsend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object regdictionarysend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object jsonb_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object range_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object regnamespacesend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object regrolesend(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object array_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }

  public static Object record_send(byte[] bytes, Class<?> requestedClass) {
    return null;
  }
}
