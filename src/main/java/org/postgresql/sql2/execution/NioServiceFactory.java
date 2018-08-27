package org.postgresql.sql2.execution;

import java.io.IOException;

/**
 * Factory to create the {@link NioService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface NioServiceFactory {

  /**
   * Creates the {@link NioService}.
   * 
   * @param context {@link NioServiceContext}.
   * @return {@link NioService}.
   * @throws IOException If fails to create the {@link NioService}.
   */
  NioService createNioService(NioServiceContext context) throws IOException;

}
