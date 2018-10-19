package org.postgresql.sql2.communication.network;

import org.postgresql.sql2.PgSubmission;
import org.postgresql.sql2.communication.NetworkRequest;
import org.postgresql.sql2.communication.NetworkWriteContext;

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