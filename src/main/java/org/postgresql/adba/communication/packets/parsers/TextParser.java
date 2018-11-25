package org.postgresql.adba.communication.packets.parsers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.stream.Collectors;
import jdk.incubator.sql2.SqlException;

public class TextParser {
  private static final DateTimeFormatter timestampWithoutTimeZoneFormatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS]");
  private static final DateTimeFormatter timestampWithTimeZoneFormatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS]X");
  private static final DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter localTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss[.SSSSSS]");
  private static final DateTimeFormatter offsetTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss[.SSSSSS]X");

  private static final int IPV4_PART_COUNT = 4;
  private static final int IPV6_PART_COUNT = 8;

  public static Object boolOut(String in, Class<?> requestedClass) {
    return in.equals("t");
  }

  /**
   * reads a hex string into byte array.
   * @param in the hex string
   * @param requestedClass class requested by user
   * @return the bytes
   */
  public static Object byteaOut(String in, Class<?> requestedClass) {
    int len = in.length() - 2;
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(in.charAt(i + 2), 16) << 4)
          + Character.digit(in.charAt(i + 3), 16));
    }
    return data;
  }

  public static Object charOut(String in, Class<?> requestedClass) {
    return in.charAt(0);
  }

  public static Object nameout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  /**
   * Converts the string from the database to the requested class.
   * @param in the number as a string
   * @param requestedClass the class that the user wanted
   * @return a Number
   */
  public static Object int8Out(String in, Class<?> requestedClass) {
    if (Integer.class.equals(requestedClass)) {
      return Integer.parseInt(in);
    }

    if (Short.class.equals(requestedClass)) {
      return Short.parseShort(in);
    }

    return Long.parseLong(in);
  }

  /**
   * Converts the string from the database to the requested class.
   * @param in the number as a string
   * @param requestedClass the class that the user wanted
   * @return a Number
   */
  public static Object int2Out(String in, Class<?> requestedClass) {
    if (Long.class.equals(requestedClass)) {
      return Long.parseLong(in);
    }

    if (Integer.class.equals(requestedClass)) {
      return Integer.parseInt(in);
    }

    return Short.parseShort(in);
  }

  public static Object int2vectorout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  /**
   * Converts the string from the database to the requested class.
   * @param in the number as a string
   * @param requestedClass the class that the user wanted
   * @return a Number
   */
  public static Object int4Out(String in, Class<?> requestedClass) {
    if (Long.class.equals(requestedClass)) {
      return Long.parseLong(in);
    }

    if (Short.class.equals(requestedClass)) {
      return Short.parseShort(in);
    }

    return Integer.parseInt(in);
  }

  public static Object regprocout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object oidout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object tidout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object xidout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object cidout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object oidvectorout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object pg_ddl_command_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object jsonOut(String in, Class<?> requestedClass) {
    return in;
  }

  public static Object xml_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object pg_node_tree_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object smgrout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object point_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object lseg_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object path_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object box_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object poly_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object line_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  /**
   * Converts a CIDR from the database into an InetAddress object.
   *
   * @param in a string that represent an InetAddress
   * @param requestedClass what the user wanted
   * @return an InetAddress object
   */
  public static Object cidrOut(String in, Class<?> requestedClass) {
    in = in.split("/")[0]; // disregard network part

    // Make a first pass to categorize the characters in this string.
    boolean hasColon = false;
    boolean hasDot = false;
    for (int i = 0; i < in.length(); i++) {
      char c = in.charAt(i);
      if (c == '.') {
        hasDot = true;
      } else if (c == ':') {
        if (hasDot) {
          throw new SqlException("Received unparsable ip address from server", null, "", 0, "", 0);
        }
        hasColon = true;
      } else if (Character.digit(c, 16) == -1) {
        throw new SqlException("Received unparsable ip address from server", null, "", 0, "", 0);
      }
    }

    // Now decide which address family to parse.
    if (hasColon) {
      if (hasDot) {
        in = convertDottedQuadToHex(in);
        if (in == null) {
          throw new SqlException("Received unparsable ip address from server", null, "", 0, "", 0);
        }
      }
      try {
        byte[] result = textToNumericFormatV6(in);
        if (result == null) {
          throw new SqlException("Received unparsable ip address from server", null, "", 0, "", 0);
        }
        return InetAddress.getByAddress(result);
      } catch (UnknownHostException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    } else if (hasDot) {
      try {
        byte[] result = textToNumericFormatV4(in);
        if (result == null) {
          throw new SqlException("Received unparsable ip address from server", null, "", 0, "", 0);
        }
        return InetAddress.getByAddress(result);
      } catch (UnknownHostException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
    throw new SqlException("Received unparsable ip address from server", null, "", 0, "", 0);
  }

  /**
   * Converts an array of CIDR from the database into an array of InetAddress objects
   * @param in a list of cidr elements in string form
   * @param requestedClass what the user wanted
   * @return an array of InetAddress objects
   */
  public static Object cidrOutArray(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new InetAddress[] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    InetAddress[] result = new InetAddress[parts.length];

    for (int i = 0; i < parts.length; i++) {
      if ("NULL".equals(parts[i])) {
        result[i] = null;
      } else {
        result[i] = (InetAddress) cidrOut(parts[i], InetAddress.class);
      }
    }

    return result;
  }

  private static byte parseOctet(String ipPart) {
    // Note: we already verified that this string contains only hex digits.
    int octet = Integer.parseInt(ipPart);
    // Disallow leading zeroes, because no clear standard exists on
    // whether these should be interpreted as decimal or octal.
    if (octet > 255 || (ipPart.startsWith("0") && ipPart.length() > 1)) {
      throw new NumberFormatException();
    }
    return (byte) octet;
  }

  private static short parseHextet(String ipPart) {
    // Note: we already verified that this string contains only hex digits.
    int hextet = Integer.parseInt(ipPart, 16);
    if (hextet > 0xffff) {
      throw new NumberFormatException();
    }
    return (short) hextet;
  }

  private static byte [] textToNumericFormatV4(String ipString) {
    byte[] bytes = new byte[IPV4_PART_COUNT];
    int i = 0;
    try {
      for (String octet : ipString.split("\\.")) {
        bytes[i++] = parseOctet(octet);
      }
    } catch (NumberFormatException ex) {
      return null;
    }

    return i == IPV4_PART_COUNT ? bytes : null;
  }

  private static byte [] textToNumericFormatV6(String ipString) {
    // An address can have [2..8] colons, and N colons make N+1 parts.
    List<String> parts = Arrays.stream(ipString.split(":")).collect(Collectors.toList());
    if (parts.size() < 3 || parts.size() > IPV6_PART_COUNT + 1) {
      return null;
    }

    // Disregarding the endpoints, find "::" with nothing in between.
    // This indicates that a run of zeroes has been skipped.
    int skipIndex = -1;
    for (int i = 1; i < parts.size() - 1; i++) {
      if (parts.get(i).length() == 0) {
        if (skipIndex >= 0) {
          return null; // Can't have more than one ::
        }
        skipIndex = i;
      }
    }

    int partsHi; // Number of parts to copy from above/before the "::"
    int partsLo; // Number of parts to copy from below/after the "::"
    if (skipIndex >= 0) {
      // If we found a "::", then check if it also covers the endpoints.
      partsHi = skipIndex;
      partsLo = parts.size() - skipIndex - 1;
      if (parts.get(0).length() == 0 && --partsHi != 0) {
        return null; // ^: requires ^::
      }
      if (parts.get(parts.size() - 1).length() == 0 && --partsLo != 0) {
        return null; // :$ requires ::$
      }
    } else {
      // Otherwise, allocate the entire address to partsHi. The endpoints
      // could still be empty, but parseHextet() will check for that.
      partsHi = parts.size();
      partsLo = 0;
    }

    // If we found a ::, then we must have skipped at least one part.
    // Otherwise, we must have exactly the right number of parts.
    int partsSkipped = IPV6_PART_COUNT - (partsHi + partsLo);
    if (!(skipIndex >= 0 ? partsSkipped >= 1 : partsSkipped == 0)) {
      return null;
    }

    // Now parse the hextets into a byte array.
    ByteBuffer rawBytes = ByteBuffer.allocate(2 * IPV6_PART_COUNT);
    try {
      for (int i = 0; i < partsHi; i++) {
        rawBytes.putShort(parseHextet(parts.get(i)));
      }
      for (int i = 0; i < partsSkipped; i++) {
        rawBytes.putShort((short) 0);
      }
      for (int i = partsLo; i > 0; i--) {
        rawBytes.putShort(parseHextet(parts.get(parts.size() - i)));
      }
    } catch (NumberFormatException ex) {
      return null;
    }
    return rawBytes.array();
  }

  private static String convertDottedQuadToHex(String ipString) {
    int lastColon = ipString.lastIndexOf(':');
    String initialPart = ipString.substring(0, lastColon + 1);
    String dottedQuad = ipString.substring(lastColon + 1);
    byte[] quad = textToNumericFormatV4(dottedQuad);
    if (quad == null) {
      return null;
    }
    String penultimate = Integer.toHexString(((quad[0] & 0xff) << 8) | (quad[1] & 0xff));
    String ultimate = Integer.toHexString(((quad[2] & 0xff) << 8) | (quad[3] & 0xff));
    return initialPart + penultimate + ":" + ultimate;
  }

  public static Object float4Out(String in, Class<?> requestedClass) {
    return Float.parseFloat(in);
  }

  public static Object float8Out(String in, Class<?> requestedClass) {
    return Double.parseDouble(in);
  }

  public static Object abstimeout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object reltimeout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object tintervalout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object unknownout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object circle_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object cash_out(String in, Class<?> requestedClass) {
    return in;
  }

  public static Object macaddr_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object inet_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object aclitemout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object bpCharOut(String in, Class<?> requestedClass) {
    return in.charAt(0);
  }

  /**
   * Converts the string from the database to an array of Character objects.
   * @param in the array as a string
   * @param requestedClass the class that the user wanted
   * @return an array of Character objects
   */
  public static Object bpCharOutArray(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new Character[] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    Character[] result = new Character[parts.length];

    for (int i = 0; i < parts.length; i++) {
      if ("NULL".equals(parts[i])) {
        result[i] = null;
      } else {
        result[i] = parts[i].charAt(0);
      }
    }

    return result;
  }

  public static Object varcharout(String in, Class<?> requestedClass) {
    return in;
  }

  public static Object dateOut(String in, Class<?> requestedClass) {
    return LocalDate.parse(in, localDateFormatter);
  }

  /**
   * Converts the string from the database to an array of LocalDate objects.
   * @param in the array as a string
   * @param requestedClass the class that the user wanted
   * @return an array of LocalDate objects
   */
  public static Object dateOutArray(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new LocalDate[] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    LocalDate[] result = new LocalDate[parts.length];

    for (int i = 0; i < parts.length; i++) {
      if ("NULL".equals(parts[i])) {
        result[i] = null;
      } else {
        result[i] = LocalDate.parse(parts[i], localDateFormatter);
      }
    }

    return result;
  }

  public static Object timeOut(String in, Class<?> requestedClass) {
    return LocalTime.parse(in, localTimeFormatter);
  }

  /**
   * Converts the string from the database to an array of LocalTime objects.
   * @param in the array as a string
   * @param requestedClass the class that the user wanted
   * @return an array of LocalTime objects
   */
  public static Object timeOutArray(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new LocalTime[] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    LocalTime[] result = new LocalTime[parts.length];

    for (int i = 0; i < parts.length; i++) {
      if ("NULL".equals(parts[i])) {
        result[i] = null;
      } else {
        result[i] = LocalTime.parse(parts[i], localTimeFormatter);
      }
    }

    return result;
  }

  /**
   * parses a timestamp into either a LocalDateTime or LocalTime based on what the user requested.
   * @param in string from the postgresql server
   * @param requestedClass class the user requested
   * @return object
   */
  public static Object timestampOut(String in, Class<?> requestedClass) {
    LocalDateTime ldt = LocalDateTime.parse(in, timestampWithoutTimeZoneFormatter);

    if (LocalTime.class.equals(requestedClass)) {
      return ldt.toLocalTime();
    }

    return ldt;
  }

  /**
   * Converts the string from the database to an array of LocalDateTime objects.
   * @param in the array as a string
   * @param requestedClass the class that the user wanted
   * @return an array of LocalDateTime objects
   */
  public static Object timestampOutArray(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new LocalDateTime[] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    LocalDateTime[] result = new LocalDateTime[parts.length];

    for (int i = 0; i < parts.length; i++) {
      if ("NULL".equals(parts[i])) {
        result[i] = null;
      } else {
        result[i] = LocalDateTime.parse(parts[i].substring(1, parts[i].length() - 1), timestampWithoutTimeZoneFormatter);
      }
    }

    return result;
  }

  public static Object timestampTimeZoneOut(String in, Class<?> requestedClass) {
    return OffsetDateTime.parse(in, timestampWithTimeZoneFormatter);
  }

  /**
   * Converts the string from the database to an array of LocalDateTime objects.
   * @param in the array as a string
   * @param requestedClass the class that the user wanted
   * @return an array of LocalDateTime objects
   */
  public static Object timestampTimeZoneOutArray(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new OffsetDateTime[] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    OffsetDateTime[] result = new OffsetDateTime[parts.length];

    for (int i = 0; i < parts.length; i++) {
      if ("NULL".equals(parts[i])) {
        result[i] = null;
      } else {
        result[i] = OffsetDateTime.parse(parts[i].substring(1, parts[i].length() - 1), timestampWithTimeZoneFormatter);
      }
    }

    return result;
  }

  /**
   * parses a string representation of an interval.
   * @param in the string to parse
   * @param requestedClass class requested by user
   * @return a Duration object
   */
  public static Object intervalOut(String in, Class<?> requestedClass) {
    final boolean IsoFormat = !in.startsWith("@");

    // Just a simple '0'
    if (!IsoFormat && in.length() == 3 && in.charAt(2) == '0') {
      return Duration.of(0, ChronoUnit.MICROS);
    }

    int years = 0;
    int months = 0;
    int days = 0;
    int hours = 0;
    int minutes = 0;
    double seconds = 0;

    try {
      String valueToken = null;

      in = in.replace('+', ' ').replace('@', ' ');
      final StringTokenizer st = new StringTokenizer(in);
      for (int i = 1; st.hasMoreTokens(); i++) {
        String token = st.nextToken();

        if ((i & 1) == 1) {
          int endHours = token.indexOf(':');
          if (endHours == -1) {
            valueToken = token;
            continue;
          }

          // This handles hours, minutes, seconds and microseconds for
          // ISO intervals
          int offset = (token.charAt(0) == '-') ? 1 : 0;

          hours = nullSafeIntGet(token.substring(offset, endHours));
          minutes = nullSafeIntGet(token.substring(endHours + 1, endHours + 3));

          // Pre 7.4 servers do not put second information into the results
          // unless it is non-zero.
          int endMinutes = token.indexOf(':', endHours + 1);
          if (endMinutes != -1) {
            seconds = nullSafeDoubleGet(token.substring(endMinutes + 1));
          }

          if (offset == 1) {
            hours = -hours;
            minutes = -minutes;
            seconds = -seconds;
          }

          valueToken = null;
        } else {
          // This handles years, months, days for both, ISO and
          // Non-ISO intervals. Hours, minutes, seconds and microseconds
          // are handled for Non-ISO intervals here.

          if (token.startsWith("year")) {
            years = nullSafeIntGet(valueToken);
          } else if (token.startsWith("mon")) {
            months = nullSafeIntGet(valueToken);
          } else if (token.startsWith("day")) {
            days = nullSafeIntGet(valueToken);
          } else if (token.startsWith("hour")) {
            hours = nullSafeIntGet(valueToken);
          } else if (token.startsWith("min")) {
            minutes = nullSafeIntGet(valueToken);
          } else if (token.startsWith("sec")) {
            seconds = nullSafeDoubleGet(valueToken);
          }
        }
      }
    } catch (NumberFormatException e) {
      throw new SqlException("Conversion of interval failed", e, "", 0, "", 0);
    }

    if (!IsoFormat && in.endsWith("ago")) {
      // Inverse the leading sign
      return Duration.of((long)((years * -31556952000000L) + (months * -2592000000000L) + (days * -86400000000L)
          + (hours * -3600000000L) + (minutes * -60000000L) + (seconds * -1000000L)), ChronoUnit.MICROS);
    } else {
      return Duration.of((long)((years * 31556952000000L) + (months * 2592000000000L) + (days * 86400000000L)
          + (hours * 3600000000L) + (minutes * 60000000L) + (seconds * 1000000L)), ChronoUnit.MICROS);
    }
  }

  /**
   * Parses an array of interval objects that come from the database.
   * @param in incoming data
   * @param requestedClass datatype that the user wanted
   * @return a Duration[]
   */
  public static Object intervalOutArray(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new Duration[] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    return Arrays.stream(parts)
        .map(s -> {
          if (s.equals("NULL")) {
            return null;
          }
          return (Duration)intervalOut(s, Duration.class);
        })
        .collect(Collectors.toList()).toArray(new Duration[]{});
  }

  public static Object timetzOut(String in, Class<?> requestedClass) {
    return OffsetTime.parse(in, offsetTimeFormatter);
  }

  /**
   * Converts the string from the database to an array of OffsetTime objects.
   * @param in the array as a string
   * @param requestedClass the class that the user wanted
   * @return an array of OffsetTime objects
   */
  public static Object timetzOutArray(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new OffsetTime[] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    OffsetTime[] result = new OffsetTime[parts.length];

    for (int i = 0; i < parts.length; i++) {
      if ("NULL".equals(parts[i])) {
        result[i] = null;
      } else {
        result[i] = OffsetTime.parse(parts[i], offsetTimeFormatter);
      }
    }

    return result;
  }

  public static Object bitOut(String in, Class<?> requestedClass) {
    return parseBitString(in, requestedClass);
  }

  public static Object varBitOut(String in, Class<?> requestedClass) {
    return parseBitString(in, requestedClass);
  }

  private static Object parseBitString(String in, Class<?> requestedClass) {
    if (requestedClass == byte[].class) {
      return new BigInteger(in, 2).toByteArray();
    }

    if (in.length() == 1) {
      return Objects.equals("1", in);
    }

    return new BigInteger(in, 2).toByteArray();
  }

  /**
   * parse function for data that comes from an bit[] or varbit[] column.
   * @param in String with 0's and 1's
   * @param requestedClass what the user wanted
   * @return an byte[][] object.
   */
  public static Object bitOutArray(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new byte[][] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    byte[][] result = new byte[parts.length][];

    for (int i = 0; i < parts.length; i++) {
      if ("NULL".equals(parts[i])) {
        result[i] = null;
      } else {
        result[i] = (byte[]) parseBitString(parts[i], byte[].class);
      }
    }

    return result;
  }

  public static Object numericOut(String in, Class<?> requestedClass) {
    return new BigDecimal(in);
  }

  /**
   * Converts the string from the database to an array of LocalDateTime objects.
   * @param in the array as a string
   * @param requestedClass the class that the user wanted
   * @return an array of LocalDateTime objects
   */
  public static Object numericOutArray(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new BigDecimal[] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    BigDecimal[] result = new BigDecimal[parts.length];

    for (int i = 0; i < parts.length; i++) {
      if ("NULL".equals(parts[i])) {
        result[i] = null;
      } else {
        result[i] = new BigDecimal(parts[i]);
      }
    }

    return result;
  }

  public static Object textOut(String in, Class<?> requestedClass) {
    return in;
  }

  public static Object regprocedureout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object regoperout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object regoperatorout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object regclassout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object regtypeout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object cstring_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object any_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object anyarray_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object void_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object trigger_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object language_handler_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object internal_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object opaque_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object anyelement_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object anynonarray_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object uuidOut(String in, Class<?> requestedClass) {
    return UUID.fromString(in);
  }

  /**
   * Converts the string from the database to an array of UUID objects.
   * @param in the array as a string
   * @param requestedClass the class that the user wanted
   * @return an array of UUID objects
   */
  public static Object uuidOutArray(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new UUID[] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    UUID[] result = new UUID[parts.length];

    for (int i = 0; i < parts.length; i++) {
      if ("NULL".equals(parts[i])) {
        result[i] = null;
      } else {
        result[i] = UUID.fromString(parts[i]);
      }
    }

    return result;
  }

  public static Object txid_snapshot_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object fdw_handler_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object pg_lsn_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object tsm_handler_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object anyenum_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object tsvectorout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object tsqueryout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object gtsvectorout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object regconfigout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object regdictionaryout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object jsonb_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object anyrange_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object event_trigger_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object range_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object regnamespaceout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object regroleout(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object array_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  /**
   * Converts the string from the database to an array of shorts.
   * @param in the array as a string
   * @param requestedClass the class that the user wanted
   * @return an array of shorts
   */
  public static Object int2ArrayOut(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new Short[] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    Short[] result = new Short[parts.length];

    for (int i = 0; i < parts.length; i++) {
      if ("NULL".equals(parts[i])) {
        result[i] = null;
      } else {
        result[i] = Short.parseShort(parts[i]);
      }
    }

    return result;
  }

  /**
   * Converts the string from the database to an array of ints.
   * @param in the array as a string
   * @param requestedClass the class that the user wanted
   * @return an array of ints
   */
  public static Object int4ArrayOut(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new Integer[] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    Integer[] result = new Integer[parts.length];

    for (int i = 0; i < parts.length; i++) {
      if ("NULL".equals(parts[i])) {
        result[i] = null;
      } else {
        result[i] = Integer.parseInt(parts[i]);
      }
    }

    return result;
  }

  /**
   * Converts the string from the database to an array of longs.
   * @param in the array as a string
   * @param requestedClass the class that the user wanted
   * @return an array of longs
   */
  public static Object int8ArrayOut(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new Long[] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    Long[] result = new Long[parts.length];

    for (int i = 0; i < parts.length; i++) {
      if ("NULL".equals(parts[i])) {
        result[i] = null;
      } else {
        result[i] = Long.parseLong(parts[i]);
      }
    }

    return result;
  }

  /**
   * Converts the string from the database to an array of floats.
   * @param in the array as a string
   * @param requestedClass the class that the user wanted
   * @return an array of floats
   */
  public static Object floatArrayOut(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new Float[] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    Float[] result = new Float[parts.length];

    for (int i = 0; i < parts.length; i++) {
      if ("NULL".equals(parts[i])) {
        result[i] = null;
      } else {
        result[i] = Float.parseFloat(parts[i]);
      }
    }

    return result;
  }

  /**
   * Converts the string from the database to an array of doubles.
   * @param in the array as a string
   * @param requestedClass the class that the user wanted
   * @return an array of doubles
   */
  public static Object doubleArrayOut(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new Double[] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    Double[] result = new Double[parts.length];

    for (int i = 0; i < parts.length; i++) {
      if ("NULL".equals(parts[i])) {
        result[i] = null;
      } else {
        result[i] = Double.parseDouble(parts[i]);
      }
    }

    return result;
  }

  /**
   * Converts the string from the database to an array of doubles.
   * @param in the array as a string
   * @param requestedClass the class that the user wanted
   * @return an array of doubles
   */
  public static Object booleanArrayOut(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new Boolean[] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    Boolean[] result = new Boolean[parts.length];

    for (int i = 0; i < parts.length; i++) {
      if ("NULL".equals(parts[i])) {
        result[i] = null;
      } else {
        result[i] = "t".equals(parts[i]);
      }
    }

    return result;
  }

  /**
   * Converts the string from the database to an array of doubles.
   * @param in the array as a string
   * @param requestedClass the class that the user wanted
   * @return an array of doubles
   */
  public static Object byteaArrayOut(String in, Class<?> requestedClass) {
    if ("{}".equals(in)) {
      return new byte[][] {};
    }

    String[] parts = in.substring(1, in.length() - 1).split(",");

    byte[][] result = new byte[parts.length][];

    for (int i = 0; i < parts.length; i++) {
      if ("NULL".equals(parts[i])) {
        result[i] = null;
      } else {
        result[i] = new byte[(parts[i].length() - 4) / 2];
        for (int j = 0; j < parts[i].length() - 6; j += 2) {
          result[i][j / 2] = (byte) ((Character.digit(parts[i].charAt(j + 4), 16) << 4)
              + Character.digit(parts[i].charAt(j + 5), 16));
        }
      }
    }

    return result;
  }

  /**
   * Converts the string from the database to an array of strings.
   * @param in the array as a string
   * @param requestedClass the class that the user wanted
   * @return an array of strings
   */
  public static Object textArrayOut(String in, Class<?> requestedClass) {
    List<String> result = new ArrayList<>();

    StringBuilder sb = new StringBuilder();
    boolean stringStarted = false;
    boolean insideQuotes = false;
    boolean escapeNext = false;
    for (int i = 0; i < in.length(); i++) {
      if (!stringStarted) {
        if (in.charAt(i) == '{' && in.charAt(i + 1) != '"') {
          stringStarted = true;
        } else if (in.charAt(i) == '}' && in.charAt(i - 1) == ',') {
          result.add(sb.toString());
          sb.setLength(0);
        } else if (in.charAt(i) == '{' && in.charAt(i + 1) == '"') {
          continue;
        } else if (in.charAt(i - 1) == ',' && in.charAt(i) == ',') {
          result.add("");
          stringStarted = true;
        } else if (in.charAt(i - 1) == ',' && in.charAt(i) != '"') {
          if (in.charAt(i) != ',') {
            sb.append(in.charAt(i));
          }
          stringStarted = true;
        } else if (in.charAt(i) == '"') {
          stringStarted = true;
          insideQuotes = true;
        }
      } else {
        if (escapeNext) {
          sb.append(in.charAt(i));
          escapeNext = false;
        } else if (in.charAt(i) == '\\') {
          escapeNext = true;
        } else if (in.charAt(i) == '"' && insideQuotes) {
          result.add(sb.toString());
          sb.setLength(0);
          insideQuotes = false;
          stringStarted = false;
        } else if (in.charAt(i) == ',' && !insideQuotes) {
          String str = sb.toString();
          if ("NULL".equals(str)) {
            result.add(null);
          } else {
            result.add(str);
          }
          sb.setLength(0);
          stringStarted = false;
        } else if (in.charAt(i) == '}' && !insideQuotes && in.charAt(i - 1) == '{') {
          stringStarted = false;
        } else if (in.charAt(i) == '}' && !insideQuotes) {
          String str = sb.toString();
          if ("NULL".equals(str)) {
            result.add(null);
          } else {
            result.add(str);
          }
          sb.setLength(0);
          stringStarted = false;
        } else {
          sb.append(in.charAt(i));
        }
      }
    }

    return result.toArray(new String[]{});
  }

  public static Object record_out(String in, Class<?> requestedClass) {
    throw new RuntimeException("not implemented yet");
  }

  public static Object passthrough(String in, Class<?> requestedClass) {
    return in;
  }

  /**
   * Returns integer value of value or 0 if value is null.
   *
   * @param value integer as string value
   * @return integer parsed from string value
   * @throws NumberFormatException if the string contains invalid chars
   */
  private static int nullSafeIntGet(String value) throws NumberFormatException {
    return (value == null) ? 0 : Integer.parseInt(value);
  }

  /**
   * Returns double value of value or 0 if value is null.
   *
   * @param value double as string value
   * @return double parsed from string value
   * @throws NumberFormatException if the string contains invalid chars
   */
  private static double nullSafeDoubleGet(String value) throws NumberFormatException {
    return (value == null) ? 0 : Double.parseDouble(value);
  }
}
