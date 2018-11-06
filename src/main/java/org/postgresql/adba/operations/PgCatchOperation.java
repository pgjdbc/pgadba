package org.postgresql.adba.operations;

import jdk.incubator.sql2.PrimitiveOperation;
import jdk.incubator.sql2.Submission;
import org.postgresql.adba.PgSession;
import org.postgresql.adba.PgSubmission;
import org.postgresql.adba.submissions.BaseSubmission;

public class PgCatchOperation<S> implements PrimitiveOperation<S> {
  private PgSession connection;

  public PgCatchOperation(PgSession connection) {
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
