package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.PGSubmission;

import java.time.Duration;
import java.util.function.Consumer;

public class PGCloseOperation implements Operation<Void> {
  private PGConnection connection;
  private Consumer<Throwable> errorHandler;

  public PGCloseOperation(PGConnection connection) {
    this.connection = connection;
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
    PGSubmission<Void> submission = new PGSubmission<>(this::cancel, PGSubmission.Types.CLOSE, errorHandler);
    submission.setConnectionSubmission(false);
    submission.setSql(null);
    submission.setHolder(null);
    connection.addSubmissionOnQue(submission);
    return submission;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
