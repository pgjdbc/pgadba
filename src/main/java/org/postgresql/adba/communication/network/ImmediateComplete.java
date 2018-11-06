package org.postgresql.adba.communication.network;

import org.postgresql.adba.PgSubmission;
import org.postgresql.adba.communication.NetworkRequest;
import org.postgresql.adba.communication.NetworkWriteContext;

/**
 * Immediate completes the {@link PgSubmission}.
 * 
 * @author Daniel Sagenschneider
 */
public class ImmediateComplete implements NetworkRequest {

  private final PgSubmission<?> submission;

  public ImmediateComplete(PgSubmission<?> submission) {
    this.submission = submission;
  }

  @Override
  public NetworkRequest write(NetworkWriteContext context) throws Exception {
    submission.finish(null);
    return null;
  }

}