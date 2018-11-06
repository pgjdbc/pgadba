package org.postgresql.adba.execution;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 * Context for the {@link NioService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface NioServiceContext {

  /**
   * Obtains the {@link SelectableChannel}.
   * 
   * @return {@link SelectableChannel}.
   */
  SelectableChannel getChannel();

  /**
   * Flags that a write is required.
   */
  void writeRequired();

  /**
   * Sets the interested operations as per {@link SelectionKey}.
   * 
   * @param interestedOps Interested operations as per {@link SelectionKey}.
   * @throws IOException If fails to set operations.
   */
  void setInterestedOps(int interestedOps) throws IOException;

  /**
   * Unregisters from the {@link NioLoop}.
   * 
   * @throws IOException If fails to unregister.
   */
  void unregister() throws IOException;
}
