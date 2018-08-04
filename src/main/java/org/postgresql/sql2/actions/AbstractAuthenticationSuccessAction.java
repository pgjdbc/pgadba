package org.postgresql.sql2.actions;

import java.io.IOException;

import org.postgresql.sql2.communication.BEFrame;
import org.postgresql.sql2.communication.NetworkAction;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.packets.AuthenticationRequest;

/**
 * Abstract authentication success {@link NetworkAction}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractAuthenticationSuccessAction implements NetworkAction {

  @Override
  public boolean isBlocking() {
    return true;
  }

  @Override
  public boolean isRequireResponse() {
    return true;
  }

  @Override
  public NetworkAction read(NetworkReadContext context) throws IOException {
    // Expecting authentication challenge
    BEFrame frame = context.getBEFrame();
    switch (frame.getTag()) {

    case AUTHENTICATION:
      AuthenticationRequest authentication = new AuthenticationRequest(frame.getPayload());
      switch (authentication.getType()) {

      case SUCCESS:
        // Connected, so trigger any waiting submissions
        context.writeRequired();
        return null;

      default:
        throw new IllegalStateException("Unhandled authentication " + authentication.getType());
      }

    default:
      throw new IllegalStateException("Invalid tag '" + frame.getTag() + "' for " + this.getClass().getSimpleName());
    }
  }

}