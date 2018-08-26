package org.postgresql.sql2.execution;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;

import jdk.incubator.sql2.Operation;

/**
 * Provides an event loop for servicing communication.
 *
 * <p>This allows plugging in different {@link NioLoop} implementations. For
 * example, the same {@link Selector} can be used for both asynchronous database
 * {@link Operation} and HTTP servicing by the web application.
 * 
 * @author Daniel Sagenschneider
 */
public interface NioLoop {

  /**
   * Registers an {@link NioService}.
   * 
   * @param channel           {@link SelectableChannel}.
   * @param nioServiceFactory {@link NioServiceFactory} to create the
   *                          {@link NioService}.
   * @return {@link NioService} registered.
   * @throws IOException If fails to register {@link NioService}.
   */
  NioService registerNioService(SelectableChannel channel, NioServiceFactory nioServiceFactory) throws IOException;

}