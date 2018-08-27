package org.postgresql.sql2.operations;

import jdk.incubator.sql2.LocalOperation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PgConnection;
import org.postgresql.sql2.PgSubmission;
import org.postgresql.sql2.submissions.GroupSubmission;
import org.postgresql.sql2.submissions.LocalSubmission;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class PgLocalOperation<T> implements LocalOperation<T> {
  private static final Callable defaultAction = () -> {
    return null;
  };
  private PgConnection connection;
  private Callable<T> action = defaultAction;
  private Consumer<Throwable> errorHandler;
  private GroupSubmission groupSubmission;

  public PgLocalOperation(PgConnection connection, GroupSubmission groupSubmission) {
    this.connection = connection;
    this.groupSubmission = groupSubmission;
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
    PgSubmission<T> submission = new LocalSubmission<>(this::cancel, errorHandler, action, groupSubmission);
    connection.submit(submission);
    return submission;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
