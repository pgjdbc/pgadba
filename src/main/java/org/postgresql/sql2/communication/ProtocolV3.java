package org.postgresql.sql2.communication;

import jdk.incubator.sql2.ConnectionProperty;
import org.postgresql.sql2.PGConnectionProperties;
import org.postgresql.sql2.communication.packets.AuthenticationRequest;
import org.postgresql.sql2.communication.packets.ParameterStatus;
import org.postgresql.sql2.communication.packets.ReadyForQuery;
import org.postgresql.sql2.util.BinaryHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProtocolV3 {
  private ProtocolV3States.States currentState = ProtocolV3States.States.NOT_CONNECTED;
  private Map<ConnectionProperty, Object> properties;

  private ConcurrentLinkedQueue<FEFrame> outputQue = new ConcurrentLinkedQueue<>();
  private ConcurrentLinkedQueue<FEFrame> waitToSendQue = new ConcurrentLinkedQueue<>();

  public ProtocolV3(Map<ConnectionProperty, Object> properties) {
    this.properties = properties;
  }

  public void readPacket(BEFrame packet) {
    switch (packet.getTag()) {
      case AUTHENTICATION:
        doAuthentication(packet);
        break;
      case CANCELLATION_KEY_DATA:
        break;
      case BIND_COMPLETE:

        break;
      case CLOSE_COMPLETE:
        break;
      case COMMAND_COMPLETE:
        break;
      case COPY_DATA:
        break;
      case COPY_DONE:
        break;
      case COPY_IN_RESPONSE:
        break;
      case COPY_OUT_RESPONSE:
        break;
      case COPY_BOTH_RESPONSE:
        break;
      case DATA_ROW:
        break;
      case EMPTY_QUERY_RESPONSE:
        break;
      case ERROR_RESPONSE:
        break;
      case FUNCTION_CALL_RESPONSE:
        break;
      case NEGOTIATE_PROTOCOL_VERSION:
        break;
      case NO_DATA:
        break;
      case NOTICE_RESPONSE:
        break;
      case NOTIFICATION_RESPONSE:
        break;
      case PARAM_DESCRIPTION:
        break;
      case PARAM_STATUS:
        doParameterStatus(packet);
        break;
      case PARSE_COMPLETE:
        break;
      case PORTAL_SUSPENDED:
        break;
      case READY_FOR_QUERY:
        doReadyForQuery(packet);
        break;
      case ROW_DESCRIPTION:
        break;
    }
  }

  public synchronized void sendData(SocketChannel socketChannel) {
    if(outputQue.size() == 0 && waitToSendQue.size() != 0 && currentState == ProtocolV3States.States.IDLE) {
      outputQue.add(waitToSendQue.poll());
    }
    if(outputQue.size() != 0) {
      FEFrame packet = outputQue.peek();

      if(packet == null) {
        return;
      }

      try {
        socketChannel.write(packet.getPayload());
      } catch (IOException e) {
        e.printStackTrace();
      }
      if(!packet.hasRemaining()) {
        outputQue.poll();
      }
    }
  }

  public void queFrame(FEFrame frame) {
    waitToSendQue.add(frame);
  }

  public void sendStartupPacket() {
    try {
      ByteArrayOutputStream array = new ByteArrayOutputStream();
      array.write(0);
      array.write(0);
      array.write(0);
      array.write(0);
      array.write(BinaryHelper.writeInt(3 * 65536));
      array.write("user".getBytes());
      array.write(0);
      array.write(((String) properties.get(PGConnectionProperties.USER)).getBytes());
      array.write(0);
      array.write("database".getBytes());
      array.write(0);
      array.write(((String) properties.get(PGConnectionProperties.DATABASE)).getBytes());
      array.write(0);
      array.write("application_name".getBytes());
      array.write(0);
      array.write("java_sql2_client".getBytes());
      array.write(0);
      array.write("client_encoding".getBytes());
      array.write(0);
      array.write("UTF8".getBytes());
      array.write(0);
      array.write(0);

      outputQue.add(new FEFrame(array.toByteArray(), true));
      currentState = ProtocolV3States.lookup(currentState, ProtocolV3States.Events.CONNECTION);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void doAuthentication(BEFrame packet) {
    AuthenticationRequest req = new AuthenticationRequest(packet.getPayload());

    switch (req.getType()) {
      case SUCCESS:
        currentState = ProtocolV3States.lookup(currentState, ProtocolV3States.Events.AUTHENTICATION_SUCCESS);
        break;
      case KERBEROS_V5:
        break;
      case CLEAR_TEXT:
        break;
      case MD5:
        currentState = ProtocolV3States.lookup(currentState, ProtocolV3States.Events.AUTHENTICATION_REQUEST);
        byte[] content = BinaryHelper.encode(((String) properties.get(PGConnectionProperties.USER)).getBytes(StandardCharsets.UTF_8),
            ((String) properties.get(PGConnectionProperties.PASSWORD)).getBytes(StandardCharsets.UTF_8), req.getSalt());
        byte[] payload = new byte[content.length + 6];

        payload[0] = FEFrame.FrontendTag.PASSWORD_MESSAGE.getByte();
        System.arraycopy(content, 0, payload, 5, content.length);
        payload[payload.length - 1] = 0;

        outputQue.add(new FEFrame(payload, false));
        currentState = ProtocolV3States.lookup(currentState, ProtocolV3States.Events.PASSWORD_SENT);
        break;
      case SCM_CREDENTIAL:
        break;
      case GSS:
        break;
      case GSS_CONTINUE:
        break;
      case SSPI:
        break;
      case SASL:
        break;
      case SASL_CONTINUE:
        break;
      case SASL_FINAL:
        break;
    }
  }

  private void doReadyForQuery(BEFrame packet) {
    ReadyForQuery readyForQuery = new ReadyForQuery(packet.getPayload());

    //todo handle transaction stuff

    currentState = ProtocolV3States.lookup(currentState, ProtocolV3States.Events.READY_FOR_QUERY);
  }

  public void doParameterStatus(BEFrame packet) {
    ParameterStatus parameterStatus = new ParameterStatus(packet.getPayload());

    properties.put(PGConnectionProperties.lookup(parameterStatus.getName()), parameterStatus.getValue());
    currentState = ProtocolV3States.lookup(currentState, ProtocolV3States.Events.PARAMETER_STATUS);
  }
}
