package org.postgresql.sql2.actions;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;

import org.postgresql.sql2.PGConnectionProperties;
import org.postgresql.sql2.communication.BEFrame;
import org.postgresql.sql2.communication.NetworkAction;
import org.postgresql.sql2.communication.NetworkConnectContext;
import org.postgresql.sql2.communication.NetworkInitialiseContext;
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
public class PGConnectAction implements NetworkAction {

  /*
   * =================== NetworkRequest ====================
   */

  private boolean isBlocking = true;

  @Override
  public void init(NetworkInitialiseContext context) throws IOException {
    // Undertake connecting
    Map<ConnectionProperty, Object> properties = context.getProperties();
    context.getSocketChannel().connect(new InetSocketAddress((String) properties.get(PGConnectionProperties.HOST),
        (Integer) properties.get(PGConnectionProperties.PORT)));
  }

  @Override
  public void connect(NetworkConnectContext context) throws IOException {

    // Handle completion of connect
    if (!context.getSocketChannel().finishConnect()) {
      throw new IOException("Failure to finish connection");
    }
  }

  @Override
  public void write(NetworkWriteContext context) throws IOException {

    // Obtain the properties
    Map<ConnectionProperty, Object> properties = context.getProperties();

    // As now connected, send start up
    OutputStream wire = context.getOutputStream();

    // TODO pre-calculate byte[] to reduce method calls
    wire.write(0);
    wire.write(0);
    wire.write(0);
    wire.write(0);
    wire.write(BinaryHelper.writeInt(3 * 65536));
    wire.write("user".getBytes());
    wire.write(0);
    wire.write(((String) properties.get(PGConnectionProperties.USER)).getBytes());
    wire.write(0);
    wire.write("database".getBytes());
    wire.write(0);
    wire.write(((String) properties.get(PGConnectionProperties.DATABASE)).getBytes());
    wire.write(0);
    wire.write("application_name".getBytes());
    wire.write(0);
    wire.write("java_sql2_client".getBytes());
    wire.write(0);
    wire.write("client_encoding".getBytes());
    wire.write(0);
    wire.write("UTF8".getBytes());
    wire.write(0);
    wire.write(0);
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

}