package org.postgresql.adba.communication.network;

import org.postgresql.adba.communication.FeFrame;
import org.postgresql.adba.communication.NetworkOutputStream;
import org.postgresql.adba.communication.NetworkRequest;
import org.postgresql.adba.communication.NetworkResponse;
import org.postgresql.adba.communication.NetworkWriteContext;
import org.postgresql.adba.submissions.CloseSubmission;

/**
 * Close {@link NetworkRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class CloseRequest implements NetworkRequest {

  private final CloseSubmission submission;

  /**
   * Instantiate.
   *
   * @param submission the submission this request connects to
   */
  public CloseRequest(CloseSubmission submission) {
    this.submission = submission;
  }

  @Override
  public NetworkRequest write(NetworkWriteContext context) throws Exception {

    // Send the close
    NetworkOutputStream wire = context.getOutputStream();
    wire.write(FeFrame.FrontendTag.TERMINATE.getByte());
    wire.initPacket();
    wire.completePacket();
    wire.close();

    // Nothing further
    return null;
  }

  @Override
  public NetworkResponse getRequiredResponse() {
    return new CloseResponse(submission);
  }
}