package org.postgresql.sql2.communication.network;

import java.io.IOException;
import java.util.function.Consumer;

import org.postgresql.sql2.PgConnectionProperties;
import org.postgresql.sql2.communication.BEFrameParser;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.packets.AuthenticationRequest;
import org.postgresql.sql2.communication.packets.ParameterStatus;
import org.postgresql.sql2.submissions.ConnectSubmission;

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
    switch (context.getFrameTag()) {

    case BEFrameParser.AUTHENTICATION:
      AuthenticationRequest authentication = new AuthenticationRequest(context);
      switch (authentication.getType()) {

      case SUCCESS:
        // Connected, so trigger any waiting submissions
        this.connectSubmission.finish(null);
        return this;

      default:
        throw new IllegalStateException("Unhandled authentication " + authentication.getType());
      }

    case BEFrameParser.PARAM_STATUS:
      // Load parameters for connection
      ParameterStatus paramStatus = new ParameterStatus(context);
      context.setProperty(PgConnectionProperties.lookup(paramStatus.getName()), paramStatus.getValue());
      return this;

    case BEFrameParser.CANCELLATION_KEY_DATA:
      // TODO handle cancellation key
      return this;

    case BEFrameParser.READY_FOR_QUERY:
      return null;

    default:
      throw new IllegalStateException(
          "Invalid tag '" + context.getFrameTag() + "' for " + this.getClass().getSimpleName());
    }

  }

  @Override
  public NetworkResponse handleException(Throwable ex) {
    Consumer<Throwable> errorHandler = this.connectSubmission.getErrorHandler();
    if (errorHandler != null) {
      this.connectSubmission.getErrorHandler().accept(ex);
    } else {
      // TODO handle connection error
      ex.printStackTrace();
    }
    return null;
  }

}