package org.postgresql.sql2.communication;

import java.nio.channels.SocketChannel;
import java.util.Map;
import jdk.incubator.sql2.SessionProperty;

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
   * Obtains the {@link SessionProperty} values.
   * 
   * @return {@link SessionProperty} values.
   */
  Map<SessionProperty, Object> getProperties();

  void startTls();
}