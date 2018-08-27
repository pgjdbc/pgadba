package org.postgresql.sql2;

import jdk.incubator.sql2.AdbaConnectionProperty;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.ConnectionProperty;
import org.postgresql.sql2.exceptions.PropertyException;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PgConnectionBuilder implements Connection.Builder {
  private Map<ConnectionProperty, Object> properties = new HashMap<>();
  private PgDataSource dataSource;

  /**
   * Creates a builder for the supplied dataSource.
   *
   * @param dataSource dataSource that the created connections should be a part of.
   */
  public PgConnectionBuilder(PgDataSource dataSource) {
    this.dataSource = dataSource;
    for (PgConnectionProperties prop : PgConnectionProperties.values()) {
      properties.put(prop, prop.defaultValue());
    }

    for (Map.Entry<ConnectionProperty, Object> prop : dataSource.getProperties().entrySet()) {
      properties.put(prop.getKey(), prop.getValue());
    }
  }

  @Override
  public Connection.Builder property(ConnectionProperty p, Object v) {
    if (!(p instanceof PgConnectionProperties)) {
      throw new PropertyException("Please make sure that the ConnectionProperty is of type PGConnectionProperties");
    }

    if (!(v.getClass().isAssignableFrom(p.range()))) {
      throw new PropertyException("Please make sure that the ConnectionProperty is of type PGConnectionProperties");
    }

    properties.put(p, v);

    return this;
  }

  @Override
  public Connection build() {
    Map<ConnectionProperty, Object> props = parseUrl((String) properties.get(AdbaConnectionProperty.URL), null);

    if (props != null) {
      for (Map.Entry<ConnectionProperty, Object> prop : props.entrySet()) {
        properties.put(prop.getKey(), prop.getValue());
      }
    }

    try {
      PgConnection connection = new PgConnection(properties, this.dataSource, this.dataSource.getNioLoop(),
          this.dataSource.getByteBufferPool());
      dataSource.registerConnection(connection);
      return connection;
    } catch (IOException ex) {
      throw new IllegalStateException("Failure opening connection", ex);
    }
  }

  /**
   * Parses the postgresql connection url that the user supplies to the library.
   * @param url url string
   * @param defaults the default values that's used if the user doesn't override them
   * @return a map of properties
   */
  public static Map<ConnectionProperty, Object> parseUrl(String url, Properties defaults) {
    Map<ConnectionProperty, Object> urlProps = new HashMap<>();

    String urlServer = url;
    String urlArgs = "";

    int qPos = url.indexOf('?');
    if (qPos != -1) {
      urlServer = url.substring(0, qPos);
      urlArgs = url.substring(qPos + 1);
    }

    if (!urlServer.startsWith("jdbc:postgresql:")) {
      return null;
    }
    urlServer = urlServer.substring("jdbc:postgresql:".length());

    if (urlServer.startsWith("//")) {
      urlServer = urlServer.substring(2);
      int slash = urlServer.indexOf('/');
      if (slash == -1) {
        return null;
      }
      urlProps.put(PgConnectionProperties.DATABASE,
          URLDecoder.decode(urlServer.substring(slash + 1), StandardCharsets.UTF_8));

      String[] addresses = urlServer.substring(0, slash).split(",");
      StringBuilder hosts = new StringBuilder();
      StringBuilder ports = new StringBuilder();
      for (String address : addresses) {
        int portIdx = address.lastIndexOf(':');
        if (portIdx != -1 && address.lastIndexOf(']') < portIdx) {
          String portStr = address.substring(portIdx + 1);
          try {
            // squid:S2201 The return value of "parseInt" must be used.
            // The side effect is NumberFormatException, thus ignore sonar error here
            Integer.parseInt(portStr); // NOSONAR
          } catch (NumberFormatException ex) {
            return null;
          }
          ports.append(portStr);
          hosts.append(address.subSequence(0, portIdx));
        } else {
          ports.append("5432");
          hosts.append(address);
        }
        ports.append(',');
        hosts.append(',');
      }
      ports.setLength(ports.length() - 1);
      hosts.setLength(hosts.length() - 1);
      urlProps.put(PgConnectionProperties.PORT, Integer.parseInt(ports.toString()));
      urlProps.put(PgConnectionProperties.HOST, hosts.toString());
    } else {
      /*
       * if there are no defaults set or any one of PORT, HOST, DBNAME not set then
       * set it to default
       */
      if (defaults == null || !defaults.containsKey(PgConnectionProperties.PORT.name())) {
        urlProps.put(PgConnectionProperties.PORT, 5432);
      }
      if (defaults == null || !defaults.containsKey(PgConnectionProperties.HOST.name())) {
        urlProps.put(PgConnectionProperties.HOST, "localhost");
      }
      if (defaults == null || !defaults.containsKey(PgConnectionProperties.DATABASE.name())) {
        urlProps.put(PgConnectionProperties.DATABASE, URLDecoder.decode(urlServer, StandardCharsets.UTF_8));
      }
    }

    // parse the args part of the url
    String[] args = urlArgs.split("&");
    for (String token : args) {
      if (token.isEmpty()) {
        continue;
      }
      int pos = token.indexOf('=');
      if (pos == -1) {
        urlProps.put(PgConnectionProperties.lookup(token), "");
      } else {
        urlProps.put(PgConnectionProperties.lookup(token.substring(0, pos)),
            URLDecoder.decode(token.substring(pos + 1), StandardCharsets.UTF_8));
      }
    }

    return urlProps;
  }
}
