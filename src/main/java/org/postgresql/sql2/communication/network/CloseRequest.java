package org.postgresql.sql2.communication.network;

import org.postgresql.sql2.communication.FeFrame;
import org.postgresql.sql2.communication.NetworkOutputStream;
import org.postgresql.sql2.communication.NetworkRequest;
import org.postgresql.sql2.communication.NetworkResponse;
import org.postgresql.sql2.communication.NetworkWriteContext;
import org.postgresql.sql2.submissions.CloseSubmission;

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