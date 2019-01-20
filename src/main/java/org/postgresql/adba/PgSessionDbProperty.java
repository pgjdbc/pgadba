package org.postgresql.adba;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import jdk.incubator.sql2.SessionProperty;

/**
 * Properties on the session that is set by the server.
 */
public enum PgSessionDbProperty implements SessionProperty {
  /**
   * the charset that the server sets.
   */
  CLIENT_ENCODING(Charset.class, StandardCharsets.UTF_8, false),

  /**
   * the charset that the server uses.
   */
  SERVER_ENCODING(Charset.class, StandardCharsets.UTF_8, false),

  /**
   * Style of dates set by the server.
   */
  DATESTYLE(String.class, "", false),

  /**
   * If the server uses integer of floating point dates.
   */
  INTEGER_DATETIMES(String.class, "", false),

  /**
   * The format that intervals is sent in.
   */
  INTERVALSTYLE(String.class, "", false),

  /**
   * If we are connected as super user.
   */
  IS_SUPERUSER(String.class, "", false),

  /**
   * The server version we are connected to.
   */
  SERVER_VERSION(String.class, "", false),

  /**
   * The authorization method.
   */
  SESSION_AUTHORIZATION(String.class, "", false),

  /**
   * If strings are standard conforming.
   */
  STANDARD_CONFORMING_STRINGS(String.class, "", false),

  /**
   * The time zone of the server we connect to.
   */
  TIMEZONE(String.class, "", false);


  private Class range;
  private Object defaultValue;
  private boolean sensitive;

  PgSessionDbProperty(Class range, Object defaultValue, boolean sensitive) {
    this.range = range;
    this.defaultValue = defaultValue;
    this.sensitive = sensitive;
  }

  @Override
  public Class range() {
    return range;
  }

  @Override
  public Object defaultValue() {
    return defaultValue;
  }

  @Override
  public boolean isSensitive() {
    return sensitive;
  }

  /**
   * Returns the connection property that matches the supplied string.
   *
   * @param name name to search for
   * @return the matching property
   */
  public static PgSessionDbProperty lookup(String name) {
    for (PgSessionDbProperty prop : values()) {
      if (prop.toString().equalsIgnoreCase(name)) {
        return prop;
      }
    }

    throw new IllegalArgumentException("no property with name: " + name);
  }

}
