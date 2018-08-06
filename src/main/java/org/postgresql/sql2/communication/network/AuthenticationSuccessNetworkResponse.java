package org.postgresql.sql2.communication.network;

import java.io.IOException;

import org.postgresql.sql2.communication.BEFrame;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.packets.AuthenticationRequest;
import org.postgresql.sql2.submissions.ConnectSubmission;

/**
 * Authentication success {@link NetworkResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class AuthenticationSuccessNetworkResponse implements NetworkResponse {

  private final ConnectSubmission connectSubmission;

  public AuthenticationSuccessNetworkResponse(ConnectSubmission connectSubmission) {
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
        return new ReadyForQueryNetworkResponse();

      default:
        throw new IllegalStateException("Unhandled authentication " + authentication.getType());
      }

    default:
      throw new IllegalStateException("Invalid tag '" + frame.getTag() + "' for " + this.getClass().getSimpleName());
    }
  }

}