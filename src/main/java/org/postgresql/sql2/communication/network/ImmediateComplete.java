package org.postgresql.sql2.communication.network;

import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.communication.NetworkRequest;
import org.postgresql.sql2.communication.NetworkWriteContext;

/**
 * Immediate completes the {@link PGSubmission}.
 * 
 * @author Daniel Sagenschneider
 */
public class ImmediateComplete implements NetworkRequest {

  private final PGSubmission<?> submission;

  public ImmediateComplete(PGSubmission<?> submission) {
    this.submission = submission;
  }

  @Override
  public NetworkRequest write(NetworkWriteContext context) throws Exception {
    this.submission.finish(null);
    return null;
  }

}