package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.submissions.BaseSubmission;
import org.postgresql.sql2.operations.helpers.ParameterHolder;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.Collector;

public class PGValidationOperation implements Operation<Void> {
  private final PGConnection connection;
  private final Connection.Validation depth;
  private Consumer<Throwable> errorHandler;

  public PGValidationOperation(PGConnection connection, Connection.Validation depth) {
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
          BaseSubmission<Void> submission = new BaseSubmission<>(this::cancel, BaseSubmission.Types.VOID, errorHandler, null, null);
          submission.getCompletionStage().toCompletableFuture().completeExceptionally(new IllegalStateException());
          return submission;
        } else {
          BaseSubmission<Void> submission = new BaseSubmission<>(this::cancel, BaseSubmission.Types.VOID, errorHandler, null, null);
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
        BaseSubmission<Void> submission = new BaseSubmission<>(this::cancel, BaseSubmission.Types.VOID, errorHandler, new ParameterHolder(), null);
        submission.setSql("select 1");
        submission.setCollector(Collector.of(
            () -> null,
            (a, v) -> {
            },
            (a, b) -> null,
            a -> null));
        connection.addSubmissionOnQue(submission);
        return submission;
    }
    throw new IllegalArgumentException();
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
