package org.postgresql.sql2.communication.network;

import java.util.concurrent.atomic.AtomicInteger;

import org.postgresql.sql2.communication.packets.RowDescription;

/**
 * Query.
 * 
 * @author Daniel Sagenschneider
 */
public class Query {

  /**
   * Index for unique names.
   */
  private static AtomicInteger nameIndex = new AtomicInteger(0);

  /**
   * Name for the {@link Query}.
   */
  private final String name;

  /**
   * Indicates whether parsed.
   */
  private boolean isParsed = false;

  /**
   * Indicates if waiting parse.
   */
  private boolean isAwaitingParse = false;

  /**
   * {@link RowDescription}.
   */
  private RowDescription rowDescription = null;

  /**
   * Instantiate.
   */
  public Query() {
    this.name = "q" + nameIndex.incrementAndGet();
  }

  /**
   * Obtains the name.
   * 
   * @return Name.
   */
  public String getQueryName() {
    return this.name;
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
   * Obtains the {@link RowDescription}.
   * 
   * @return {@link RowDescription}.
   */
  RowDescription getRowDescription() {
    return this.rowDescription;
  }

  /**
   * Specifies the {@link RowDescription}.
   * 
   * @param rowDescription {@link RowDescription}.
   */
  void setRowDescription(RowDescription rowDescription) {
    this.rowDescription = rowDescription;
  }

}