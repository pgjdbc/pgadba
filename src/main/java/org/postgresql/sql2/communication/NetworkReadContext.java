package org.postgresql.sql2.communication;

/**
 * Context for writing to the network.
 * 
 * @author Daniel Sagenschneider
 */
public interface NetworkReadContext extends NetworkContext {

  /**
   * Obtains the {@link BEFrame} just read.
   * 
   * @return {@link BEFrame} just read.
   */
  BEFrame getBEFrame();

  /**
   * Triggers for a write.
   */
  void writeRequired();

}