package org.postgresql.sql2.communication.network;

import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.communication.packets.CommandComplete;
import org.postgresql.sql2.communication.packets.DataRow;
import org.postgresql.sql2.operations.helpers.ParameterHolder;
import org.postgresql.sql2.util.PGCount;

import jdk.incubator.sql2.SqlException;

/**
 * Portal.
 * 
 * @author Daniel Sagenschneider
 */
public class Portal {

  public static void doHandleException(PGSubmission<?> submission, Throwable ex) {
    if (!(ex instanceof SqlException)) {
      ex = new SqlException(ex.getMessage(), ex, null, 0, null, 0);
    }
    Consumer<Throwable> errorHandler = submission.getErrorHandler();
    if (errorHandler != null) {
      errorHandler.accept(ex);
    }
    ((CompletableFuture) submission.getCompletionStage()).completeExceptionally(ex);
  }

  private static AtomicLong nameIndex = new AtomicLong(0);

  private final PGSubmission<?> submission;

  private String name;

  private long nextRowNumber = 0;

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

  /**
   * Handles the {@link Throwable}.
   * 
   * @param ex {@link Throwable}.
   */
  public void handleException(Throwable ex) {
    doHandleException(this.submission, ex);
  }

  /**
   * Obtains the possibly associated {@link Query}.
   * 
   * @return {@link Query}. May be <code>null</code>.
   */
  Query getQuery() {
    return this.query;
  }

  /**
   * Specifies the {@link Query}.
   * 
   * @param query {@link Query}.
   */
  void setQuery(Query query) {
    this.query = query;
  }

  /**
   * Obtains the next row number.
   * 
   * @return Next row number.
   */
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
   * @param complete      Command is complete.
   * @param socketChannel {@link SocketChannel}.
   */
  void commandComplete(CommandComplete complete, SocketChannel socketChannel) {
    switch (this.submission.getCompletionType()) {
    case COUNT:
      this.submission.finish(new PGCount(complete.getNumberOfRowsAffected()));
      break;
    case ROW:
      this.submission.finish(null);
      break;
    case CLOSE:
      this.submission.finish(socketChannel);
      break;
    case TRANSACTION:
      this.submission.finish(complete.getType());
      break;
    case ARRAY_COUNT:
      this.submission.finish(complete.getNumberOfRowsAffected());
      break;
    case VOID:
      ((CompletableFuture) this.submission.getCompletionStage()).complete(null);
      break;
    case PROCESSOR:
      this.submission.finish(null);
      break;
    case OUT_PARAMETER:
      this.submission.finish(null);
      break;
    default:
      throw new IllegalStateException("Invalid completion type '" + this.submission.getCompletionType() + "' for "
          + this.getClass().getSimpleName());
    }
  }

}