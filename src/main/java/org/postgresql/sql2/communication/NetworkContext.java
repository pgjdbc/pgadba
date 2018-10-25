package org.postgresql.sql2.communication;

import jdk.incubator.sql2.ConnectionProperty;

import java.nio.channels.SocketChannel;
import java.util.Map;

/**
 * Context available to all {@link NetworkRequest} events.
 * 
 * @author Daniel Sagenschneider
 */
public interface NetworkContext {

  /**
   * Obtains the {@link SocketChannel}.
   * 
   * @return {@link SocketChannel}.
   */
  SocketChannel getSocketChannel();

  /**
   * Obtains the {@link ConnectionProperty} values.
   * 
   * @return {@link ConnectionProperty} values.
   */
  Map<ConnectionProperty, Object> getProperties();

  void startTls();
}