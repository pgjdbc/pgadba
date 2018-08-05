package org.postgresql.sql2.actions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.postgresql.sql2.PGConnectionProperties;
import org.postgresql.sql2.communication.FEFrame;
import org.postgresql.sql2.communication.NetworkAction;
import org.postgresql.sql2.communication.NetworkOutputStream;
import org.postgresql.sql2.communication.NetworkWriteContext;
import org.postgresql.sql2.communication.packets.AuthenticationRequest;
import org.postgresql.sql2.util.BinaryHelper;

import jdk.incubator.sql2.ConnectionProperty;

/**
 * {@link NetworkAction} to provide password authentication.
 * 
 * @author Daniel Sagenschneider
 */
public class PGAuthenticatePasswordAction extends AbstractAuthenticationSuccessAction {

  private final AuthenticationRequest authentication;

  public PGAuthenticatePasswordAction(AuthenticationRequest authentication) {
    this.authentication = authentication;
  }

  /*
   * ==================== NetworkAction ==========================
   */

  @Override
  public void write(NetworkWriteContext context) throws IOException {

    // Obtain the properties
    Map<ConnectionProperty, Object> properties = context.getProperties();

    // Create the payload (TODO determine if can reduce object creation)
    byte[] content = BinaryHelper.encode(
        ((String) properties.get(PGConnectionProperties.USER)).getBytes(StandardCharsets.UTF_8),
        ((String) properties.get(PGConnectionProperties.PASSWORD)).getBytes(StandardCharsets.UTF_8),
        this.authentication.getSalt());

    // Write the request
    NetworkOutputStream wire = context.getOutputStream();
    wire.write(FEFrame.FrontendTag.PASSWORD_MESSAGE.getByte());
    wire.initPacket();
    wire.write(content);
    wire.completePacket();
  }

}