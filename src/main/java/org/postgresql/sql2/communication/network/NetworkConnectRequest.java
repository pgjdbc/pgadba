package org.postgresql.sql2.communication.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import org.postgresql.sql2.PGConnectionProperties;
import org.postgresql.sql2.communication.BEFrameParser;
import org.postgresql.sql2.communication.NetworkConnect;
import org.postgresql.sql2.communication.NetworkConnectContext;
import org.postgresql.sql2.communication.NetworkOutputStream;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.NetworkRequest;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.NetworkWriteContext;
import org.postgresql.sql2.communication.packets.AuthenticationRequest;
import org.postgresql.sql2.submissions.ConnectSubmission;
import org.postgresql.sql2.util.BinaryHelper;

import jdk.incubator.sql2.ConnectionProperty;

/**
 * Connect {@link NetworkRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class NetworkConnectRequest implements NetworkConnect, NetworkRequest, NetworkResponse {

  public static final String CHARSET = "UTF8";

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
    Map<ConnectionProperty, Object> properties = context.getProperties();
    context.getSocketChannel().connect(new InetSocketAddress((String) properties.get(PGConnectionProperties.HOST),
        (Integer) properties.get(PGConnectionProperties.PORT)));
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
    Map<ConnectionProperty, Object> properties = context.getProperties();

    // As now connected, send start up
    NetworkOutputStream wire = context.getOutputStream();
    wire.initPacket();
    wire.write(BinaryHelper.writeInt(3 * 65536));
    wire.write("user");
    wire.write(((String) properties.get(PGConnectionProperties.USER)));
    wire.write("database");
    wire.write(((String) properties.get(PGConnectionProperties.DATABASE)));
    wire.write("application_name");
    wire.write("java_sql2_client");
    wire.write("client_encoding");
    wire.write(CHARSET);
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
  public NetworkResponse read(NetworkReadContext context) throws IOException {

    // Expecting authentication challenge
    switch (context.getFrameTag()) {

    case BEFrameParser.AUTHENTICATION:
      AuthenticationRequest authentication = new AuthenticationRequest(context);
      switch (authentication.getType()) {

      case MD5:
        // Password authentication required
        context.write(new PasswordRequest(authentication, this.connectSubmission));
        return null;

      case SUCCESS:
        // Connected, so trigger any waiting submissions
        context.writeRequired();
        return new AuthenticationResponse(this.connectSubmission);

      default:
        throw new IllegalStateException("Unhandled authentication " + authentication.getType());
      }

    default:
      throw new IllegalStateException(
          "Invalid tag '" + context.getFrameTag() + "' for " + this.getClass().getSimpleName());
    }
  }

  @Override
  public NetworkResponse handleException(Throwable ex) {
    Portal.doHandleException(this.connectSubmission, ex);
    return null;
  }

}