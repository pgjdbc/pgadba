package org.postgresql.sql2.communication.packets.parsers;

import org.postgresql.sql2.util.BinaryHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    return BinaryHelper.writeLong(((Number)input).longValue());
  }

  public static byte[] fromFloat(Object input) {
    return null;
  }

  public static byte[] fromDouble(Object input) {
    return null;
  }

  public static byte[] fromBigDecimal(Object input) {
    if(input instanceof Number) {
      return input.toString().getBytes(StandardCharsets.UTF_8);
    }
    return null;
  }

  public static byte[] fromChar(Object input) {
    if(input instanceof Character) {
      return ((Character) input).toString().getBytes(StandardCharsets.UTF_8);
    }
    return null;
  }

  public static byte[] fromString(Object input) {
    return ((String)input).getBytes(StandardCharsets.UTF_8);
  }

  public static byte[] fromLocalDate(Object input) {
    if(input instanceof LocalDate) {
      LocalDate x = (LocalDate) input;
      if (x == LocalDate.MAX) {
        return "infinity".getBytes(StandardCharsets.UTF_8);
      } else if (x == LocalDate.MIN) {
        return "-infinity".getBytes(StandardCharsets.UTF_8);
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try {
        baos.write(x.format(localDateFormatter).getBytes(StandardCharsets.UTF_8));

        if(x.getYear() < 0) {
          baos.write(" BC".getBytes(StandardCharsets.UTF_8));
        }

        return baos.toByteArray();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  public static byte[] fromLocalTime(Object input) {
    if(input instanceof LocalTime) {
      LocalTime x = (LocalTime) input;

      return x.format(localTimeFormatter).getBytes(StandardCharsets.UTF_8);
    }
    return null;
  }

  public static byte[] fromLocalDateTime(Object input) {
    if(input instanceof LocalDateTime) {
      LocalDateTime x = (LocalDateTime) input;
      if (x == LocalDateTime.MAX) {
        return "infinity".getBytes(StandardCharsets.UTF_8);
      } else if (x == LocalDateTime.MIN) {
        return "-infinity".getBytes(StandardCharsets.UTF_8);
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try {
        baos.write(x.format(localDateTimeFormatter).getBytes(StandardCharsets.UTF_8));

        if(x.getYear() < 0) {
          baos.write(" BC".getBytes(StandardCharsets.UTF_8));
        }

        return baos.toByteArray();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
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
    if(input instanceof OffsetTime) {
      OffsetTime x = (OffsetTime) input;

      return x.format(offsetTimeFormatter).getBytes(StandardCharsets.UTF_8);
    }
    return null;
  }

  public static byte[] fromOffsetDateTime(Object input) {
    if(input instanceof OffsetDateTime) {
      OffsetDateTime x = (OffsetDateTime) input;
      if (x == OffsetDateTime.MAX) {
        return "infinity".getBytes(StandardCharsets.UTF_8);
      } else if (x == OffsetDateTime.MIN) {
        return "-infinity".getBytes(StandardCharsets.UTF_8);
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try {
        baos.write(x.format(offsetDateTimeFormatter).getBytes(StandardCharsets.UTF_8));

        if(x.getYear() < 0) {
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
