package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PgConnection;
import org.postgresql.sql2.communication.network.CloseRequest;
import org.postgresql.sql2.submissions.CloseSubmission;

import java.time.Duration;
import java.util.function.Consumer;

public class PgCloseOperation implements Operation<Void> {
  private PgConnection connection;
  private Consumer<Throwable> errorHandler;

  public PgCloseOperation(PgConnection connection) {
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
    return this;
  }

  @Override
  public Submission<Void> submit() {
    CloseSubmission submission = new CloseSubmission(this::cancel, errorHandler);
    CloseRequest closeRequest = new CloseRequest(submission);
    connection.sendNetworkRequest(closeRequest);
    
    // Closing so unregister connection
    this.connection.unregister();
    
    return submission;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
