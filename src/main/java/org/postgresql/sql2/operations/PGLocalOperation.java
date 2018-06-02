package org.postgresql.sql2.operations;

import jdk.incubator.sql2.LocalOperation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.PGSubmission;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class PGLocalOperation<T> implements LocalOperation<T> {
  private static final Callable defaultAction = () -> {
    return null;
  };
  private PGConnection connection;
  private Callable<T> action = defaultAction;
  private Consumer<Throwable> errorHandler;

  public PGLocalOperation(PGConnection connection) {
    this.connection = connection;
  }

  @Override
  public LocalOperation<T> onExecution(Callable<T> action) {
    if (action != null) {
      this.action = action;
    }
    return this;
  }

  @Override
  public LocalOperation<T> onError(Consumer<Throwable> errorHandler) {
    if (this.errorHandler != null) {
      throw new IllegalStateException("you are not allowed to call onError multiple times");
    }

    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public LocalOperation<T> timeout(Duration minTime) {
    return this;
  }

  @Override
  public Submission<T> submit() {
    PGSubmission<T> submission = new PGSubmission<>(this::cancel, PGSubmission.Types.LOCAL, errorHandler);
    submission.setConnectionSubmission(false);
    submission.setLocalAction(action);
    connection.addSubmissionOnQue(submission);
    return submission;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
