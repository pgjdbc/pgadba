package org.postgresql.sql2.communication.network;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.postgresql.sql2.PgSubmission;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.packets.ErrorPacket;
import org.postgresql.sql2.communication.packets.parts.ErrorResponseField;

import jdk.incubator.sql2.SqlException;

/**
 * Abstract {@link PgSubmission} {@link NetworkResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractQueryResponse implements NetworkResponse {

  /**
   * Handle exception that occurred.
   *
   * @param submission the submission that was active when the exception happened
   * @param ex         the exception
   */
  public static void doHandleException(PgSubmission<?> submission, Throwable ex) {
    if (ex instanceof ErrorPacket) {
      ErrorPacket e = (ErrorPacket) ex;
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

  /**
   * {@link Query}.
   */
  protected final Query query;

  /**
   * Instantiate.
   * 
   * @param query {@link Query}.
   */
  public AbstractQueryResponse(Query query) {
    this.query = query;
  }

  @Override
  public NetworkResponse handleException(Throwable ex) {
    doHandleException(this.query.getSubmission(), ex);
    return new ReadyForQueryResponse();
  }
}