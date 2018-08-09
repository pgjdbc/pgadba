package org.postgresql.sql2.communication.network;

import java.util.concurrent.atomic.AtomicLong;

import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.communication.packets.CommandComplete;
import org.postgresql.sql2.communication.packets.DataRow;
import org.postgresql.sql2.operations.helpers.ParameterHolder;

/**
 * Portal.
 * 
 * @author Daniel Sagenschneider
 */
public class Portal {

  private static AtomicLong nameIndex = new AtomicLong(0);

  private final PGSubmission<?> submission;

  private String name;

  private long nextRowNumber = 1;

  /**
   * Thread safe as only accessed via network thread.
   */
  private Query query = null;

  /**
   * Instantiate.
   * 
   * @param submission {@link PGSubmission}.
   */
  public Portal(PGSubmission<?> submission) {
    this.name = "p" + nameIndex.incrementAndGet();
    this.submission = submission;
  }

  /**
   * Obtains the SQL.
   * 
   * @return SQL.
   */
  public String getSql() {
    return this.submission.getSql();
  }

  /**
   * Obtains the {@link ParameterHolder}.
   * 
   * @return {@link ParameterHolder}.
   */
  public ParameterHolder getParameterHolder() {
    return this.submission.getHolder();
  }

  /**
   * Obtains the portal name.
   * 
   * @return Portal name.
   */
  public String getPortalName() {
    return this.name;
  }

  Query getQuery() {
    return this.query;
  }

  void setQuery(Query query) {
    this.query = query;
  }

  long nextRowNumber() {
    return this.nextRowNumber++;
  }

  /**
   * Adds a data row.
   * 
   * @param dataRow {@link DataRow}.
   */
  void addDataRow(DataRow dataRow) {
    this.submission.addRow(dataRow);
  }

  /**
   * Flags the command is complete.
   * 
   * @param complete Command is complete.
   */
  void commandComplete(CommandComplete complete) {
    this.submission.finish(complete);
  }

}