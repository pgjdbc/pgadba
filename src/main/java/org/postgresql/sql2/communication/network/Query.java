package org.postgresql.sql2.communication.network;

import static org.postgresql.sql2.PgSubmission.Types.ARRAY_COUNT;

import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.postgresql.sql2.PgSubmission;
import org.postgresql.sql2.communication.packets.CommandComplete;
import org.postgresql.sql2.communication.packets.DataRow;
import org.postgresql.sql2.submissions.ArrayCountSubmission;
import org.postgresql.sql2.util.PgCount;

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
   * {@link PgSubmission}.
   */
  private final PgSubmission<?> submission;

  /**
   * {@link QueryReuse}.
   */
  private final QueryReuse reuse;

  /**
   * Name for the {@link Query}.
   */
  private final String name;

  /**
   * Next row number.
   */
  private long nextRowNumber = 0;

  /**
   * Instantiate.
   * 
   * @param submission {@link PgSubmission}.
   * @param reuse      {@link QueryReuse}.
   */
  public Query(PgSubmission<?> submission, QueryReuse reuse) {
    this.submission = submission;
    this.reuse = reuse;
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
   * Obtains the {@link QueryReuse}.
   * 
   * @return {@link QueryReuse}.
   */
  public QueryReuse getReuse() {
    return this.reuse;
  }

  /**
   * Obtains the {@link PgSubmission}.
   * 
   * @return {@link PgSubmission}.
   */
  public PgSubmission<?> getSubmission() {
    return this.submission;
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
        throw new IllegalStateException(
            "Invalid completion type '" + submission.getCompletionType() + "' for " + this.getClass().getSimpleName());
      }
    } catch (Throwable t) {
      ((CompletableFuture<?>) submission.getCompletionStage()).completeExceptionally(t);
    }
  }

  /**
   * Some submission types needs multiple rounds of queries before the operation
   * is finished. This function returns true if more is needed.
   *
   * @return true if another query should be sent to the database
   * @throws ExecutionException   if the bound variables are a future that fails
   * @throws InterruptedException if the bound variables are a future that fails
   */
  public boolean hasMoreToExecute() throws ExecutionException, InterruptedException {
    if (submission.getCompletionType() == ARRAY_COUNT) {
      return ((ArrayCountSubmission) submission).hasMoreToExecute();
    }
    return false;
  }
}