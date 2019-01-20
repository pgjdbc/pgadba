package org.postgresql.adba.communication;

import java.nio.channels.SocketChannel;
import jdk.incubator.sql2.SessionProperty;
import org.postgresql.adba.util.PropertyHolder;

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
  PropertyHolder getProperties();

  void startTls();
}