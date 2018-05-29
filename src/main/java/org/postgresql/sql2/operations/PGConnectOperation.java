package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.PGSubmission;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class PGConnectOperation implements Operation<Void> {
  private Consumer<Throwable> errorHandler;
  private Duration minTime;
  private CompletionStage<Void> memberTail;
  private PGConnection connection;

  public PGConnectOperation(CompletionStage<Void> memberTail, PGConnection connection) {
    this.memberTail = memberTail;
    this.connection = connection;
  }

  @Override
  public Operation<Void> onError(Consumer<Throwable> errorHandler) {
    if (this.errorHandler != null) {
      throw new IllegalStateException("you are not allowed to call onError multiple times");
    }

    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public Operation<Void> timeout(Duration minTime) {
    this.minTime = minTime;
    return this;
  }

  @Override
  public Submission<Void> submit() {
    PGSubmission<Void> submission = new PGSubmission<>(this::cancel, PGSubmission.Types.VOID, errorHandler);
    submission.setConnectionSubmission(true);
    submission.getCompletionStage().thenAccept(s -> {
      connection.setLifeCycleOpen();
    });
    connection.addSubmissionOnQue(submission);
    return submission;
  }

  boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
