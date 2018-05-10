package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.operations.helpers.ParameterHolder;

import java.time.Duration;
import java.util.function.Consumer;

public class PGOperation implements Operation<Object> {
  private final PGConnection connection;
  private final String sql;

  public PGOperation(PGConnection connection, String sql) {
    this.connection = connection;
    this.sql = sql;
  }

  @Override
  public Operation<Object> onError(Consumer<Throwable> handler) {
    return null;
  }

  @Override
  public Operation<Object> timeout(Duration minTime) {
    return null;
  }

  @Override
  public Submission<Object> submit() {
    PGSubmission<Object> submission = new PGSubmission<>(this::cancel, PGSubmission.Types.VOID);
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
