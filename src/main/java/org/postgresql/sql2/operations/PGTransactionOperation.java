package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;
import jdk.incubator.sql2.Transaction;
import jdk.incubator.sql2.TransactionOutcome;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.operations.helpers.ParameterHolder;

import java.time.Duration;
import java.util.function.Consumer;

public class PGTransactionOperation implements Operation<TransactionOutcome> {
  private Transaction transaction;
  private PGConnection connection;

  public PGTransactionOperation(Transaction transaction, PGConnection connection) {
    this.transaction = transaction;
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
    PGSubmission<TransactionOutcome> submission = new PGSubmission<>(this::cancel, PGSubmission.Types.TRANSACTION);
    submission.setConnectionSubmission(false);
    if(transaction.isRollbackOnly()) {
      submission.setSql("ROLLBACK TRANSACTION");
    } else {
      submission.setSql("COMMIT TRANSACTION");
    }
    submission.setHolder(new ParameterHolder());
    connection.addSubmissionOnQue(submission);
    return submission;
  }

  boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
