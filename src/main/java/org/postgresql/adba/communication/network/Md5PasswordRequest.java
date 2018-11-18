package org.postgresql.adba.communication.network;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import jdk.incubator.sql2.AdbaSessionProperty;
import jdk.incubator.sql2.SessionProperty;
import org.postgresql.adba.communication.FeFrame;
import org.postgresql.adba.communication.NetworkOutputStream;
import org.postgresql.adba.communication.NetworkRequest;
import org.postgresql.adba.communication.NetworkResponse;
import org.postgresql.adba.communication.NetworkWriteContext;
import org.postgresql.adba.communication.packets.AuthenticationRequest;
import org.postgresql.adba.submissions.ConnectSubmission;
import org.postgresql.adba.util.BinaryHelper;

/**
 * {@link NetworkRequest} to provide password authentication.
 * 
 * @author Daniel Sagenschneider
 */
public class Md5PasswordRequest implements NetworkRequest {

  private final AuthenticationRequest authentication;

  private final ConnectSubmission connectSubmission;

  public Md5PasswordRequest(AuthenticationRequest authentication, ConnectSubmission connectSubmission) {
    this.authentication = authentication;
    this.connectSubmission = connectSubmission;
  }

  /*
   * ==================== NetworkAction ==========================
   */

  @Override
  public NetworkRequest write(NetworkWriteContext context) throws IOException {

    // Obtain the properties
    Map<SessionProperty, Object> properties = context.getProperties();

    // Create the payload (TODO determine if can reduce object creation)
    String username = (String) properties.get(AdbaSessionProperty.USER);
    String password = (String) properties.get(AdbaSessionProperty.PASSWORD);
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