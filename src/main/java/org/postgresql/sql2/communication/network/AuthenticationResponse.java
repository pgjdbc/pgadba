package org.postgresql.sql2.communication.network;

import java.util.concurrent.CompletableFuture;
import org.postgresql.sql2.PgConnectionProperty;
import org.postgresql.sql2.communication.BeFrame;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.packets.AuthenticationRequest;
import org.postgresql.sql2.communication.packets.ParameterStatus;
import org.postgresql.sql2.submissions.ConnectSubmission;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Authentication success {@link NetworkResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class AuthenticationResponse implements NetworkResponse {

  private final ConnectSubmission connectSubmission;

  public AuthenticationResponse(ConnectSubmission connectSubmission) {
    this.connectSubmission = connectSubmission;
  }

  @Override
  public NetworkResponse read(NetworkReadContext context) throws IOException {
    // Expecting authentication challenge
    BeFrame frame = context.getBeFrame();
    switch (frame.getTag()) {

      case AUTHENTICATION:
        AuthenticationRequest authentication = new AuthenticationRequest(frame.getPayload());
        switch (authentication.getType()) {

          case SUCCESS:
            // Connected, so trigger any waiting submissions
            connectSubmission.finish(null);
            return this;

          default:
            throw new IllegalStateException("Unhandled authentication " + authentication.getType());
        }

      case PARAM_STATUS:
        // Load parameters for connection
        ParameterStatus paramStatus = new ParameterStatus(frame.getPayload());
        context.setProperty(PgConnectionProperty.lookup(paramStatus.getName()), paramStatus.getValue());
        return this;

      case CANCELLATION_KEY_DATA:
        // TODO handle cancellation key
        return this;

      case READY_FOR_QUERY:
        return null;

      default:
        throw new IllegalStateException("Invalid tag '" + frame.getTag() + "' for " + this.getClass().getSimpleName());
    }
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