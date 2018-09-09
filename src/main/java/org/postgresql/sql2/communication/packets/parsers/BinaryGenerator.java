package org.postgresql.sql2.communication.packets.parsers;

import java.util.Arrays;
import org.postgresql.sql2.util.BinaryHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

public class BinaryGenerator {
  private static final DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter localTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.nnnnnnnnn");
  private static final DateTimeFormatter localDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnnnnn");
  private static final DateTimeFormatter offsetTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.nnnnnnnnn X");
  private static final DateTimeFormatter offsetDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnnnnn X");

  public static byte[] fromBit(Object input) {
    return null;
  }

  public static byte[] fromTinyInt(Object input) {
    return null;
  }

  /**
   * parses Number to a short represented as a byte array.
   * @param input the Number to convert
   * @return a byte array of length 0 or 2
   */
  public static byte[] fromSmallInt(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    return BinaryHelper.writeShort(((Number)input).shortValue());
  }

  /**
   * parses Number to a int represented as a byte array.
   * @param input the Number to convert
   * @return a byte array of length 0 or 4
   */
  public static byte[] fromInt(Object input) {
    if (input == null) {
      return new byte[]{};
    }

    return BinaryHelper.writeInt(((Number)input).intValue());
  }

  /**
   * parses Number to a long represented as a byte array.
   * @param input the Number to convert
   * @return a byte array of length 8
   */
  public static byte[] fromBigInt(Object input) {
    return BinaryHelper.writeLong(((Number)input).longValue());
  }

  /**
   * parses Float to a byte array.
   * @param input the Float to convert
   * @return a byte array of length 4 or null
   */
  public static byte[] fromFloat(Object input) {
    if (input instanceof Float) {
      return ByteBuffer.allocate(4).putFloat((Float) input).array();
    }
    return null;
  }

  /**
   * parses an array of Floats to a byte array.
   * @param input the Float[] to convert
   * @return a byte array
   */
  public static byte[] fromFloatArray(Object input) {
    if (input instanceof Float[]) {
      Float[] in = (Float[]) input;
      int size = 20 + in.length * 8;
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
        BinaryHelper.writeIntAtPos(4, pos, data);
        pos += 4;
        BinaryHelper.writeFloatAtPos(in[i], pos, data);
        pos += 4;
      }
      return data;
    }

    return null;
  }

  /**
   * parses Double to a byte array.
   * @param input the Double to convert
   * @return a byte array of length 8 or null
   */
  public static byte[] fromDouble(Object input) {
    if (input instanceof Double) {
      return ByteBuffer.allocate(8).putDouble((Double) input).array();
    }
    return null;
  }

  /**
   * parses a BigDecimal to a byte array.
   * @param input the BigDecimal to convert
   * @return a byte array of appropriate length or null
   */
  public static byte[] fromBigDecimal(Object input) {
    if (input instanceof Number) {
      return input.toString().getBytes(StandardCharsets.UTF_8);
    }
    return null;
  }

  /**
   * parses a Character to a byte array.
   * @param input the Character to convert
   * @return a byte array
   */
  public static byte[] fromChar(Object input) {
    if (input instanceof Character) {
      return ((Character) input).toString().getBytes(StandardCharsets.UTF_8);
    }
    return null;
  }

  /**
   * parses a Character to a byte array.
   * @param input the Character to convert
   * @return a byte array
   */
  public static byte[] fromString(Object input) {
    return ((String)input).getBytes(StandardCharsets.UTF_8);
  }

  /**
   * parses an array into to a byte array.
   * @param input the Array to convert
   * @return a byte array
   */
  public static byte[] fromByteaArray(Object input) {
    if (input instanceof byte[][]) {
      byte[][] in = (byte[][]) input;
      int size = 20 + in.length * 4 + Arrays.stream(in).mapToInt(a -> a.length).sum();
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
        BinaryHelper.writeIntAtPos(in[i].length, pos, data);
        pos += 4;
        System.arraycopy(in[i], 0, data, pos, in[i].length);
        pos += in[i].length;
      }
      return data;
    }

    return new byte[] {};
  }

  /**
   * parses an array into to a byte array.
   * @param input the Array to convert
   * @return a byte array
   */
  public static byte[] fromShortArray(Object input) {
    if (input instanceof Short[]) {
      Short[] in = (Short[]) input;
      int size = 20 + in.length * 6;
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
        BinaryHelper.writeIntAtPos(2, pos, data);
        pos += 4;
        BinaryHelper.writeShortAtPos(in[i], pos, data);
        pos += 2;
      }
      return data;
    }

    return new byte[] {};
  }

  /**
   * parses an array into to a byte array.
   * @param input the Array to convert
   * @return a byte array
   */
  public static byte[] fromIntegerArray(Object input) {
    if (input instanceof Integer[]) {
      Integer[] in = (Integer[]) input;
      int size = 20 + in.length * 8;
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
        BinaryHelper.writeIntAtPos(4, pos, data);
        pos += 4;
        BinaryHelper.writeIntAtPos(in[i], pos, data);
        pos += 4;
      }
      return data;
    }

    return new byte[] {};
  }

  /**
   * parses a LocalDate to a byte array.
   * @param input the LocalDate to convert
   * @return a byte array
   */
  public static byte[] fromLocalDate(Object input) {
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
        e.printStackTrace();
      }
    }
    return null;
  }

  /**
   * parses a LocalTime to a byte array.
   * @param input the LocalTime to convert
   * @return a byte array
   */
  public static byte[] fromLocalTime(Object input) {
    if (input instanceof LocalTime) {
      LocalTime x = (LocalTime) input;

      return x.format(localTimeFormatter).getBytes(StandardCharsets.UTF_8);
    }
    return null;
  }

  /**
   * parses a LocalDateTime to a byte array.
   * @param input the LocalDateTime to convert
   * @return a byte array
   */
  public static byte[] fromLocalDateTime(Object input) {
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
        e.printStackTrace();
      }
    }
    return null;
  }

  /**
   * converts what the user sends for a binary blog to what the server wants as a binary blob
   * (which are the same things).
   * @param input data from the user
   * @return a byte array containing the information
   */
  public static byte[] fromByteArray(Object input) {
    if (input instanceof byte[]) {
      return (byte[])input;
    }

    return null;
  }

  public static byte[] fromNull(Object input) {
    return new byte[] {};
  }

  public static byte[] fromJavaObject(Object input) {
    return null;
  }

  /**
   * parses a Boolean to a byte array.
   * @param input the Boolean to convert
   * @return a byte array
   */
  public static byte[] fromBoolean(Object input) {
    if (input instanceof Boolean) {
      if ((Boolean)input) {
        return new byte[]{1};
      } else {
        return new byte[]{0};
      }
    }
    return null;
  }

  public static byte[] fromXml(Object input) {
    return null;
  }

  /**
   * parses a OffsetTime to a byte array.
   * @param input the OffsetTime to convert
   * @return a byte array
   */
  public static byte[] fromOffsetTime(Object input) {
    if (input instanceof OffsetTime) {
      OffsetTime x = (OffsetTime) input;

      return x.format(offsetTimeFormatter).getBytes(StandardCharsets.UTF_8);
    }
    return null;
  }

  /**
   * parses a OffsetDateTime to a byte array.
   * @param input the OffsetDateTime to convert
   * @return a byte array
   */
  public static byte[] fromOffsetDateTime(Object input) {
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
        e.printStackTrace();
      }
    }
    return null;
  }
}
