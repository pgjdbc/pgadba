package org.postgresql.adba;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import jdk.incubator.sql2.AdbaSessionProperty;
import jdk.incubator.sql2.SessionProperty;
import org.postgresql.adba.buffer.ByteBufferPool;
import org.postgresql.adba.execution.NioLoop;

public enum PgSessionProperty implements SessionProperty {
  /**
   * The host name of the server. Defaults to localhost. To specify an IPv6 address your must enclose the host parameter
   * with square brackets, for example:
   *
   * <p>jdbc:postgresql://[::1]:5740/accounting
   */
  HOST(String.class, "localhost", false),

  /**
   * The port number the server is listening on. Defaults to the PostgreSQL? standard port number (5432).
   */
  PORT(Integer.class, 5432, false),

  /**
   * The database name. The default is to connect to a database with the same name as the user name.
   */
  DATABASE(String.class, "", false),

  /**
   * Connect using SSL.
   */
  SSL(Boolean.class, false, false),

  /**
   * Sets SO_SNDBUF on the connection stream.
   */
  SEND_BUFFER_SIZE(Integer.class, 87380, false),

  /**
   * Sets SO_RCVBUF on the connection stream.
   */
  RECV_BUFFER_SIZE(Integer.class, 16384, false),

  /**
   * Set the amount of logging information printed to the DriverManager's current value for LogStream or LogWriter. It
   * currently supports values of org.postgresql.Driver.DEBUG (2) and org.postgresql.Driver.INFO (1). INFO will log very
   * little information while DEBUG will produce significant detail. This property is only really useful if you are a
   * developer or are having problems with the driver.
   */
  LOG_LEVEL(Level.class, Level.INFO, false),

  /**
   * The character set to use for data sent to the database or received from the database. This property is only relevant
   * for server versions less than or equal to 7.2. The 7.3 release was the first with multibyte support compiled by default
   * and the driver uses its character set translation facilities instead of trying to do it itself.
   */
  CHARSET(Charset.class, StandardCharsets.UTF_8, false),

  /**
   * Clients may leak Connection objects by failing to call its close() method. Eventually these objects will be garbage
   * collected and the finalize() method will be called which will close the Connection if caller has neglected to do
   * this himself. The usage of a finalizer is just a stopgap solution. To help developers detect and correct the source
   * of these leaks the logUnclosedConnections URL parameter has been added. It captures a stacktrace at each Connection
   * opening and if the finalize() method is reached without having been closed the stacktrace is printed to the log.
   */
  LOG_UNCLOSED_CONNECTIONS(Boolean.class, false, false),

  /**
   * Determine the number of queries that are cached in each connection. The default is 256, meaning if you use more than
   * 256 different queries in prepareStatement() calls, the least recently used ones will be discarded. The cache allows
   * application to benefit from ?Server Prepared Statements? prepareThreshold even if the prepared statement is closed
   * after each execution. The value of 0 disables the cache.
   *
   * <p>Each connection has its own statement cache.
   */
  PREPARED_STATEMENT_CACHE_QUERIES(Integer.class, 256, false),

  /**
   * Specify how long to wait for establishment of a database connection. The timeout is specified in seconds.
   */
  LOGIN_TIMEOUT(Integer.class, 0, false),

  /**
   * The timeout value used for socket connect operations. If connecting to the server takes longer than this value, the
   * connection is broken. The timeout is specified in seconds and a value of zero means that it is disabled. The default
   * value is 0 (unlimited) up to 9.4.1208, and 10 seconds since 9.4.1209
   */
  CONNECT_TIMEOUT(Integer.class, 0, false),

  /**
   * (since 9.4.1209) Cancel command is sent out of band over its own connection, so cancel message can itself get stuck.
   * This property controls "connect timeout" and "socket timeout" used for cancel commands. The timeout is specified in
   * seconds. Default value is 10 seconds.
   */
  CANCEL_SIGNAL_TIMEOUT(Integer.class, 10, false),

  /**
   * The timeout value used for socket read operations. If reading from the server takes longer than this value, the
   * connection is closed. This can be used as both a brute force global query timeout and a method of detecting network
   * problems. The timeout is specified in seconds and a value of zero means that it is disabled.
   */
  SOCKET_TIMEOUT(Integer.class, 0, false),

  /**
   * Enable or disable TCP keep-alive probe. The default is false.
   */
  TCP_KEEP_ALIVE(Boolean.class, false, false),

  /**
   * Specifies the name of the application that is using the connection. This allows a database administrator to see what
   * applications are connected to the server and what resources they are using through views like pgstatactivity.
   */
  APPLICATION_NAME(String.class, "java_pgadba", false),

  /**
   * Assume that the server is at least the given version, thus enabling to some optimization at connection time instead
   * of trying to be version blind.
   */
  ASSUME_MIN_SERVER_VERSION(Float.class, 10.0, false),

  /**
   * Specify the schema to be set in the search-path. This schema will be used to resolve unqualified object names used
   * in statements over this connection.
   */
  CURRENT_SCHEMA(String.class, "", false),

  /**
   * In default mode (disabled) hosts are connected in the given order. If enabled hosts are chosen randomly from the
   * set of suitable candidates.
   */
  LOAD_BALANCE_HOSTS(Boolean.class, false, false),

  /* TODO: settings below here are set by the server and should be moved into it's own class i think */
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
  TIMEZONE(String.class, "", false),

  /**
   * Allows specifying the {@link NioLoop}.
   */
  NIO_LOOP(String.class, null, false),
  
  /**
   * Allows specifying the {@link ByteBufferPool}.
   */
  BYTE_BUFFER_POOL(String.class, "", false);

  private Class range;
  private Object defaultValue;
  private boolean sensitive;

  PgSessionProperty(Class range, Object defaultValue, boolean sensitive) {
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
  public static SessionProperty lookup(String name) {
    for (PgSessionProperty prop : values()) {
      if (prop.toString().equalsIgnoreCase(name)) {
        return prop;
      }
    }

    for (AdbaSessionProperty prop : AdbaSessionProperty.values()) {
      if (prop.toString().equalsIgnoreCase(name)) {
        return prop;
      }
    }

    throw new IllegalArgumentException("no property with name: " + name);
  }
}
