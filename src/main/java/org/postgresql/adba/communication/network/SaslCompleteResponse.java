package org.postgresql.adba.communication.network;

import com.ongres.scram.client.ScramSession.ClientFinalProcessor;
import com.ongres.scram.common.exception.ScramInvalidServerSignatureException;
import com.ongres.scram.common.exception.ScramParseException;
import com.ongres.scram.common.exception.ScramServerErrorException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import jdk.incubator.sql2.SqlException;
import org.postgresql.adba.communication.BeFrame;
import org.postgresql.adba.communication.NetworkReadContext;
import org.postgresql.adba.communication.NetworkResponse;
import org.postgresql.adba.communication.packets.AuthenticationRequest;
import org.postgresql.adba.submissions.ConnectSubmission;

public class SaslCompleteResponse implements NetworkResponse {

  private ConnectSubmission connectSubmission;
  private ClientFinalProcessor clientFinalProcessor;

  public SaslCompleteResponse(ConnectSubmission connectSubmission,
      ClientFinalProcessor clientFinalProcessor) {
    this.connectSubmission = connectSubmission;
    this.clientFinalProcessor = clientFinalProcessor;
  }

  @Override
  public NetworkResponse read(NetworkReadContext context) {
    BeFrame frame = context.getBeFrame();
    switch (frame.getTag()) {

      case AUTHENTICATION:
        AuthenticationRequest saslComplete = new AuthenticationRequest(frame.getPayload());
        try {
          clientFinalProcessor.receiveServerFinalMessage(saslComplete.getSaslFinalMessage());

          return new AuthenticationResponse(connectSubmission);
        } catch (ScramParseException e) {
          ((CompletableFuture)connectSubmission.getCompletionStage()).completeExceptionally(
              new SqlException("Invalid server-final-message: " + saslComplete.getSaslFinalMessage(),
                  e, "not logged in", 0, "", 0));
        } catch (ScramServerErrorException e) {
          ((CompletableFuture)connectSubmission.getCompletionStage()).completeExceptionally(
              new SqlException("SCRAM authentication failed, server returned error: " + e.getError().getErrorMessage(),
                  e, "not logged in", 0, "", 0));
        } catch (ScramInvalidServerSignatureException e) {
          ((CompletableFuture)connectSubmission.getCompletionStage()).completeExceptionally(
              new SqlException("Invalid server SCRAM signature", e, "not logged in", 0, "", 0));
        } catch (Throwable e) {
          ((CompletableFuture)connectSubmission.getCompletionStage()).completeExceptionally(
              new SqlException(e.getMessage(), e, "not logged in", 0, "", 0));
        }

        break;
      default:
        connectSubmission.getCompletionStage().toCompletableFuture().completeExceptionally(
            new RuntimeException("unexpected network packet, expected SaslComplete"));
    }
    return null;
  }

  @Override
  public NetworkResponse handleException(Throwable ex) {
    Consumer<Throwable> errorHandler = connectSubmission.getErrorHandler();
    if (errorHandler != null) {
      connectSubmission.getErrorHandler().accept(ex);
    }
    ((CompletableFuture<Void>)connectSubmission.getCompletionStage()).completeExceptionally(ex);
    return null;
  }
}
