package org.postgresql.sql2.operations;

import jdk.incubator.sql2.PrimitiveOperation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.submissions.BaseSubmission;

public class PGCatchOperation<S> implements PrimitiveOperation<S> {
  private PGConnection connection;

  public PGCatchOperation(PGConnection connection) {
    this.connection = connection;
  }

  @Override
  public Submission<S> submit() {
    BaseSubmission<S> submission = new BaseSubmission<>(this::cancel, PGSubmission.Types.CATCH, null, null, null, null);
    connection.addSubmissionOnQue(submission);
    return submission;
  }

  boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
