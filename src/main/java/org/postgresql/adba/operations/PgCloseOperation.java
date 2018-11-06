package org.postgresql.adba.operations;

import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;
import org.postgresql.adba.PgSession;
import org.postgresql.adba.communication.NetworkConnection;
import org.postgresql.adba.communication.network.CloseRequest;
import org.postgresql.adba.submissions.CloseSubmission;

import java.time.Duration;
import java.util.function.Consumer;

public class PgCloseOperation implements Operation<Void> {
  private PgSession connection;
  private Consumer<Throwable> errorHandler;
  private NetworkConnection protocol;

  public PgCloseOperation(PgSession connection, NetworkConnection protocol) {
    this.connection = connection;
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
    return this;
  }

  @Override
  public Submission<Void> submit() {
    CloseSubmission submission = new CloseSubmission(this::cancel, errorHandler);
    submission.getCompletionStage().thenAccept(s -> connection.setLifeCycleClosed());
    CloseRequest closeRequest = new CloseRequest(submission);
    protocol.sendNetworkRequest(closeRequest);

    // Closing so unregister connection
    this.connection.unregister();
    
    return submission;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
