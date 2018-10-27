package org.postgresql.sql2.communication.network;

import java.io.IOException;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.NetworkResponse;

public class CloseResponse implements NetworkResponse {

  private final Submission<Void> submission;

  public CloseResponse(Submission<Void> submission) {
    this.submission = submission;
  }

  @Override
  public NetworkResponse read(NetworkReadContext context) throws IOException {
    submission.getCompletionStage().toCompletableFuture().complete(null);
    return null;
  }

  @Override
  public NetworkResponse handleException(Throwable ex) {
    submission.getCompletionStage().toCompletableFuture().completeExceptionally(ex);
    return null;
  }
}
