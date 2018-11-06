package org.postgresql.adba.communication;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link OutputStream} to the {@link NetworkConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class NetworkOutputStream extends OutputStream {

  /**
   * Initialises the packet.
   * 
   * @throws IOException If fails to initialise the packet.
   */
  public abstract void initPacket() throws IOException;

  /**
   * Writes text into the packet.
   * 
   * @param text Text.
   * @throws IOException If fails to write the text.
   */
  public abstract void write(String text) throws IOException;

  /**
   * Writes the terminator.
   * 
   * @throws IOException If fails to write the terminator.
   */
  public void writeTerminator() throws IOException {
    this.write(0);
  }

  /**
   * Completes the packet.
   * 
   * @throws IOException If fails to complete the packet.
   */
  public abstract void completePacket() throws IOException;

}