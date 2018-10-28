package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PgSession;
import org.postgresql.sql2.PgSubmission;
import org.postgresql.sql2.operations.helpers.ParameterHolder;
import org.postgresql.sql2.submissions.VoidSubmission;

import java.time.Duration;
import java.util.function.Consumer;

public class PgOperation<S> implements Operation<S> {
  private final PgSession connection;
  private final String sql;
  private Consumer<Throwable> errorHandler;

  public PgOperation(PgSession connection, String sql) {
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
    PgSubmission<S> submission = new VoidSubmission<>(this::cancel, errorHandler, new ParameterHolder(), null, sql);
    connection.submit(submission);
    return submission;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
