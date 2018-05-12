package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.PGSubmission;

import java.time.Duration;
import java.util.function.Consumer;

public class PGCatchOperation implements Operation<Object> {
  private PGConnection connection;
  private Consumer<Throwable> errorHandler;

  public PGCatchOperation(PGConnection connection) {
    this.connection = connection;
  }

  @Override
  public Operation<Object> onError(Consumer<Throwable> errorHandler) {
    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public Operation<Object> timeout(Duration minTime) {
    return this;
  }

  @Override
  public Submission<Object> submit() {
    PGSubmission<Object> submission = new PGSubmission<>(this::cancel, null);
    submission.setConnectionSubmission(false);
    submission.setErrorHandler(errorHandler);
    connection.addSubmissionOnQue(submission);
    return submission;
  }

  boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
