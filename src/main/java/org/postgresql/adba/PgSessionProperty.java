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
   * Connect using SSL. The driver must have been compiled with SSL support. This property does not need a value associated
   * with it. The mere presence of it specifies a SSL connection. However, for compatibility with future versions, the value
   * "true" is preferred. For more information see Chapter 4, Using SSL.
   */
  SSL(Boolean.class, false, false),

  /**
   * The provided value is a class name to use as the SSLSocketFactory when establishing a SSL connection. For more
   * information see the section called ?Custom SSLSocketFactory?.
   */
  SSL_FACTORY(Class.class, null, false),

  /**
   * This value is an optional argument to the constructor of the ssl factory class provided above. For more information
   * see the section called ?Custom SSLSocketFactory?.
   */
  SSL_FACTORY_ARG(String.class, "", false),

  /**
   * Act like an older version of the driver to retain compatibility with older applications. At the moment this controls
   * two driver behaviours: the handling of binary data fields, and the handling of parameters set via setString().
   *
   * <p>Older versions of the driver used this property to also control the protocol used to connect to the backend. This
   * is now controlled by the protocolVersion property.
   *
   * <p>Information on binary data handling is detailed in Chapter 7, Storing Binary Data. To force the use of Large Objects
   * set the compatible property to 7.1.
   *
   * <p>When compatible is set to 7.4 or below, the default for the string type parameter is changed to unspecified.
   */
  COMPATIBLE(Float.class, 10.0, false),

  /**
   * Sets SO_SNDBUF on the connection stream.
   */
  SEND_BUFFER_SIZE(Integer.class, 87380, false),

  /**
   * Sets SO_RCVBUF on the connection stream.
   */
  RECV_BUFFER_SIZE(Integer.class, 16384, false),

  /**
   * The driver supports both the V2 and V3 frontend/backend protocols. The V3 protocol was introduced in 7.4 and the
   * driver will by default try to connect using the V3 protocol, if that fails it will fall back to the V2 protocol.
   * If the protocolVersion property is specified, the driver will try only the specified protocol (which should be
   * either "2" or "3"). Setting protocolVersion to "2" may be used to avoid the failed attempt to use the V3 protocol
   * when connecting to a version 7.3 or earlier server, or to force the driver to use the V2 protocol despite connecting
   * to a 7.4 or greater server.
   */
  PROTOCOL_VERSION(Integer.class, 3, false),

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
   * When using the V3 protocol the driver monitors changes in certain server configuration parameters that should not
   * be touched by end users. The client_encoding setting is set by the driver and should not be altered. If the driver
   * detects a change it will abort the connection. There is one legitimate exception to this behaviour though, using the
   * COPY command on a file residing on the server's filesystem. The only means of specifying the encoding of this file
   * is by altering the client_encoding setting. The JDBC team considers this a failing of the COPY command and hopes to
   * provide an alternate means of specifying the encoding in the future, but for now there is this URL parameter. Enable
   * this only if you need to override the client encoding when doing a copy.
   */
  ALLOW_ENCODING_CHANGES(Boolean.class, false, false),

  /**
   * Clients may leak Connection objects by failing to call its close() method. Eventually these objects will be garbage
   * collected and the finalize() method will be called which will close the Connection if caller has neglected to do
   * this himself. The usage of a finalizer is just a stopgap solution. To help developers detect and correct the source
   * of these leaks the logUnclosedConnections URL parameter has been added. It captures a stacktrace at each Connection
   * opening and if the finalize() method is reached without having been closed the stacktrace is printed to the log.
   */
  LOG_UNCLOSED_CONNECTIONS(Boolean.class, false, false),

  /**
   * A list of types to enable binary transfer. Either OID numbers or names.
   */
  BINARY_TRANSFER_ENABLE(String[].class, new String[]{}, false),

  /**
   * A list of types to disable binary transfer. Either OID numbers or names. Overrides values in the driver
   * default set and values set with binaryTransferEnable.
   */
  BINARY_TRANSFER_DISABLE(String[].class, new String[]{}, false),

  /**
   * Determine the number of PreparedStatement executions required before switching over to use server side prepared statements.
   * The default is five, meaning start using server side prepared statements on the fifth execution of the same
   * PreparedStatement object. More information on server side prepared statements is available in the section called
   * ?Server Prepared Statements?.
   */
  PREPARE_THRESHOLD(Integer.class, 5, false),

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
   * Determine the maximum size (in mebibytes) of the prepared queries cache (see preparedStatementCacheQueries). The
   * default is 5, meaning if you happen to cache more than 5 MiB of queries the least recently used ones will be discarded.
   * The main aim of this setting is to prevent OutOfMemoryError. The value of 0 disables the cache. If a query would
   * consume more than a half of preparedStatementCacheSizeMiB, then it is discarded immediately.
   */
  PREPARED_STATEMENT_CACHE_SIZE_MIB(Integer.class, 5, false),

  /**
   * Determine the number of rows fetched in ResultSet by one fetch with trip to the database. Limiting the number of rows
   * are fetch with each trip to the database allow avoids unnecessary memory consumption and as a consequence
   * OutOfMemoryException The default is zero, meaning that in ResultSet will be fetch all rows at once.
   * Negative number is not available.
   */
  DEFAULT_ROW_FETCH_SIZE(Integer.class, 0, false),

  /**
   * Enable optimization to rewrite and collapse compatible INSERT statements that are batched. If enabled, pgjdbc
   * rewrites batch of insert into ... values(?, ?) into insert into ... values(?, ?), (?, ?), ... That reduces per-statement
   * overhead. The drawback is if one of the statements fail, the whole batch fails. The default value is false. The option
   * is available since 9.4.1208
   */
  REWRITE_BATCHED_INSERTS(Boolean.class, false, false),

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
   * Certain postgresql types such as TEXT do not have a well defined length. When returning meta-data about these types
   * through functions like ResultSetMetaData.getColumnDisplaySize and ResultSetMetaData.getPrecision we must provide a
   * value and various client tools have different ideas about what they would like to see. This parameter specifies the
   * length to return for types of unknown length.
   */
  UNKNOWN_LENGTH(Integer.class, Integer.MAX_VALUE, false),

  /**
   * Specify the type to use when binding PreparedStatement parameters set via setString(). If stringtype is set to VARCHAR
   * (the default), such parameters will be sent to the server as varchar parameters. If stringtype is set to unspecified,
   * parameters will be sent to the server as untyped values, and the server will attempt to infer an appropriate type.
   * This is useful if you have an existing application that uses setString() to set parameters that are actually some
   * other type, such as integers, and you are unable to change the application to use an appropriate method such as setInt().
   */
  STRING_TYPE(String.class, "VARCHAR", false),

  /**
   * The Kerberos service name to use when authenticating with GSSAPI. This is equivalent to libpq's PGKRBSRVNAME
   * environment variable and defaults to "postgres".
   */
  KERBEROS_SERVER_NAME(String.class, "postgres", false),

  /**
   * Specifies the name of the JAAS system or application login configuration.
   */
  JAAS_APPLICATION_NAME(String.class, "", false),

  /**
   * Specifies the name of the application that is using the connection. This allows a database administrator to see what
   * applications are connected to the server and what resources they are using through views like pgstatactivity.
   */
  APPLICATION_NAME(String.class, "java_pgadba", false),

  /**
   * Force either SSPI (Windows transparent single-sign-on) or GSSAPI (Kerberos, via JSSE) to be used when the server
   * requests Kerberos or SSPI authentication. Permissible values are auto (default, see below), sspi (force SSPI) or
   * gssapi (force GSSAPI-JSSE).
   *
   * <p>If this parameter is auto, SSPI is attempted if the server requests SSPI authentication, the JDBC client is running
   * on Windows, and the Waffle libraries required for SSPI are on the CLASSPATH. Otherwise Kerberos/GSSAPI via JSSE
   * is used. Note that this behaviour does not exactly match that of libpq, which uses Windows' SSPI libraries for
   * Kerberos (GSSAPI) requests by default when on Windows.
   *
   * <p>gssapi mode forces JSSE's GSSAPI to be used even if SSPI is available, matching the pre-9.4 behaviour.
   *
   * <p>On non-Windows platforms or where SSPI is unavailable, forcing sspi mode will fail with a PSQLException.
   */
  GSS_LIB(String.class, "auto", false),

  /**
   * Specifies the name of the Windows SSPI service class that forms the service class part of the SPN. The default,
   * POSTGRES, is almost always correct.
   *
   * <p>See: SSPI authentication (Pg docs) Service Principal Names (MSDN), DsMakeSpn (MSDN) Configuring SSPI (Pg wiki).
   *
   * <p>This parameter is ignored on non-Windows platforms.
   */
  SSPI_SERVICE_CLASS(String.class, "POSTGRES", false),

  /**
   * Use SPNEGO in SSPI authentication requests.
   */
  USE_SPNEGO(Boolean.class, false, false),

  /**
   * Put the connection in read-only mode.
   */
  READ_ONLY(Boolean.class, false, false),

  /**
   * Enable optimization that disables column name sanitiser.
   */
  DISABLE_COLUMN_SANITISER(Boolean.class, false, false),

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
   * Allows opening connections to only servers with required state, the allowed values are any, master, slave and
   * preferSlave. The master/slave distinction is currently done by observing if the server allows writes. The value
   * preferSlave tries to connect to slaves if any are available, otherwise allows falls back to connecting also to master.
   */
  TARGET_SERVER_TYPE(String.class, "any", false),

  /**
   * Controls how long in seconds the knowledge about a host state is cached in JVM wide global cache.
   * The default value is 10 seconds.
   */
  HOST_RECHECK_SECONDS(Integer.class, 10, false),

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
