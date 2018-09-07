package org.postgresql.sql2.communication.network;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Portal.
 * 
 * @author Daniel Sagenschneider
 */
public class Portal {

  private static AtomicLong nameIndex = new AtomicLong(0);

  private final String name;

  /**
   * Instantiate.
   */
  public Portal() {
    this.name = "p" + nameIndex.incrementAndGet();
  }

  /**
   * Obtains the portal name.
   * 
   * @return Portal name.
   */
  public String getPortalName() {
    return this.name;
  }

}