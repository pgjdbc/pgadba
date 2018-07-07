package org.postgresql.sql2.communication;

import jdk.incubator.sql2.ConnectionProperty;
import jdk.incubator.sql2.SqlException;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnectionProperties;
import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.communication.packets.AuthenticationRequest;
import org.postgresql.sql2.communication.packets.CommandComplete;
import org.postgresql.sql2.communication.packets.DataRow;
import org.postgresql.sql2.communication.packets.ErrorResponse;
import org.postgresql.sql2.communication.packets.ParameterStatus;
import org.postgresql.sql2.communication.packets.ReadyForQuery;
import org.postgresql.sql2.communication.packets.RowDescription;
import org.postgresql.sql2.operations.helpers.FEFrameSerializer;
import org.postgresql.sql2.util.BinaryHelper;
import org.postgresql.sql2.util.PGCount;
import org.postgresql.sql2.util.PreparedStatementCache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NoConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.postgresql.sql2.communication.packets.parts.ErrorResponseField.Types.DETAIL;
import static org.postgresql.sql2.communication.packets.parts.ErrorResponseField.Types.HINT;
import static org.postgresql.sql2.communication.packets.parts.ErrorResponseField.Types.MESSAGE;
import static org.postgresql.sql2.communication.packets.parts.ErrorResponseField.Types.SEVERITY;
import static org.postgresql.sql2.communication.packets.parts.ErrorResponseField.Types.SQLSTATE_CODE;

public class ProtocolV3 {
  private ProtocolV3States.States currentState = ProtocolV3States.States.NOT_CONNECTED;
  private Map<ConnectionProperty, Object> properties;
  private PreparedStatementCache preparedStatementCache = new PreparedStatementCache();

  private ConcurrentLinkedQueue<FEFrame> outputQue = new ConcurrentLinkedQueue<>();
  private ConcurrentLinkedQueue<FEFrame> waitToSendQue = new ConcurrentLinkedQueue<>();

  private ConcurrentLinkedQueue<PGSubmission> submissions = new ConcurrentLinkedQueue<>();

  private BEFrameReader BEFrameReader = new BEFrameReader();

  private SocketChannel socketChannel;

  private boolean sentStartPacket = false;

  private long rowNumber = 0;

  private ConcurrentLinkedQueue<String> descriptionNameQue = new ConcurrentLinkedQueue<>();
  private ConcurrentLinkedQueue<String> sentSqlNameQue = new ConcurrentLinkedQueue<>();

