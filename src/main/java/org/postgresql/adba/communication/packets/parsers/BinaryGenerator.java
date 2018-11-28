package org.postgresql.adba.communication.packets.parsers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import org.postgresql.adba.pgdatatypes.Box;
import org.postgresql.adba.pgdatatypes.Line;
import org.postgresql.adba.pgdatatypes.LineSegment;
import org.postgresql.adba.pgdatatypes.Path;
import org.postgresql.adba.pgdatatypes.Point;
import org.postgresql.adba.util.BinaryHelper;

public class BinaryGenerator {

  private static final DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter localTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.nnnnnnnnn");
  private static final DateTimeFormatter localDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnnnnn");
  private static final DateTimeFormatter offsetTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.nnnnnnnnn X");
  private static final DateTimeFormatter offsetDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnnnnn X");

  private static final byte[] bits = new byte[]{1, 2, 4, 8, 16, 32, 64, (byte) 128};

  /**
   * parses something that should go into an bit or varbit type in postgresql so that it can be sent
   * over the network.
   *
   * @param input the bits to convert
   * @return a byte array of with an '0' or an '1' for every bit in the input
   */
  public static byte[] fromBit(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Boolean) {
      if (((boolean) input)) {
        return new byte[]{49};
      } else {
        return new byte[]{48};
      }
    }

    if (input instanceof byte[]) {
      byte[] ret = new byte[((byte[]) input).length * 8];

      byte[] inb = (byte[]) input;
      for (int i = 0; i < inb.length; i++) {
        for (int j = 0; j < 8; j++) {
          if ((inb[i] & bits[j]) != 0) {
            ret[i * 8 + (7 - j)] = 49;
          } else {
            ret[i * 8 + (7 - j)] = 48;
          }
        }
      }

      return ret;
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as bit to server");
  }

  /**
   * parses something that should go into an bit[] or varbit[] type in postgresql so that it can be sent
   * over the network.
   *
   * @param input the bits to convert
   * @return a byte array of with an '0' or an '1' for every bit in the input
   */
  public static byte[] fromBitArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    StringBuilder sb = new StringBuilder("{");

    if (input instanceof boolean[]) {
      boolean[] ba = (boolean[]) input;
      for (int i = 0; i < ba.length; i++) {
        if (i != 0) {
          sb.append(",");
        }

        if (((boolean) input)) {
          sb.append("1");
        } else {
          sb.append("0");
        }
      }

      sb.append("}");
      return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    if (input instanceof byte[][]) {
      for (int k = 0; k < ((byte[][]) input).length; k++) {
        if (k != 0) {
          sb.append(",");
        }

        byte[] inb = ((byte[][]) input)[k];
        for (byte b : inb) {
          for (int j = 7; j >= 0; j--) {
            if ((b & bits[j]) != 0) {
              sb.append("1");
            } else {
              sb.append("0");
            }
          }
        }
      }

      sb.append("}");
      return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as bit to server");
  }

  public static byte[] fromTinyInt(Object input) {
    throw new RuntimeException("not implemented yet");
  }

  /**
   * parses Number to a short represented as a byte array.
   *
   * @param input the Number to convert
   * @return a byte array of length 0 or 2
   */
  public static byte[] fromSmallInt(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    return BinaryHelper.writeShort(((Number) input).shortValue());
  }

  /**
   * parses Number to a int represented as a byte array.
   *
   * @param input the Number to convert
   * @return a byte array of length 0 or 4
   */
  public static byte[] fromInt(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    return BinaryHelper.writeInt(((Number) input).intValue());
  }

  /**
   * parses Number to a long represented as a byte array.
   *
   * @param input the Number to convert
   * @return a byte array of length 8
   */
  public static byte[] fromBigInt(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    return BinaryHelper.writeLong(((Number) input).longValue());
  }

  /**
   * parses Float to a byte array.
   *
   * @param input the Float to convert
   * @return a byte array of length 4 or null
   */
  public static byte[] fromFloat(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Float) {
      return ByteBuffer.allocate(4).putFloat((Float) input).array();
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a float to server");
  }

  /**
   * parses an array of Floats to a byte array.
   *
   * @param input the Float[] to convert
   * @return a byte array
   */
  public static byte[] fromFloatArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Float[]) {
      Float[] in = (Float[]) input;
      int size = 20 + in.length * 4 + Arrays.stream(in).mapToInt(f -> f == null ? 0 : 4).sum();
      byte[] data = new byte[size];
      int pos = 0;
      BinaryHelper.writeIntAtPos(1, pos, data); // number of dimensions
      pos += 4;
      BinaryHelper.writeIntAtPos(0, pos, data); // flags
      pos += 4;
      BinaryHelper.writeIntAtPos(700, pos, data); // oid of float
      pos += 4;
      BinaryHelper.writeIntAtPos(in.length, pos, data); // length of first dimension
      pos += 4;
      BinaryHelper.writeIntAtPos(1, pos, data); // lower b
      pos += 4;
      for (int i = 0; i < in.length; i++) {
        if (in[i] == null) {
          BinaryHelper.writeIntAtPos(-1, pos, data);
          pos += 4;
        } else {
          BinaryHelper.writeIntAtPos(4, pos, data);
          pos += 4;
          BinaryHelper.writeFloatAtPos(in[i], pos, data);
          pos += 4;
        }
      }
      return data;
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a float[] to server");
  }

