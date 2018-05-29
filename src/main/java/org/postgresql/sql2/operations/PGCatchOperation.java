package org.postgresql.sql2.operations;

import jdk.incubator.sql2.PrimitiveOperation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.PGSubmission;

public class PGCatchOperation<S> implements PrimitiveOperation<S> {
  private PGConnection connection;

  public PGCatchOperation(PGConnection connection) {
    this.connection = connection;
  }

  @Override
  public Submission<S> submit() {
    PGSubmission<S> submission = new PGSubmission<>(this::cancel, null, null);
    submission.setConnectionSubmission(false);
    connection.addSubmissionOnQue(submission);
    return submission;
  }

  boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