  public ProtocolV3(Map<ConnectionProperty, Object> properties) {
    this.properties = properties;
    try {
      this.socketChannel = SocketChannel.open();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void visit() {
    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    PGSubmission sub = submissions.peek();
    if (sub != null) {
      try {
        if (sub.getCompletionType() == PGSubmission.Types.CONNECT && sub.getSendConsumed().compareAndSet(false, true)) {
          socketChannel.configureBlocking(false);
          socketChannel.connect(new InetSocketAddress((String) properties.get(PGConnectionProperties.HOST),
              (Integer) properties.get(PGConnectionProperties.PORT)));
        }
        if (!socketChannel.finishConnect()) {
          return;
        } else if (!sentStartPacket) {
          sendStartupPacket();
          sentStartPacket = true;
        }

        if(sub.getCompletionType() == PGSubmission.Types.LOCAL || sub.getCompletionType() == PGSubmission.Types.CATCH) {
          sub.finish(null);
          submissions.poll();
        } else if(sub.getCompletionType() == PGSubmission.Types.GROUP) {
          sub.finish(null);
          submissions.poll();
        } else if (sub.getCompletionType() != PGSubmission.Types.CONNECT && currentState == ProtocolV3States.States.IDLE
            && sub.getSendConsumed().compareAndSet(false, true)) {
          if (preparedStatementCache.sqlNotPreparedBefore(sub.getHolder(), sub.getSql())) {
            queFrame(FEFrameSerializer.toParsePacket(sub.getHolder(), sub.getSql(), preparedStatementCache));
          }
          queFrame(FEFrameSerializer.toDescribePacket(sub.getHolder(), sub.getSql(), preparedStatementCache));
          descriptionNameQue.add(preparedStatementCache.getNameForQuery(sub.getSql(), sub.getParamTypes()));
          for(int i = 0; i < sub.numberOfQueryRepetitions(); i++) {
            queFrame(FEFrameSerializer.toBindPacket(sub.getHolder(), sub.getSql(), preparedStatementCache, i));
            queFrame(FEFrameSerializer.toExecutePacket(sub.getHolder(), sub.getSql(), preparedStatementCache));
            sentSqlNameQue.add(preparedStatementCache.getNameForQuery(sub.getSql(), sub.getParamTypes()));
            queFrame(FEFrameSerializer.toSyncPacket());
          }
        }

        try {
          int bytesRead = socketChannel.read(readBuffer);
          BEFrameReader.updateState(readBuffer, bytesRead);
        } catch (NotYetConnectedException e) {
        }

        BEFrame packet;
        while ((packet = BEFrameReader.popFrame()) != null) {
          readPacket(packet);
        }

        sendData(socketChannel);

        if (outputQue.size() == 0 && waitToSendQue.size() == 0 && sub.getCompletionType() == PGSubmission.Types.CLOSE) {
          try {
            socketChannel.close();
            ((CompletableFuture) sub.getCompletionStage())
                .complete(sub.finish(null));
          } catch (IOException e) {
            ((CompletableFuture) sub.getCompletionStage())
                .completeExceptionally(e);
          }
        }
      } catch (NoConnectionPendingException ignore) {
      } catch (Throwable e) {
        ((CompletableFuture) sub.getCompletionStage())
            .completeExceptionally(e);
      }
    }
  }

  public void readPacket(BEFrame packet) {
    switch (packet.getTag()) {
      case AUTHENTICATION:
        doAuthentication(packet);
        break;
      case CANCELLATION_KEY_DATA:
        break;
      case BIND_COMPLETE:
        doBindComplete(packet);
        break;
      case CLOSE_COMPLETE:
        break;
      case COMMAND_COMPLETE:
        doCommandComplete(packet);
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
        doDataRow(packet);
        break;
      case EMPTY_QUERY_RESPONSE:
        break;
      case ERROR_RESPONSE:
        doError(packet);
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
        doParseComplete(packet);
        break;
      case PORTAL_SUSPENDED:
        break;
      case READY_FOR_QUERY:
        doReadyForQuery(packet);
        break;
      case ROW_DESCRIPTION:
        doRowDescription(packet);
        break;
    }
  }

  private void doRowDescription(BEFrame packet) {
    RowDescription rowDescription = new RowDescription(packet.getPayload());
    String portalName = descriptionNameQue.poll();
    preparedStatementCache.addDescriptionToPortal(portalName, rowDescription.getDescriptions());
  }

  private void doDataRow(BEFrame packet) {
    String portalName = sentSqlNameQue.peek();
    DataRow row = new DataRow(packet.getPayload(), preparedStatementCache.getDescription(portalName), rowNumber++);
    PGSubmission sub = submissions.peek();
    sub.addRow(row);
  }

  private void doCommandComplete(BEFrame packet) {
    CommandComplete cc = new CommandComplete(packet.getPayload());

    PGSubmission sub = submissions.peek();
    if(sub == null) {
      throw new IllegalStateException("Command Complete packet arrived without an corresponding submission, internal state corruption");
    }

    switch (sub.getCompletionType()) {
      case COUNT:
        sub.finish(new PGCount(cc.getNumberOfRowsAffected()));
        submissions.poll();
        break;
      case ROW:
        sentSqlNameQue.poll();
        sub.finish(null);
        submissions.poll();
        break;
      case CLOSE:
        try {
          socketChannel.close();
          ((CompletableFuture) sub.getCompletionStage())
              .complete(sub.finish(null));
        } catch (IOException e) {
          ((CompletableFuture) sub.getCompletionStage())
              .completeExceptionally(e);
        }
        submissions.poll();
        break;
      case TRANSACTION:
        sub.finish(cc.getType());
        submissions.poll();
        break;
      case ARRAY_COUNT:
        boolean allCollected = (Boolean)sub.finish(cc.getNumberOfRowsAffected());
        if(allCollected) {
          submissions.poll();
        }
        break;
      case VOID:
        ((CompletableFuture) sub.getCompletionStage())
            .complete(null);
        submissions.poll();
        break;
      case PROCESSOR:
        sub.finish(null);
        submissions.poll();
        break;
      case OUT_PARAMETER:
        sub.finish(null);
        submissions.poll();
        break;
    }

    currentState = ProtocolV3States.lookup(currentState, ProtocolV3States.Events.COMMAND_COMPLETE);
  }

  private void doBindComplete(BEFrame packet) {
    currentState = ProtocolV3States.lookup(currentState, ProtocolV3States.Events.BIND_COMPLETE);
  }

  private void doParseComplete(BEFrame packet) {
    currentState = ProtocolV3States.lookup(currentState, ProtocolV3States.Events.PARSE_COMPLETE);
  }

  private void doError(BEFrame packet) {
    ErrorResponse error = new ErrorResponse(packet.getPayload());

    StringBuilder message = new StringBuilder("Severity: " + error.getField(SEVERITY) +
        "\nMessage: " + error.getField(MESSAGE));

    if (error.getField(DETAIL) != null)
      message.append("\nDetail: ").append(error.getField(DETAIL));
    if (error.getField(HINT) != null)
      message.append("\nHint: ").append(error.getField(HINT));

    PGSubmission sub = submissions.poll();

    SqlException exception = new SqlException(message.toString(), null, error.getField(SQLSTATE_CODE), 0,
        sub.getSql(), 0);

    if(sub.getErrorHandler() != null) {
      sub.getErrorHandler().accept(exception);
    }

    ((CompletableFuture) sub.getCompletionStage())
        .completeExceptionally(exception);
  }

  public synchronized void sendData(SocketChannel socketChannel) {
    if (outputQue.size() == 0 && waitToSendQue.size() != 0 && currentState == ProtocolV3States.States.IDLE) {
      outputQue.add(waitToSendQue.poll());
    }
    if (outputQue.size() != 0) {
      FEFrame packet = outputQue.peek();

      if (packet == null) {
        return;
      }

      try {
        socketChannel.write(packet.getPayload());
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (!packet.hasRemaining()) {
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
        Submission sub = submissions.poll();
        sub.getCompletionStage().toCompletableFuture().complete(null);
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
    rowNumber = 0;

    ReadyForQuery readyForQuery = new ReadyForQuery(packet.getPayload());

    //todo handle transaction stuff

    currentState = ProtocolV3States.lookup(currentState, ProtocolV3States.Events.READY_FOR_QUERY);
  }

  public void doParameterStatus(BEFrame packet) {
    ParameterStatus parameterStatus = new ParameterStatus(packet.getPayload());

    properties.put(PGConnectionProperties.lookup(parameterStatus.getName()), parameterStatus.getValue());
    currentState = ProtocolV3States.lookup(currentState, ProtocolV3States.Events.PARAMETER_STATUS);
  }

  public void addSubmission(PGSubmission submission) {
    submissions.add(submission);
  }

  public boolean isConnectionClosed() {
    return !socketChannel.isConnected();
  }
}