  /**
   * parses Double to a byte array.
   *
   * @param input the Double to convert
   * @return a byte array of length 8 or null
   */
  public static byte[] fromDouble(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Double) {
      return ByteBuffer.allocate(8).putDouble((Double) input).array();
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a double to server");
  }

  /**
   * parses an array of Floats to a byte array.
   *
   * @param input the Float[] to convert
   * @return a byte array
   */
  public static byte[] fromDoubleArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Double[]) {
      Double[] in = (Double[]) input;
      int size = 20 + in.length * 4 + Arrays.stream(in).mapToInt(d -> d == null ? 0 : 8).sum();
      byte[] data = new byte[size];
      int pos = 0;
      BinaryHelper.writeIntAtPos(1, pos, data); // number of dimensions
      pos += 4;
      BinaryHelper.writeIntAtPos(0, pos, data); // flags
      pos += 4;
      BinaryHelper.writeIntAtPos(701, pos, data); // oid of float8
      pos += 4;
      BinaryHelper.writeIntAtPos(in.length, pos, data); // length of first dimension
      pos += 4;
      BinaryHelper.writeIntAtPos(1, pos, data); // lower b
      pos += 4;
      for (int i = 0; i < in.length; i++) {
        if (in[i] == null) {
          BinaryHelper.writeIntAtPos(-1, pos, data);
          pos += 4;
        } else {
          BinaryHelper.writeIntAtPos(8, pos, data);
          pos += 4;
          BinaryHelper.writeDoubleAtPos(in[i], pos, data);
          pos += 8;
        }
      }
      return data;
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a double[] to server");
  }

  /**
   * parses a BigDecimal to a byte array.
   *
   * @param input the BigDecimal to convert
   * @return a byte array of appropriate length or null
   */
  public static byte[] fromBigDecimal(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Number) {
      return input.toString().getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a BigDecimal to server");
  }

  /**
   * parses an array of BigDecimal objects to a byte array.
   *
   * @param input the BigDecimal[] to convert
   * @return a byte array of appropriate length or null
   */
  public static byte[] fromBigDecimalArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof BigDecimal[]) {
      BigDecimal[] in = (BigDecimal[]) input;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try {
        baos.write('{');
        for (int i = 0; i < in.length; i++) {
          if (i != 0) {
            baos.write(',');
          }
          if (in[i] == null) {
            baos.write("NULL".getBytes(StandardCharsets.UTF_8));
          } else {
            baos.write(in[i].toString().getBytes(StandardCharsets.UTF_8));
          }
        }
        baos.write('}');
      } catch (IOException e) {
        throw new IllegalArgumentException("couldn't parse input to byte array", e);
      }
      return baos.toByteArray();
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a BigDecimal[] to server");
  }

  /**
   * parses a Character to a byte array.
   *
   * @param input the Character to convert
   * @return a byte array
   */
  public static byte[] fromChar(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Character) {
      return ((Character) input).toString().getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a char to server");
  }

  /**
   * parses an array of Characters to a byte array.
   *
   * @param input the Character[] to convert
   * @return a byte array
   */
  public static byte[] fromCharArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Character[]) {
      Character[] in = (Character[]) input;
      int size = 20 + in.length * 4 + Arrays.stream(in).mapToInt(c -> c == null ? 0 : c.toString()
          .getBytes(StandardCharsets.UTF_8).length).sum();
      byte[] data = new byte[size];
      int pos = 0;
      BinaryHelper.writeIntAtPos(1, pos, data); // number of dimensions
      pos += 4;
      BinaryHelper.writeIntAtPos(0, pos, data); // flags
      pos += 4;
      BinaryHelper.writeIntAtPos(1042, pos, data); // oid of bpchar
      pos += 4;
      BinaryHelper.writeIntAtPos(in.length, pos, data); // length of first dimension
      pos += 4;
      BinaryHelper.writeIntAtPos(1, pos, data); // lower b
      pos += 4;
      for (int i = 0; i < in.length; i++) {
        if (in[i] == null) {
          BinaryHelper.writeIntAtPos(-1, pos, data);
          pos += 4;
        } else {
          byte[] bb = in[i].toString().getBytes(StandardCharsets.UTF_8);
          BinaryHelper.writeIntAtPos(bb.length, pos, data);
          pos += 4;
          if (bb.length == 1) {
            data[pos] = bb[0];
          } else if (bb.length == 2) {
            data[pos] = bb[0];
            data[pos + 1] = bb[1];
          }
          pos += bb.length;
        }
      }
      return data;
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a char[] to server");
  }

  /**
   * parses a String to a byte array.
   *
   * @param input the String to convert
   * @return a byte array
   */
  public static byte[] fromString(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    return ((String) input).getBytes(StandardCharsets.UTF_8);
  }

  /**
   * parses a UUID to a byte array.
   *
   * @param input the UUID to convert
   * @return a byte array
   */
  public static byte[] fromUuid(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    return input.toString().getBytes(StandardCharsets.UTF_8);
  }

  /**
   * parses a UUID[] to a byte array.
   *
   * @param input the UUID[] to convert
   * @return a byte array
   */
  public static byte[] fromUuidArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    UUID[] in = (UUID[]) input;
    String str = "{" + String.join(",", Arrays.stream(in).map(u -> u == null ? "NULL" : u.toString())
        .collect(Collectors.toList())) + "}";
    return str.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * parses an array into to a byte array.
   *
   * @param input the Array to convert
   * @return a byte array
   */
  public static byte[] fromByteaArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof byte[][]) {
      byte[][] in = (byte[][]) input;
      int size = 20 + in.length * 4 + Arrays.stream(in).mapToInt(a -> a == null ? 0 : a.length).sum();
      byte[] data = new byte[size];
      int pos = 0;
      BinaryHelper.writeIntAtPos(1, pos, data); // number of dimensions
      pos += 4;
      BinaryHelper.writeIntAtPos(0, pos, data); // flags
      pos += 4;
      BinaryHelper.writeIntAtPos(17, pos, data); // oid of bytea
      pos += 4;
      BinaryHelper.writeIntAtPos(in.length, pos, data); // flags
      pos += 4;
      BinaryHelper.writeIntAtPos(1, pos, data); // flags
      pos += 4;
      for (int i = 0; i < in.length; i++) {
        if (in[i] == null) {
          BinaryHelper.writeIntAtPos(-1, pos, data);
          pos += 4;
        } else {
          BinaryHelper.writeIntAtPos(in[i].length, pos, data);
          pos += 4;
          System.arraycopy(in[i], 0, data, pos, in[i].length);
          pos += in[i].length;
        }
      }
      return data;
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a byte[][] to server");
  }

  /**
   * parses an array into to a byte array.
   *
   * @param input the Array to convert
   * @return a byte array
   */
  public static byte[] fromShortArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Short[]) {
      Short[] in = (Short[]) input;
      int size = 20 + in.length * 4 + Arrays.stream(in).mapToInt(s -> s == null ? 0 : 2).sum();
      byte[] data = new byte[size];
      int pos = 0;
      BinaryHelper.writeIntAtPos(1, pos, data); // number of dimensions
      pos += 4;
      BinaryHelper.writeIntAtPos(0, pos, data); // flags
      pos += 4;
      BinaryHelper.writeIntAtPos(21, pos, data); // oid of int2
      pos += 4;
      BinaryHelper.writeIntAtPos(in.length, pos, data); // length of first dimension
      pos += 4;
      BinaryHelper.writeIntAtPos(1, pos, data); // lower b
      pos += 4;
      for (int i = 0; i < in.length; i++) {
        if (in[i] == null) {
          BinaryHelper.writeIntAtPos(-1, pos, data);
          pos += 4;
        } else {
          BinaryHelper.writeIntAtPos(2, pos, data);
          pos += 4;
          BinaryHelper.writeShortAtPos(in[i], pos, data);
          pos += 2;
        }
      }
      return data;
    }

    return new byte[]{};
  }

  /**
   * parses an array into to a byte array.
   *
   * @param input the Array to convert
   * @return a byte array
   */
  public static byte[] fromIntegerArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Integer[]) {
      Integer[] in = (Integer[]) input;
      int size = 20 + in.length * 4 + Arrays.stream(in).mapToInt(i -> i == null ? 0 : 4).sum();
      byte[] data = new byte[size];
      int pos = 0;
      BinaryHelper.writeIntAtPos(1, pos, data); // number of dimensions
      pos += 4;
      BinaryHelper.writeIntAtPos(0, pos, data); // flags
      pos += 4;
      BinaryHelper.writeIntAtPos(23, pos, data); // oid of int4
      pos += 4;
      BinaryHelper.writeIntAtPos(in.length, pos, data); // length of first dimension
      pos += 4;
      BinaryHelper.writeIntAtPos(1, pos, data); // lower b
      pos += 4;
      for (int i = 0; i < in.length; i++) {
        if (in[i] == null) {
          BinaryHelper.writeIntAtPos(-1, pos, data);
          pos += 4;
        } else {
          BinaryHelper.writeIntAtPos(4, pos, data);
          pos += 4;
          BinaryHelper.writeIntAtPos(in[i], pos, data);
          pos += 4;
        }
      }
      return data;
    }

    return new byte[]{};
  }

  /**
   * parses an array into to a byte array.
   *
   * @param input the Array to convert
   * @return a byte array
   */
  public static byte[] fromLongArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Long[]) {
      Long[] in = (Long[]) input;
      int size = 20 + in.length * 4 + Arrays.stream(in).mapToInt(l -> l == null ? 0 : 8).sum();
      byte[] data = new byte[size];
      int pos = 0;
      BinaryHelper.writeIntAtPos(1, pos, data); // number of dimensions
      pos += 4;
      BinaryHelper.writeIntAtPos(0, pos, data); // flags
      pos += 4;
      BinaryHelper.writeIntAtPos(20, pos, data); // oid of int8
      pos += 4;
      BinaryHelper.writeIntAtPos(in.length, pos, data); // length of first dimension
      pos += 4;
      BinaryHelper.writeIntAtPos(1, pos, data); // lower b
      pos += 4;
      for (int i = 0; i < in.length; i++) {
        if (in[i] == null) {
          BinaryHelper.writeIntAtPos(-1, pos, data);
          pos += 4;
        } else {
          BinaryHelper.writeIntAtPos(8, pos, data);
          pos += 4;
          BinaryHelper.writeLongAtPos(in[i], pos, data);
          pos += 8;
        }
      }
      return data;
    }

    return new byte[]{};
  }

  /**
   * parses an array into to a byte array.
   *
   * @param input the Array to convert
   * @return a byte array
   */
  public static byte[] fromBooleanArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Boolean[]) {
      Boolean[] in = (Boolean[]) input;
      int size = 20 + Arrays.stream(in).mapToInt(b -> b == null ? 4 : 5).sum();
      byte[] data = new byte[size];
      int pos = 0;
      BinaryHelper.writeIntAtPos(1, pos, data); // number of dimensions
      pos += 4;
      BinaryHelper.writeIntAtPos(0, pos, data); // flags
      pos += 4;
      BinaryHelper.writeIntAtPos(16, pos, data); // oid of bool
      pos += 4;
      BinaryHelper.writeIntAtPos(in.length, pos, data); // length of first dimension
      pos += 4;
      BinaryHelper.writeIntAtPos(1, pos, data); // lower b
      pos += 4;
      for (int i = 0; i < in.length; i++) {
        if (in[i] == null) {
          BinaryHelper.writeIntAtPos(-1, pos, data);
          pos += 4;
        } else {
          BinaryHelper.writeIntAtPos(1, pos, data);
          pos += 4;
          BinaryHelper.writeBooleanAtPos(in[i], pos, data);
          pos += 1;
        }
      }
      return data;
    }

    return new byte[]{};
  }

  /**
   * parses an array into to a byte array.
   *
   * @param input the Array to convert
   * @return a byte array
   */
  public static byte[] fromStringArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof String[]) {
      String[] in = (String[]) input;
      byte[][] parts = new byte[in.length][];
      for (int i = 0; i < in.length; i++) {
        if (in[i] == null) {
          parts[i] = null;
        } else {
          parts[i] = in[i].getBytes(StandardCharsets.UTF_8);
        }
      }
      int size = 20 + in.length * 4 + Arrays.stream(parts).mapToInt(a -> a == null ? 0 : a.length).sum();
      byte[] data = new byte[size];
      int pos = 0;
      BinaryHelper.writeIntAtPos(1, pos, data); // number of dimensions
      pos += 4;
      BinaryHelper.writeIntAtPos(0, pos, data); // flags
      pos += 4;
      BinaryHelper.writeIntAtPos(1043, pos, data); // oid of varchar
      pos += 4;
      BinaryHelper.writeIntAtPos(in.length, pos, data); // length of first dimension
      pos += 4;
      BinaryHelper.writeIntAtPos(1, pos, data); // lower b
      pos += 4;
      for (int i = 0; i < in.length; i++) {
        if (parts[i] == null) {
          BinaryHelper.writeIntAtPos(-1, pos, data);
          pos += 4;
        } else {
          BinaryHelper.writeIntAtPos(parts[i].length, pos, data);
          pos += 4;
          System.arraycopy(parts[i], 0, data, pos, parts[i].length);
          pos += parts[i].length;
        }
      }
      return data;
    }

    return new byte[]{};
  }

  /**
   * parses a LocalDate to a byte array.
   *
   * @param input the LocalDate to convert
   * @return a byte array
   */
  public static byte[] fromLocalDate(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof LocalDate) {
      LocalDate x = (LocalDate) input;
      if (x == LocalDate.MAX) {
        return "infinity".getBytes(StandardCharsets.UTF_8);
      } else if (x == LocalDate.MIN) {
        return "-infinity".getBytes(StandardCharsets.UTF_8);
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try {
        baos.write(x.format(localDateFormatter).getBytes(StandardCharsets.UTF_8));

        if (x.getYear() < 0) {
          baos.write(" BC".getBytes(StandardCharsets.UTF_8));
        }

        return baos.toByteArray();
      } catch (IOException e) {
        throw new IllegalArgumentException("couldn't parse input to byte array", e);
      }
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a LocalDate to server");
  }

  /**
   * parses a LocalDate to a byte array.
   *
   * @param input the LocalDate to convert
   * @return a byte array
   */
  public static byte[] fromLocalDateArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof LocalDate[]) {
      LocalDate[] in = (LocalDate[]) input;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try {
        baos.write('{');
        for (int i = 0; i < in.length; i++) {
          if (i != 0) {
            baos.write(',');
          }

          if (in[i] == null) {
            baos.write("NULL".getBytes(StandardCharsets.UTF_8));
            continue;
          }

          if (in[i] == LocalDate.MAX) {
            baos.write("infinity".getBytes(StandardCharsets.UTF_8));
            continue;
          } else if (in[i] == LocalDate.MIN) {
            baos.write("-infinity".getBytes(StandardCharsets.UTF_8));
            continue;
          }

          baos.write(in[i].format(localDateFormatter).getBytes(StandardCharsets.UTF_8));

          if (in[i].getYear() < 0) {
            baos.write(" BC".getBytes(StandardCharsets.UTF_8));
          }
        }
        baos.write('}');
      } catch (IOException e) {
        throw new IllegalArgumentException("couldn't parse input to byte array", e);
      }
      return baos.toByteArray();
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a LocalDate[] to server");
  }

  /**
   * parses a LocalTime to a byte array.
   *
   * @param input the LocalTime to convert
   * @return a byte array
   */
  public static byte[] fromLocalTime(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof LocalTime) {
      LocalTime x = (LocalTime) input;

      return x.format(localTimeFormatter).getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a LocalTime to server");
  }

  /**
   * parses a LocalTime to a byte array.
   *
   * @param input the LocalTime to convert
   * @return a byte array
   */
  public static byte[] fromLocalTimeArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof LocalTime[]) {
      LocalTime[] in = (LocalTime[]) input;

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try {
        baos.write('{');
        for (int i = 0; i < in.length; i++) {
          if (i != 0) {
            baos.write(',');
          }
          if (in[i] == null) {
            baos.write("NULL".getBytes(StandardCharsets.UTF_8));
          } else {
            baos.write(in[i].format(localTimeFormatter).getBytes(StandardCharsets.UTF_8));
          }
        }
        baos.write('}');
      } catch (IOException e) {
        throw new IllegalArgumentException("couldn't parse input to byte array", e);
      }
      return baos.toByteArray();
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a LocalTime[] to server");
  }

  /**
   * parses a LocalDateTime to a byte array.
   *
   * @param input the LocalDateTime to convert
   * @return a byte array
   */
  public static byte[] fromLocalDateTime(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof LocalDateTime) {
      LocalDateTime x = (LocalDateTime) input;
      if (x == LocalDateTime.MAX) {
        return "infinity".getBytes(StandardCharsets.UTF_8);
      } else if (x == LocalDateTime.MIN) {
        return "-infinity".getBytes(StandardCharsets.UTF_8);
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try {
        baos.write(x.format(localDateTimeFormatter).getBytes(StandardCharsets.UTF_8));

        if (x.getYear() < 0) {
          baos.write(" BC".getBytes(StandardCharsets.UTF_8));
        }

        return baos.toByteArray();
      } catch (IOException e) {
        throw new IllegalArgumentException("couldn't parse input to byte array", e);
      }
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a LocalDateTime to server");
  }

  /**
   * parses a LocalDateTime to a byte array.
   *
   * @param input the LocalDateTime to convert
   * @return a byte array
   */
  public static byte[] fromLocalDateTimeArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof LocalDateTime[]) {
      LocalDateTime[] in = (LocalDateTime[]) input;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try {
        baos.write('{');
        for (int i = 0; i < in.length; i++) {
          if (i != 0) {
            baos.write(',');
          }

          if (in[i] == null) {
            baos.write("NULL".getBytes(StandardCharsets.UTF_8));
            continue;
          }

          if (in[i] == LocalDateTime.MAX) {
            baos.write("infinity".getBytes(StandardCharsets.UTF_8));
            continue;
          } else if (in[i] == LocalDateTime.MIN) {
            baos.write("-infinity".getBytes(StandardCharsets.UTF_8));
            continue;
          }

          baos.write(in[i].format(localDateTimeFormatter).getBytes(StandardCharsets.UTF_8));

          if (in[i].getYear() < 0) {
            baos.write(" BC".getBytes(StandardCharsets.UTF_8));
          }
        }
        baos.write('}');
      } catch (IOException e) {
        throw new IllegalArgumentException("couldn't parse input to byte array", e);
      }
      return baos.toByteArray();
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a LocalDateTime[] to server");
  }

  /**
   * converts what the user sends for a binary blog to what the server wants as a binary blob (which are the same things).
   *
   * @param input data from the user
   * @return a byte array containing the information
   */
  public static byte[] fromByteArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof byte[]) {
      return (byte[]) input;
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a byte[] to server");
  }

  public static byte[] fromNull(Object input) {
    return new byte[]{};
  }

  /**
   * doesn't do anything, as serialization to and from java objects are a security risk.
   *
   * @param input object
   * @return a byte array
   */
  public static byte[] fromJavaObject(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    throw new RuntimeException("not implemented");
  }

  /**
   * parses a Boolean to a byte array.
   *
   * @param input the Boolean to convert
   * @return a byte array
   */
  public static byte[] fromBoolean(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Boolean) {
      if ((Boolean) input) {
        return new byte[]{1};
      } else {
        return new byte[]{0};
      }
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a boolean to server");
  }

  public static byte[] fromXml(Object input) {
    throw new RuntimeException("not implemented yet");
  }

  /**
   * parses a OffsetTime to a byte array.
   *
   * @param input the OffsetTime to convert
   * @return a byte array
   */
  public static byte[] fromOffsetTime(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof OffsetTime) {
      OffsetTime x = (OffsetTime) input;

      return x.format(offsetTimeFormatter).getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a OffsetTime to server");
  }

  /**
   * parses a OffsetTime to a byte array.
   *
   * @param input the OffsetTime to convert
   * @return a byte array
   */
  public static byte[] fromOffsetTimeArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof OffsetTime[]) {
      OffsetTime[] in = (OffsetTime[]) input;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try {
        baos.write('{');
        for (int i = 0; i < in.length; i++) {
          if (i != 0) {
            baos.write(',');
          }

          if (in[i] == null) {
            baos.write("NULL".getBytes(StandardCharsets.UTF_8));
          } else {
            baos.write(in[i].format(offsetTimeFormatter).getBytes(StandardCharsets.UTF_8));
          }
        }
        baos.write('}');
      } catch (IOException e) {
        throw new IllegalArgumentException("couldn't parse input to byte array", e);
      }
      return baos.toByteArray();
    }

    throw new RuntimeException(input.getClass().getName() + " can't be converted to byte[] to send as a OffsetTime[] to server");
  }

  /**
   * parses a OffsetDateTime to a byte array.
   *
   * @param input the OffsetDateTime to convert
   * @return a byte array
   */
  public static byte[] fromOffsetDateTime(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof OffsetDateTime) {
      OffsetDateTime x = (OffsetDateTime) input;
      if (x == OffsetDateTime.MAX) {
        return "infinity".getBytes(StandardCharsets.UTF_8);
      } else if (x == OffsetDateTime.MIN) {
        return "-infinity".getBytes(StandardCharsets.UTF_8);
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try {
        baos.write(x.format(offsetDateTimeFormatter).getBytes(StandardCharsets.UTF_8));

        if (x.getYear() < 0) {
          baos.write(" BC".getBytes(StandardCharsets.UTF_8));
        }

        return baos.toByteArray();
      } catch (IOException e) {
        throw new IllegalArgumentException("couldn't parse input to byte array", e);
      }
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a OffsetDateTime to server");
  }

  /**
   * parses an array of OffsetDateTime objects to a byte array.
   *
   * @param input the OffsetDateTime[] to convert
   * @return a byte array
   */
  public static byte[] fromOffsetDateTimeArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof OffsetDateTime[]) {
      OffsetDateTime[] in = (OffsetDateTime[]) input;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try {
        baos.write('{');
        for (int i = 0; i < in.length; i++) {
          if (i != 0) {
            baos.write(',');
          }

          if (in[i] == null) {
            baos.write("NULL".getBytes(StandardCharsets.UTF_8));
            continue;
          }

          if (in[i] == OffsetDateTime.MAX) {
            baos.write("infinity".getBytes(StandardCharsets.UTF_8));
            continue;
          } else if (in[i] == OffsetDateTime.MIN) {
            baos.write("-infinity".getBytes(StandardCharsets.UTF_8));
            continue;
          }

          baos.write(in[i].format(offsetDateTimeFormatter).getBytes(StandardCharsets.UTF_8));

          if (in[i].getYear() < 0) {
            baos.write(" BC".getBytes(StandardCharsets.UTF_8));
          }
        }
        baos.write('}');
      } catch (IOException e) {
        throw new IllegalArgumentException("couldn't parse input to byte array", e);
      }
      return baos.toByteArray();
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a OffsetDateTime to server");
  }

  /**
   * Converts a Duration object to a string the database understands.
   * @param input a Duration
   * @return a string on ISO8601 format
   */
  public static byte[] fromInterval(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Duration) {
      Duration d = (Duration) input;

      if (d.isZero()) {
        return "0".getBytes(StandardCharsets.UTF_8);
      }

      return d.toString().getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a Duration to server");
  }

  /**
   * Converts an array of Duration objects to a string the database understands.
   * @param input a Duration[]
   * @return a list of strings on ISO8601 format
   */
  public static byte[] fromIntervalArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    StringBuilder sb = new StringBuilder("{");

    if (input instanceof Duration[]) {
      for (int i = 0; i < ((Duration[]) input).length; i++) {
        if (i != 0) {
          sb.append(",");
        }

        Duration d = ((Duration[]) input)[i];

        if (d == null) {
          sb.append("NULL");
        } else if (d.isZero()) {
          sb.append("0");
        } else {
          sb.append(d.toString());
        }
      }

      sb.append("}");
      return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a Duration[] to server");
  }

  /**
   * Sends a string of json to bytes that the server hopefully understands.
   * @param input Should be a String
   * @return a byte array of json
   */
  public static byte[] fromJson(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof String) {
      return ((String) input).getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a Duration[] to server");
  }

  /**
   * parses an array of json strings into to a byte array.
   *
   * @param input the Array to convert
   * @return a byte array
   */
  public static byte[] fromJsonArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof String[]) {
      String[] in = (String[]) input;
      byte[][] parts = new byte[in.length][];
      for (int i = 0; i < in.length; i++) {
        if (in[i] == null) {
          parts[i] = null;
        } else {
          parts[i] = in[i].getBytes(StandardCharsets.UTF_8);
        }
      }
      int size = 20 + in.length * 4 + Arrays.stream(parts).mapToInt(a -> a == null ? 0 : a.length).sum();
      byte[] data = new byte[size];
      int pos = 0;
      BinaryHelper.writeIntAtPos(1, pos, data); // number of dimensions
      pos += 4;
      BinaryHelper.writeIntAtPos(0, pos, data); // flags
      pos += 4;
      BinaryHelper.writeIntAtPos(114, pos, data); // oid of json
      pos += 4;
      BinaryHelper.writeIntAtPos(in.length, pos, data); // length of first dimension
      pos += 4;
      BinaryHelper.writeIntAtPos(1, pos, data); // lower b
      pos += 4;
      for (int i = 0; i < in.length; i++) {
        if (parts[i] == null) {
          BinaryHelper.writeIntAtPos(-1, pos, data);
          pos += 4;
        } else {
          BinaryHelper.writeIntAtPos(parts[i].length, pos, data);
          pos += 4;
          System.arraycopy(parts[i], 0, data, pos, parts[i].length);
          pos += parts[i].length;
        }
      }
      return data;
    }

    return new byte[]{};
  }

  /**
   * Converts an InetAddress object to bytes to send to the database.
   * @param input an InetAddress object
   * @return bytes
   */
  public static byte[] fromCidr(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Inet4Address) {
      Inet4Address ia = (Inet4Address) input;
      byte[] address = ia.getAddress();
      return String.format("%d.%d.%d.%d", address[0] & 0xFF, address[1] & 0xFF, address[2] & 0xFF, address[3] & 0xFF)
          .getBytes(StandardCharsets.UTF_8);
    }
    if (input instanceof Inet6Address) {
      Inet6Address ia = (Inet6Address) input;
      byte[] address = ia.getAddress();
      return String.format("%02X%02X:%02X%02X:%02X%02X:%02X%02X:%02X%02X:%02X%02X:%02X%02X:%02X%02X",
          address[0] & 0xFF, address[1] & 0xFF, address[2] & 0xFF, address[3] & 0xFF,
          address[4] & 0xFF, address[5] & 0xFF, address[6] & 0xFF, address[7] & 0xFF,
          address[8] & 0xFF, address[9] & 0xFF, address[10] & 0xFF, address[11] & 0xFF,
          address[12] & 0xFF, address[13] & 0xFF, address[14] & 0xFF, address[15] & 0xFF)
          .getBytes(StandardCharsets.UTF_8);
    }
    if (input instanceof InetAddress) {
      InetAddress ia = (InetAddress) input;

      byte[] address = ia.getAddress();
      if (address.length == 4) {
        return String.format("%d.%d.%d.%d", address[0] & 0xFF, address[1] & 0xFF, address[2] & 0xFF, address[3] & 0xFF)
            .getBytes(StandardCharsets.UTF_8);
      } else if (address.length == 16) {
        return String.format("%02X%02X:%02X%02X:%02X%02X:%02X%02X:%02X%02X:%02X%02X:%02X%02X:%02X%02X",
            address[0] & 0xFF, address[1] & 0xFF, address[2] & 0xFF, address[3] & 0xFF,
            address[4] & 0xFF, address[5] & 0xFF, address[6] & 0xFF, address[7] & 0xFF,
            address[8] & 0xFF, address[9] & 0xFF, address[10] & 0xFF, address[11] & 0xFF,
            address[12] & 0xFF, address[13] & 0xFF, address[14] & 0xFF, address[15] & 0xFF)
            .getBytes(StandardCharsets.UTF_8);
      }
      return ((String) input).getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a InetAddress to server");
  }

  /**
   * Converts an array of InetAddress objects to bytes to send to the database.
   * @param input an InetAddress array
   * @return bytes
   */
  public static byte[] fromCidrArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof InetAddress[]) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      baos.write('{');
      InetAddress[] in = (InetAddress[]) input;
      for (int i = 0; i < in.length; i++) {
        if (i != 0) {
          baos.write(',');
        }

        try {
          if (in[i] == null) {
            baos.write("NULL".getBytes(StandardCharsets.UTF_8));
          } else {
            baos.write(fromCidr(in[i]));
          }
        } catch (IOException e) {
          throw new RuntimeException("error converting array to byte array");
        }
      }
      baos.write('}');
      return baos.toByteArray();
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a InetAddress[] to server");
  }

  /**
   * Converts a Point object to something the server understands.
   *
   * @param input a Point
   * @return string representation of said point as bytes
   */
  public static byte[] fromPoint(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Point) {
      return ("(" + ((Point) input).getX() + "," + ((Point) input).getY() + ")").getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a Point to server");
  }

  /**
   * Converts a array of Point objects to something the server understands.
   *
   * @param input a Point array
   * @return string representation of said points as bytes
   */
  public static byte[] fromPointArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Point[]) {
      StringBuilder sb = new StringBuilder("{");
      Point[] in = (Point[]) input;
      for (int i = 0; i < in.length; i++) {
        if (i != 0) {
          sb.append(',');
        }

        if (in[i] == null) {
          sb.append("NULL");
        } else {
          sb.append('"').append(new String(fromPoint(in[i]))).append('"');
        }
      }
      sb.append('}');
      return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a Point[] to server");
  }

  /**
   * Converts a Line object to something the server understands.
   *
   * @param input a Line
   * @return string representation of said line as bytes
   */
  public static byte[] fromLine(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Line) {
      return ("{" + ((Line) input).getA() + "," + ((Line) input).getB() + ","
          + ((Line) input).getC() + "}").getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a Line to server");
  }

  /**
   * Converts a array of Line objects to something the server understands.
   *
   * @param input a Line array
   * @return string representation of said lines as bytes
   */
  public static byte[] fromLineArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Line[]) {
      StringBuilder sb = new StringBuilder("{");
      Line[] in = (Line[]) input;
      for (int i = 0; i < in.length; i++) {
        if (i != 0) {
          sb.append(',');
        }

        if (in[i] == null) {
          sb.append("NULL");
        } else {
          sb.append('"').append(new String(fromLine(in[i]))).append('"');
        }
      }
      sb.append('}');
      return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a Line[] to server");
  }

  /**
   * Converts a LineSegment object to something the server understands.
   *
   * @param input a LineSegment
   * @return a byte array
   */
  public static byte[] fromLineSegment(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof LineSegment) {
      LineSegment ls = (LineSegment) input;
      return ("(" + ls.getX1() + "," + ls.getY1() + ","
          + ls.getX2() + "," + ls.getY2() + ")").getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a LineSegment to server");
  }

  /**
   * Converts a array of LineSegment objects to something the server understands.
   *
   * @param input a LineSegment array
   * @return a byte array
   */
  public static byte[] fromLineSegmentArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof LineSegment[]) {
      StringBuilder sb = new StringBuilder("{");
      LineSegment[] in = (LineSegment[]) input;
      for (int i = 0; i < in.length; i++) {
        if (i != 0) {
          sb.append(',');
        }

        if (in[i] == null) {
          sb.append("NULL");
        } else {
          sb.append('"').append(new String(fromLineSegment(in[i]))).append('"');
        }
      }
      sb.append('}');
      return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a LineSegment[] to server");
  }

  /**
   * Converts a Box object to something the server understands.
   *
   * @param input a Box
   * @return a byte array
   */
  public static byte[] fromBox(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Box) {
      Box box = (Box) input;
      return ("(" + box.getX1() + "," + box.getY1() + ","
          + box.getX2() + "," + box.getY2() + ")").getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a Box to server");
  }

  /**
   * Converts a array of Box objects to something the server understands.
   *
   * @param input a Box array
   * @return a byte array
   */
  public static byte[] fromBoxArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Box[]) {
      StringBuilder sb = new StringBuilder("{");
      Box[] in = (Box[]) input;
      for (int i = 0; i < in.length; i++) {
        if (i != 0) {
          sb.append(';');
        }

        if (in[i] == null) {
          sb.append("NULL");
        } else {
          sb.append('"').append(new String(fromBox(in[i]))).append('"');
        }
      }
      sb.append('}');
      return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a Box[] to server");
  }

  /**
   * Converts a Path object to something the server understands.
   *
   * @param input a Path
   * @return a byte array
   */
  public static byte[] fromPath(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Path) {
      Path path = (Path) input;
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < path.getPoints().size(); i++) {
        if (i != 0) {
          sb.append(',');
        }

        sb.append("(").append(path.getPoints().get(i).getX()).append(",").append(path.getPoints().get(i).getY()).append(")");
      }
      if (path.isClosed()) {
        return ("(" + sb.toString() + ")").getBytes(StandardCharsets.UTF_8);
      } else {
        return ("[" + sb.toString() + "]").getBytes(StandardCharsets.UTF_8);
      }
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a Box to server");
  }

  /**
   * Converts a array of Path objects to something the server understands.
   *
   * @param input a Path array
   * @return a byte array
   */
  public static byte[] fromPathArray(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    if (input instanceof Path[]) {
      StringBuilder sb = new StringBuilder("{");
      Path[] in = (Path[]) input;
      for (int i = 0; i < in.length; i++) {
        if (i != 0) {
          sb.append(',');
        }

        if (in[i] == null) {
          sb.append("NULL");
        } else {
          sb.append('"').append(new String(fromPath(in[i]))).append('"');
        }
      }
      sb.append('}');
      return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    throw new RuntimeException(input.getClass().getName()
        + " can't be converted to byte[] to send as a Box[] to server");
  }
}
