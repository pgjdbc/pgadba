package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.submissions.BaseSubmission;
import org.postgresql.sql2.submissions.ConnectSubmission;
import org.postgresql.sql2.submissions.GroupSubmission;

import java.time.Duration;
import java.util.function.Consumer;

public class PGConnectOperation implements Operation<Void> {
  private Consumer<Throwable> errorHandler;
  private Duration minTime;
  private PGConnection connection;
  private GroupSubmission groupSubmission;

  public PGConnectOperation(PGConnection connection, GroupSubmission groupSubmission) {
    this.connection = connection;
    this.groupSubmission = groupSubmission;
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
    ConnectSubmission submission = new ConnectSubmission(this::cancel, BaseSubmission.Types.CONNECT, errorHandler, groupSubmission);
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
