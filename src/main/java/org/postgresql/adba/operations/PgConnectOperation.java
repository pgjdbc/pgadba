package org.postgresql.adba.operations;

import java.time.Duration;
import java.util.function.Consumer;
import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;
import org.postgresql.adba.PgSession;
import org.postgresql.adba.communication.NetworkConnection;
import org.postgresql.adba.submissions.ConnectSubmission;
import org.postgresql.adba.submissions.GroupSubmission;

public class PgConnectOperation implements Operation<Void> {

  private Consumer<Throwable> errorHandler;
  private Duration minTime;
  private PgSession connection;
  private GroupSubmission groupSubmission;
  private NetworkConnection protocol;

  /**
   * Initialize.
   * @param connection the session that created this Operation
   * @param groupSubmission if the operation is part of a group
   * @param protocol network link
   */
  public PgConnectOperation(PgSession connection, GroupSubmission groupSubmission, NetworkConnection protocol) {
    this.connection = connection;
    this.groupSubmission = groupSubmission;
    this.protocol = protocol;
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
    protocol.sendNetworkConnect(submission.getNetworkConnect());

    return submission;
  }

  boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }

}