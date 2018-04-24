package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;
import jdk.incubator.sql2.TransactionOutcome;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.PGSubmission;

import java.time.Duration;
import java.util.function.Consumer;

public class PGTransactionOperation implements Operation<TransactionOutcome> {
  private PGConnection connection;

  public PGTransactionOperation(PGConnection connection) {
    this.connection = connection;
  }

  @Override
  public Operation<TransactionOutcome> onError(Consumer<Throwable> handler) {
    return this;
  }

  @Override
  public Operation<TransactionOutcome> timeout(Duration minTime) {
    return this;
  }

  @Override
  public Submission<TransactionOutcome> submit() {
    PGSubmission submission = new PGSubmission(this::cancel);
    submission.setConnectionSubmission(false);
    connection.addSubmissionOnQue(submission);
    return submission;
  }

  boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
