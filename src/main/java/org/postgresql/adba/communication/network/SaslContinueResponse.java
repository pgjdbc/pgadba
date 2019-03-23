package org.postgresql.adba.communication.network;

import org.postgresql.adba.util.scram.client.ScramSession;
import org.postgresql.adba.util.scram.common.exception.ScramException;
import java.io.IOException;
import jdk.incubator.sql2.SqlException;
import org.postgresql.adba.communication.BeFrame;
import org.postgresql.adba.communication.NetworkReadContext;
import org.postgresql.adba.communication.NetworkResponse;
import org.postgresql.adba.communication.packets.AuthenticationRequest;
import org.postgresql.adba.submissions.ConnectSubmission;

public class SaslContinueResponse implements NetworkResponse {

  private ScramSession scramSession;
  private ConnectSubmission connectSubmission;
  private ScramSession.ServerFirstProcessor serverFirstProcessor;

  public SaslContinueResponse(ScramSession scramSession, ConnectSubmission connectSubmission) {
    this.scramSession = scramSession;
    this.connectSubmission = connectSubmission;
  }

  @Override
  public NetworkResponse read(NetworkReadContext context) throws IOException {

    BeFrame frame = context.getBeFrame();
    switch (frame.getTag()) {
      case AUTHENTICATION:
        AuthenticationRequest cont = new AuthenticationRequest(frame.getPayload());

        try {
          serverFirstProcessor = scramSession.receiveServerFirstMessage(cont.getSaslContinueMessage());

          context.write(new SaslFinalPasswordRequest(serverFirstProcessor, connectSubmission));
          return null;
        } catch (ScramException e) {
          connectSubmission.getCompletionStage().toCompletableFuture().completeExceptionally(
              new SqlException(e.getMessage(), e, "not logged in", 0, "", 0));
          return null;
        }

      default:
        connectSubmission.getCompletionStage().toCompletableFuture().completeExceptionally(
            new IllegalStateException("Invalid tag '" + frame.getTag() + "' for " + this.getClass().getSimpleName()));
    }
    return null;
  }

  @Override
  public NetworkResponse handleException(Throwable ex) {
    return null;
  }
}
