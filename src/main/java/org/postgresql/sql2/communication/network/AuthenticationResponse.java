package org.postgresql.sql2.communication.network;

import java.io.IOException;

import org.postgresql.sql2.PGConnectionProperties;
import org.postgresql.sql2.communication.BEFrame;
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
    BEFrame frame = context.getBEFrame();
    switch (frame.getTag()) {

    case AUTHENTICATION:
      AuthenticationRequest authentication = new AuthenticationRequest(frame.getPayload());
      switch (authentication.getType()) {

      case SUCCESS:
        // Connected, so trigger any waiting submissions
        this.connectSubmission.finish(null);
        return this;

      default:
        throw new IllegalStateException("Unhandled authentication " + authentication.getType());
      }

    case PARAM_STATUS:
      // Load parameters for connection
      ParameterStatus paramStatus = new ParameterStatus(frame.getPayload());
      context.setProperty(PGConnectionProperties.lookup(paramStatus.getName()), paramStatus.getValue());
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
    this.connectSubmission.getErrorHandler().accept(ex);
    return null;
  }

}