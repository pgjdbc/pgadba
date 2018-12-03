package org.postgresql.adba.communication.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import jdk.incubator.sql2.AdbaSessionProperty;
import jdk.incubator.sql2.SessionProperty;
import org.postgresql.adba.PgSessionProperty;
import org.postgresql.adba.communication.BeFrame;
import org.postgresql.adba.communication.NetworkConnect;
import org.postgresql.adba.communication.NetworkConnectContext;
import org.postgresql.adba.communication.NetworkOutputStream;
import org.postgresql.adba.communication.NetworkReadContext;
import org.postgresql.adba.communication.NetworkRequest;
import org.postgresql.adba.communication.NetworkResponse;
import org.postgresql.adba.communication.NetworkWriteContext;
import org.postgresql.adba.communication.packets.AuthenticationRequest;
import org.postgresql.adba.submissions.ConnectSubmission;
import org.postgresql.adba.util.BinaryHelper;

/**
 * Connect {@link NetworkRequest}.
 *
 * @author Daniel Sagenschneider
 */
public class NetworkConnectRequest implements NetworkConnect, NetworkRequest, NetworkResponse {

  /**
   * {@link ConnectSubmission}.
   */
  private final ConnectSubmission connectSubmission;

  /**
   * Instantiate.
   *
   * @param connectSubmission {@link ConnectSubmission}.
   */
  public NetworkConnectRequest(ConnectSubmission connectSubmission) {
    this.connectSubmission = connectSubmission;
  }

  /*
   * =================== NetworkRequest ====================
   */

  @Override
  public void connect(NetworkConnectContext context) throws IOException {
    // Undertake connecting
    Map<SessionProperty, Object> properties = context.getProperties();
    context.getSocketChannel().connect(new InetSocketAddress((String) properties.get(PgSessionProperty.HOST),
        (Integer) properties.get(PgSessionProperty.PORT)));
  }

  @Override
  public NetworkRequest finishConnect(NetworkConnectContext context) throws IOException {

    // Handle completion of connect
    if (!context.getSocketChannel().finishConnect()) {
      throw new IOException("Failure to finish connection");
    }

    // Allow undertaking send start up information before any further actions
    return this;
  }

  @Override
  public NetworkRequest write(NetworkWriteContext context) throws IOException {

    // Obtain the properties
    Map<SessionProperty, Object> properties = context.getProperties();

    // As now connected, send start up
    NetworkOutputStream wire = context.getOutputStream();
    wire.initPacket();
    wire.write(BinaryHelper.writeInt(3 * 65536));
    wire.write("user");
    wire.write(((String) properties.get(AdbaSessionProperty.USER)));
    wire.write("database");
    wire.write(((String) properties.get(PgSessionProperty.DATABASE)));
    wire.write("application_name");
    wire.write(
        (String) properties.getOrDefault(PgSessionProperty.APPLICATION_NAME, PgSessionProperty.APPLICATION_NAME.defaultValue()));
    wire.write("client_encoding");
    wire.write("UTF8");
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
    return this;
  }

  @Override
  public NetworkResponse read(NetworkReadContext context) {

    // Expecting authentication challenge
    BeFrame frame = context.getBeFrame();
    switch (frame.getTag()) {

      case AUTHENTICATION:
        AuthenticationRequest authentication = new AuthenticationRequest(frame.getPayload());
        switch (authentication.getType()) {

          case MD5:
            // Password authentication required
            context.write(new Md5PasswordRequest(authentication, connectSubmission));
            return null;

          case SASL:
            // Password authentication required
            context.write(new SaslPasswordRequest(authentication, connectSubmission));
            return null;

          case SUCCESS:
            // Connected, so trigger any waiting submissions
            context.writeRequired();
            return new AuthenticationResponse(connectSubmission);

          default:
            throw new IllegalStateException("Unhandled authentication " + authentication.getType());
        }

      default:
        throw new IllegalStateException("Invalid tag '" + frame.getTag() + "' for " + this.getClass().getSimpleName());
    }
  }

  @Override
  public NetworkResponse handleException(Throwable ex) {
    Portal.doHandleException(connectSubmission, ex);
    return null;
  }

}