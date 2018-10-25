package org.postgresql.sql2.operations;

import java.time.Duration;
import java.util.function.Consumer;
import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PgConnection;
import org.postgresql.sql2.submissions.ConnectSubmission;
import org.postgresql.sql2.submissions.GroupSubmission;

public class PgConnectOperation implements Operation<Void> {

  private Consumer<Throwable> errorHandler;
  private Duration minTime;
  private PgConnection connection;
  private GroupSubmission groupSubmission;

  public PgConnectOperation(PgConnection connection, GroupSubmission groupSubmission) {
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
    ConnectSubmission submission = new ConnectSubmission(this::cancel, errorHandler, groupSubmission, connection.getProperties());
    submission.getCompletionStage().thenAccept(s -> {
      connection.setLifeCycleOpen();
    });
    connection.sendNetworkConnect(submission.getNetworkConnect());
    return submission;
  }

  boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }

}