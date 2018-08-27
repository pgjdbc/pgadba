package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PgConnection;
import org.postgresql.sql2.operations.helpers.ParameterHolder;
import org.postgresql.sql2.submissions.BaseSubmission;
import org.postgresql.sql2.submissions.RowSubmission;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.Collector;

public class PgValidationOperation implements Operation<Void> {
  private final PgConnection connection;
  private final Connection.Validation depth;
  private Consumer<Throwable> errorHandler;

  /**
   * This operation validates that the connection is still valid.
   * @param connection connection to validate
   * @param depth to what depth the validation should happen
   */
  public PgValidationOperation(PgConnection connection, Connection.Validation depth) {
    this.connection = connection;
    this.depth = depth;
  }

  @Override
  public Operation<Void> onError(Consumer<Throwable> errorHandler) {
    if (this.errorHandler != null) {
      throw new IllegalStateException("you are not allowed to call onError multiple times");
    }

    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public Operation<Void> timeout(Duration minTime) {
    return this;
  }

  @Override
  public Submission<Void> submit() {
    switch (depth) {
      case NONE:
      case LOCAL:
        if (connection.isConnectionClosed()) {
          BaseSubmission<Void> submission = new BaseSubmission<>(this::cancel, BaseSubmission.Types.VOID, errorHandler,
              null, null, null);
          submission.getCompletionStage().toCompletableFuture().completeExceptionally(new IllegalStateException());
          return submission;
        } else {
          BaseSubmission<Void> submission = new BaseSubmission<>(this::cancel, BaseSubmission.Types.VOID, errorHandler,
              null, null, null);
          submission.getCompletionStage().toCompletableFuture().complete(null);
          return submission;
        }
      case SOCKET:
        break;
      case NETWORK:
        break;
      case SERVER:
        break;
      case COMPLETE:
        RowSubmission<Void> submission = new RowSubmission<>(this::cancel, errorHandler, new ParameterHolder(),
            null, "select 1");
        submission.setCollector(Collector.of(
            () -> null,
            (a, v) -> {
            },
            (a, b) -> null,
            a -> null));
        connection.submit(submission);
        return submission;
      default:
        throw new IllegalStateException("not all enum values implemented in switch statement");
    }
    throw new IllegalArgumentException();
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
