package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.operations.helpers.ParameterHolder;

import java.time.Duration;
import java.util.function.Consumer;

public class PGOperation<S> implements Operation<S> {
  private final PGConnection connection;
  private final String sql;
  private Consumer<Throwable> errorHandler;

  public PGOperation(PGConnection connection, String sql) {
    this.connection = connection;
    this.sql = sql;
  }

  @Override
  public Operation<S> onError(Consumer<Throwable> errorHandler) {
    if (this.errorHandler != null) {
      throw new IllegalStateException("you are not allowed to call onError multiple times");
    }

    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public Operation<S> timeout(Duration minTime) {
    return this;
  }

  @Override
  public Submission<S> submit() {
    PGSubmission<S> submission = new PGSubmission<>(this::cancel, PGSubmission.Types.VOID, errorHandler);
    submission.setConnectionSubmission(false);
    submission.setSql(sql);
    submission.setHolder(new ParameterHolder());
    connection.addSubmissionOnQue(submission);
    return submission;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
