package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.operations.helpers.ParameterHolder;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.Collector;

public class PGValidationOperation implements Operation<Void> {
  private final PGConnection connection;
  private final Connection.Validation depth;

  public PGValidationOperation(PGConnection connection, Connection.Validation depth) {
    this.connection = connection;
    this.depth = depth;
  }

  @Override
  public Operation<Void> onError(Consumer<Throwable> handler) {
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
          PGSubmission<Void> submission = new PGSubmission<>(this::cancel, PGSubmission.Types.VOID);
          submission.getCompletionStage().toCompletableFuture().completeExceptionally(new IllegalStateException());
          return submission;
        } else {
          PGSubmission<Void> submission = new PGSubmission<>(this::cancel, PGSubmission.Types.VOID);
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
        PGSubmission<Void> submission = new PGSubmission<>(this::cancel, PGSubmission.Types.VOID);
        submission.setConnectionSubmission(false);
        submission.setSql("select 1");
        submission.setHolder(new ParameterHolder());
        submission.setCollector(Collector.of(
            () -> null,
            (a, v) -> {},
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
