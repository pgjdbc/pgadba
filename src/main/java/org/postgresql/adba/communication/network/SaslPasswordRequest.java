package org.postgresql.adba.communication.network;

import com.ongres.scram.client.ScramClient;
import com.ongres.scram.client.ScramSession;
import com.ongres.scram.common.stringprep.StringPreparations;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.postgresql.adba.communication.FeFrame;
import org.postgresql.adba.communication.NetworkOutputStream;
import org.postgresql.adba.communication.NetworkRequest;
import org.postgresql.adba.communication.NetworkResponse;
import org.postgresql.adba.communication.NetworkWriteContext;
import org.postgresql.adba.communication.packets.AuthenticationRequest;
import org.postgresql.adba.communication.packets.AuthenticationRequest.ScramMechanism;
import org.postgresql.adba.submissions.ConnectSubmission;
import org.postgresql.adba.util.BinaryHelper;

public class SaslPasswordRequest implements NetworkRequest {
  private final AuthenticationRequest authentication;
  private final ConnectSubmission connectSubmission;

  private ScramClient scramClient;
  private ScramSession scramSession;

  public SaslPasswordRequest(AuthenticationRequest authentication, ConnectSubmission connectSubmission) {
    this.authentication = authentication;
    this.connectSubmission = connectSubmission;
  }


  /*
   * ==================== NetworkAction ==========================
   */

  @Override
  public NetworkRequest write(NetworkWriteContext context) throws IOException {
    scramClient = ScramClient
        .channelBinding(ScramClient.ChannelBinding.NO)
        .stringPreparation(StringPreparations.NO_PREPARATION)
        .selectMechanismBasedOnServerAdvertised(authentication.getScramMechanisms().stream()
            .map(ScramMechanism::getValue).toArray(String[]::new))
        .setup();

    scramSession =
        scramClient.scramSession("*");   // Real username is ignored by server, uses startup one

    byte[] firstMessage = scramSession.clientFirstMessage().getBytes(StandardCharsets.UTF_8);

    // Write the request
    NetworkOutputStream wire = context.getOutputStream();
    wire.write(FeFrame.FrontendTag.PASSWORD_MESSAGE.getByte());
    wire.initPacket();
    wire.write(scramClient.getScramMechanism().getName());
    wire.write(BinaryHelper.writeInt(firstMessage.length));
    wire.write(firstMessage);
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
    return new SaslContinueResponse(scramSession, connectSubmission);
  }
}
