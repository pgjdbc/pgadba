package org.postgresql.sql2.operations;

import java.time.Duration;
import java.util.function.Consumer;
import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;
import jdk.incubator.sql2.TransactionCompletion;
import jdk.incubator.sql2.TransactionOutcome;
import org.postgresql.sql2.PgSession;
import org.postgresql.sql2.PgSubmission;
import org.postgresql.sql2.submissions.TransactionSubmission;

public class PgTransactionOperation implements Operation<TransactionOutcome> {
  private TransactionCompletion transaction;
  private PgSession connection;
  private Consumer<Throwable> errorHandler;

  public PgTransactionOperation(TransactionCompletion transaction, PgSession connection) {
    this.transaction = transaction;
    this.connection = connection;
  }

  @Override
  public Operation<TransactionOutcome> onError(Consumer<Throwable> errorHandler) {
    if (this.errorHandler != null) {
      throw new IllegalStateException("you are not allowed to call onError multiple times");
    }

    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public Operation<TransactionOutcome> timeout(Duration minTime) {
    return this;
  }

  @Override
  public Submission<TransactionOutcome> submit() {
    String sql;
    if (transaction.isRollbackOnly()) {
      sql = "ROLLBACK TRANSACTION";
    } else {
      sql = "COMMIT TRANSACTION";
    }
    PgSubmission<TransactionOutcome> submission = new TransactionSubmission(this::cancel, errorHandler, sql);
    connection.submit(submission);
    return submission;
  }

  boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
