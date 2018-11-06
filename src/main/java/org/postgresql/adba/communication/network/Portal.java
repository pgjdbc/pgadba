package org.postgresql.adba.communication.network;

import static org.postgresql.adba.PgSubmission.Types.ARRAY_COUNT;

import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import jdk.incubator.sql2.SqlException;
import org.postgresql.adba.PgSubmission;
import org.postgresql.adba.communication.packets.CommandComplete;
import org.postgresql.adba.communication.packets.DataRow;
import org.postgresql.adba.communication.packets.ErrorPacket;
import org.postgresql.adba.communication.packets.parts.ErrorResponseField;
import org.postgresql.adba.operations.helpers.ParameterHolder;
import org.postgresql.adba.submissions.ArrayCountSubmission;
import org.postgresql.adba.util.PgCount;

/**
 * Portal.
 * 
 * @author Daniel Sagenschneider
 */
public class Portal {

  /**
   * Handle exception that occurred.
   *
   * @param submission the submission that was active when the exception happened
   * @param ex the exception
   */
  public static void doHandleException(PgSubmission<?> submission, Throwable ex) {
    if (ex instanceof ErrorPacket) {
      ErrorPacket e = (ErrorPacket)ex;
      int code = 0;
      if (e.getField(ErrorResponseField.Types.SQLSTATE_CODE) != null) {
        try {
          code = Integer.parseInt(e.getField(ErrorResponseField.Types.SQLSTATE_CODE));
        } catch (NumberFormatException ignore) {
          // ignored for now
        }
      }
      int position = 0;
      if (e.getField(ErrorResponseField.Types.POSITION) != null) {
        position = Integer.parseInt(e.getField(ErrorResponseField.Types.POSITION));
      }
      ex = new SqlException(e.getMessage(), e, e.getField(ErrorResponseField.Types.SEVERITY), code, null, position);
    }
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

  private final PgSubmission<?> submission;

  private String name;

  private long nextRowNumber = 0;

  /**
   * Thread safe as only accessed via network thread.
   */
  private Query query = null;

  /**
   * Instantiate.
   * 
   * @param submission {@link PgSubmission}.
   */
  public Portal(PgSubmission<?> submission) {
    name = "p" + nameIndex.incrementAndGet();
    this.submission = submission;
  }

  /**
   * Obtains the SQL.
   * 
   * @return SQL.
   */
  public String getSql() {
    return submission.getSql();
  }

  /**
   * Obtains the {@link ParameterHolder}.
   * 
   * @return {@link ParameterHolder}.
   */
  public ParameterHolder getParameterHolder() {
    return submission.getHolder();
  }

  /**
   * Obtains the portal name.
   * 
   * @return Portal name.
   */
  public String getPortalName() {
    return name;
  }

  /**
   * Handles the {@link Throwable}.
   * 
   * @param ex {@link Throwable}.
   */
  public void handleException(Throwable ex) {
    doHandleException(submission, ex);
  }

  /**
   * Obtains the possibly associated {@link Query}.
   * 
   * @return {@link Query}. May be <code>null</code>.
   */
  Query getQuery() {
    return query;
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
    return nextRowNumber++;
  }

  /**
   * Adds a data row.
   * 
   * @param dataRow {@link DataRow}.
   */
  void addDataRow(DataRow dataRow) {
    submission.addRow(dataRow);
  }

  /**
   * Flags the command is complete.
   * 
   * @param complete      Command is complete.
   * @param socketChannel {@link SocketChannel}.
   */
  void commandComplete(CommandComplete complete, SocketChannel socketChannel) {
    try {
      switch (submission.getCompletionType()) {
        case COUNT:
          submission.finish(new PgCount(complete.getNumberOfRowsAffected()));
          break;
        case ROW:
          submission.finish(null);
          break;
        case CLOSE:
          submission.finish(socketChannel);
          break;
        case TRANSACTION:
          submission.finish(complete.getType());
          break;
        case ARRAY_COUNT:
          submission.finish(complete.getNumberOfRowsAffected());
          break;
        case VOID:
          ((CompletableFuture) submission.getCompletionStage()).complete(null);
          break;
        case PROCESSOR:
          submission.finish(null);
          break;
        case OUT_PARAMETER:
          submission.finish(null);
          break;
        default:
          throw new IllegalStateException("Invalid completion type '" + submission.getCompletionType() + "' for "
              + this.getClass().getSimpleName());
      }
    } catch (Throwable t) {
      ((CompletableFuture<?>)submission.getCompletionStage()).completeExceptionally(t);
    }
  }

  /**
   * Some submission types needs multiple rounds of queries before the operation is finished. This function
   * returns true if more is needed.
   *
   * @return true if another query should be sent to the database
   * @throws ExecutionException if the bound variables are a future that fails
   * @throws InterruptedException if the bound variables are a future that fails
   */
  public boolean hasMoreToExecute() throws ExecutionException, InterruptedException {
    if (submission.getCompletionType() == ARRAY_COUNT) {
      return ((ArrayCountSubmission)submission).hasMoreToExecute();
    }
    return false;
  }
}