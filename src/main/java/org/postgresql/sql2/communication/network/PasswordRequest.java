package org.postgresql.sql2.communication.network;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import jdk.incubator.sql2.AdbaConnectionProperty;
import jdk.incubator.sql2.ConnectionProperty;
import org.postgresql.sql2.communication.FeFrame;
import org.postgresql.sql2.communication.NetworkOutputStream;
import org.postgresql.sql2.communication.NetworkRequest;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.NetworkWriteContext;
import org.postgresql.sql2.communication.packets.AuthenticationRequest;
import org.postgresql.sql2.submissions.ConnectSubmission;
import org.postgresql.sql2.util.BinaryHelper;

/**
 * {@link NetworkRequest} to provide password authentication.
 * 
 * @author Daniel Sagenschneider
 */
public class PasswordRequest implements NetworkRequest {

  private final AuthenticationRequest authentication;

  private final ConnectSubmission connectSubmission;

  public PasswordRequest(AuthenticationRequest authentication, ConnectSubmission connectSubmission) {
    this.authentication = authentication;
    this.connectSubmission = connectSubmission;
  }

  /*
   * ==================== NetworkAction ==========================
   */

  @Override
  public NetworkRequest write(NetworkWriteContext context) throws IOException {

    // Obtain the properties
    Map<ConnectionProperty, Object> properties = context.getProperties();

    // Create the payload (TODO determine if can reduce object creation)
    String username = (String) properties.get(AdbaConnectionProperty.USER);
    String password = (String) properties.get(AdbaConnectionProperty.PASSWORD);
    byte[] content = BinaryHelper.encode(username.getBytes(StandardCharsets.UTF_8),
        password.getBytes(StandardCharsets.UTF_8), authentication.getSalt());

    // Write the request
    NetworkOutputStream wire = context.getOutputStream();
    wire.write(FeFrame.FrontendTag.PASSWORD_MESSAGE.getByte());
    wire.initPacket();
    wire.write(content);
    wire.writeTerminator();
    wire.completePacket();

    // No further immediate requests
    return null;
  }

  @Override
  public boolean isBlocking() {
    return true;
  }

  @Override
  public NetworkResponse getRequiredResponse() {
    return new AuthenticationResponse(connectSubmission);
  }

}