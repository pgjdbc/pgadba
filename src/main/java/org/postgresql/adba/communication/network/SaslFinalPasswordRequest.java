package org.postgresql.adba.communication.network;

import com.ongres.scram.client.ScramSession;
import com.ongres.scram.client.ScramSession.ServerFirstProcessor;
import java.nio.charset.StandardCharsets;
import jdk.incubator.sql2.AdbaSessionProperty;
import org.postgresql.adba.communication.FrontendTag;
import org.postgresql.adba.communication.NetworkOutputStream;
import org.postgresql.adba.communication.NetworkRequest;
import org.postgresql.adba.communication.NetworkResponse;
import org.postgresql.adba.communication.NetworkWriteContext;
import org.postgresql.adba.submissions.ConnectSubmission;
import org.postgresql.adba.util.PropertyHolder;

public class SaslFinalPasswordRequest implements NetworkRequest {

  private ServerFirstProcessor serverFirstProcessor;
  private ConnectSubmission connectSubmission;
  private ScramSession.ClientFinalProcessor clientFinalProcessor;

  public SaslFinalPasswordRequest(ServerFirstProcessor serverFirstProcessor,
      ConnectSubmission connectSubmission) {
    this.serverFirstProcessor = serverFirstProcessor;
    this.connectSubmission = connectSubmission;
  }

  @Override
  public NetworkRequest write(NetworkWriteContext context) throws Exception {
    // Obtain the properties
    PropertyHolder properties = context.getProperties();

    String password = (String) properties.get(AdbaSessionProperty.PASSWORD);

    clientFinalProcessor = serverFirstProcessor.clientFinalProcessor(password);

    String clientFinalMessage = clientFinalProcessor.clientFinalMessage();
    byte[] clientFinalMessageBytes = clientFinalMessage.getBytes(StandardCharsets.UTF_8);

    NetworkOutputStream wire = context.getOutputStream();
    wire.write(FrontendTag.PASSWORD_MESSAGE.getByte());
    wire.initPacket();
    wire.write(clientFinalMessageBytes);
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
    return new SaslCompleteResponse(connectSubmission, clientFinalProcessor);
  }
}
