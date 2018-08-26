package org.postgresql.sql2.operations;

import jdk.incubator.sql2.PrimitiveOperation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PgConnection;
import org.postgresql.sql2.PgSubmission;
import org.postgresql.sql2.submissions.BaseSubmission;

public class PgCatchOperation<S> implements PrimitiveOperation<S> {
  private PgConnection connection;

  public PgCatchOperation(PgConnection connection) {
    this.connection = connection;
  }

  @Override
  public Submission<S> submit() {
    BaseSubmission<S> submission = new BaseSubmission<>(this::cancel, PgSubmission.Types.CATCH, null, null, null, null);
    connection.submit(submission);
    return submission;
  }

  boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
