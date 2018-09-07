package org.postgresql.sql2.communication.network;

import org.postgresql.sql2.communication.packets.RowDescription;

/**
 * Re-use of an SQL query.
 * 
 * @author Daniel Sagenschneider
 */
public class QueryReuse {

  /**
   * {@link RowDescription}.
   */
  private RowDescription rowDescription = null;

  /**
   * Number of times this {@link Query} has been executed.
   */
  private int executeCount = 10; // TODO reset to start at 0

  /**
   * {@link Portal}.
   */
  private Portal portal = new Portal(); // TODO reset to create

  /**
   * Indicates whether parsed.
   */
  private boolean isParsed = false;

  /**
   * Indicates if waiting parse.
   */
  private boolean isAwaitingParse = false;

  /**
   * Indicates whether described.
   */
  private boolean isDescribed = false;

  /**
   * Indicates if waiting describe.
   */
  private boolean isAwaitingDescribe = false;

  /**
   * Indicates if execute as simple query.
   * 
   * @return <code>true</code> to execute as simple query.
   */
  public boolean isSimpleQuery() {
    return this.executeCount < 5; // TODO allow configuring
  }

  /**
   * Specifies the {@link RowDescription}.
   * 
   * @param rowDescription {@link RowDescription}.
   */
  public void setRowDescription(RowDescription rowDescription) {
    this.rowDescription = rowDescription;
  }

  /**
   * Obtains the {@link RowDescription}.
   * 
   * @return {@link RowDescription}.
   */
  public RowDescription getRowDescription() {
    return this.rowDescription;
  }

  /**
   * Indicates if parsed.
   * 
   * @return Parsed.
   */
  public boolean isParsed() {
    return this.isParsed;
  }

  /**
   * Flags that the query has parsed.
   */
  void flagParsed() {
    this.isParsed = true;
  }

  /**
   * Indicates if waiting on parse.
   * 
   * @return Waiting on parse.
   */
  public boolean isWaitingParse() {
    return this.isAwaitingParse;
  }

  /**
   * Flags that waiting on parse.
   */
  void flagWaitingParse() {
    this.isAwaitingParse = true;
  }

  /**
   * Indicates if waiting on describe.
   * 
   * @return Waiting on describe.
   */
  public boolean isWaitingDescribe() {
    return this.isAwaitingDescribe;
  }

  /**
   * Flags that waiting on describe.
   */
  void flagWaitingDescribe() {
    this.isAwaitingDescribe = true;
  }

  /**
   * Obtains the {@link Portal}.
   * 
   * @return {@link Portal}.
   */
  public Portal getPortal() {
    return this.portal;
  }

  /**
   * Convenience method to obtain the {@link Portal} name.
   * 
   * @return {@link Portal} name or unnamed name if no {@link Portal}.
   */
  public String getPortalNameOrUnnamed() {
    return this.portal == null ? "" : this.portal.getPortalName();
  }

}