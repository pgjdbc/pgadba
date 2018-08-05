package org.postgresql.sql2.actions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import org.postgresql.sql2.PGConnectionProperties;
import org.postgresql.sql2.communication.BEFrame;
import org.postgresql.sql2.communication.NetworkAction;
import org.postgresql.sql2.communication.NetworkConnect;
import org.postgresql.sql2.communication.NetworkConnectContext;
import org.postgresql.sql2.communication.NetworkOutputStream;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.NetworkWriteContext;
import org.postgresql.sql2.communication.packets.AuthenticationRequest;
import org.postgresql.sql2.util.BinaryHelper;

import jdk.incubator.sql2.ConnectionProperty;

/**
 * Connect {@link NetworkAction}.
 * 
 * @author Daniel Sagenschneider
 */
public class PGConnectAction implements NetworkConnect, NetworkAction {

  /*
   * =================== NetworkRequest ====================
   */

  private boolean isBlocking = true;

  @Override
  public void connect(NetworkConnectContext context) throws IOException {
    // Undertake connecting
    Map<ConnectionProperty, Object> properties = context.getProperties();
    context.getSocketChannel().connect(new InetSocketAddress((String) properties.get(PGConnectionProperties.HOST),
        (Integer) properties.get(PGConnectionProperties.PORT)));
  }

  @Override
  public NetworkAction finishConnect(NetworkConnectContext context) throws IOException {

    // Handle completion of connect
    if (!context.getSocketChannel().finishConnect()) {
      throw new IOException("Failure to finish connection");
    }

    // Allow undertaking send start up information before any further actions
    return this;
  }

  @Override
  public void write(NetworkWriteContext context) throws IOException {

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
    wire.write("UTF8");
    wire.writeTerminator();
    wire.completePacket();
  }

  @Override
  public boolean isBlocking() {
    return this.isBlocking;
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

      case MD5:
        // Password authentication required
        return new PGAuthenticatePasswordAction(authentication);

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

  @Override
  public void handleException(Throwable ex) {
    NetworkAction.super.handleException(ex);
  }

}