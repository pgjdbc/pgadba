package org.postgresql.adba.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.postgresql.adba.testutil.FutureUtil.get10;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSource.Builder;
import jdk.incubator.sql2.DataSourceFactory;
import jdk.incubator.sql2.Session;
import jdk.incubator.sql2.Submission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.postgresql.adba.PgDataSourceProperty;
import org.postgresql.adba.testutil.CollectorUtils;
import org.postgresql.adba.testutil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Ensures {@link NioLoop} handles communication.
 * 
 * @author Daniel Sagenschneider
 */
public class NioLoopTest {

  public static PostgreSQLContainer<?> postgres = DatabaseHolder.getCached();

  private static Builder createDataSource() {
    return DataSourceFactory
        .newFactory("org.postgresql.adba.PgDataSourceFactory").builder().url("jdbc:postgresql://"
            + postgres.getContainerIpAddress() + ":" + postgres.getMappedPort(5432) + "/" + postgres.getDatabaseName())
        .username(postgres.getUsername()).password(postgres.getPassword());
  }

  @Test
  public void ensureDefaultNioLoop() throws Exception {
    try (DataSource dataSource = createDataSource().build()) {
      Session session = dataSource.getSession();
      Submission<Integer> submission = session.<Integer>rowOperation("SELECT 1 as t")
          .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      Integer result = get10(submission.getCompletionStage());
      assertEquals(Integer.valueOf(1), result, "Incorrect result");
    }
  }

  @Test
  public void provideNioLoop() throws Exception {
    MockNioLoop loop = new MockNioLoop();
    try (DataSource dataSource = createDataSource().property(PgDataSourceProperty.NIO_LOOP, loop).build()) {
      Session session = dataSource.getSession();

      // Undertake single request
      Submission<Integer> submission = session.<Integer>rowOperation("SELECT 1 as t")
          .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      Integer result = get10(submission.getCompletionStage());
      assertEquals(Integer.valueOf(1), result, "Incorrect result");

      // Ensure provided NioLoop used
      assertTrue(loop.isUsed, "Should use provided loop");
    }
  }

  @Test
  public void pipelineQueries() throws Exception {
    try (DataSource dataSource = createDataSource().build()) {
      Session session = dataSource.getSession();

      // Run multiple queries over the connection
      final int queryCount = 1000;
      Submission<Integer>[] submissions = new Submission[queryCount];
      for (int i = 0; i < queryCount; i++) {
        submissions[i] = session.<Integer>rowOperation("SELECT 1 as t")
            .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      }

      // Ensure obtain all results
      for (int i = 0; i < queryCount; i++) {
        Integer result = get10(submissions[i].getCompletionStage());
        assertEquals(Integer.valueOf(1), result, "Incorrect result");
      }
    }
  }

  @Test
  public void reuseNioLoopBetweenConnections() throws Exception {
    MockNioLoop loop = new MockNioLoop();
    try (DataSource dataSource = createDataSource().property(PgDataSourceProperty.NIO_LOOP, loop).build()) {

      // Run queries on multiple connections
      final int connectionCount = 10;
      Submission<Integer>[] submissions = new Submission[connectionCount];
      for (int i = 0; i < connectionCount; i++) {
        Session session = dataSource.getSession();
        submissions[i] = session.<Integer>rowOperation("SELECT 1 as t")
            .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      }

      // Ensure obtain all results
      for (int i = 0; i < connectionCount; i++) {
        Integer result = get10(submissions[i].getCompletionStage());
        assertEquals(Integer.valueOf(1), result, "Incorrect result");
      }
    }
  }

  @Test
  public void reuseNioLoopBetweenDataSources() throws Exception {
    MockNioLoop loop = new MockNioLoop();
    try (
        DataSource dataSourceOne = createDataSource().property(PgDataSourceProperty.NIO_LOOP, loop).build();
        DataSource dataSourceTwo = createDataSource().property(PgDataSourceProperty.NIO_LOOP, loop)
            .build()) {

      // Run query via each data source
      DataSource[] dataSources = new DataSource[] { dataSourceOne, dataSourceTwo };
      Submission<Integer>[] submissions = new Submission[dataSources.length];
      for (int i = 0; i < dataSources.length; i++) {
        Session session = dataSources[i].getSession();
        submissions[i] = session.<Integer>rowOperation("SELECT 1 as t")
            .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      }

      // Ensure obtain all results
      for (int i = 0; i < dataSources.length; i++) {
        Integer result = get10(submissions[i].getCompletionStage());
        assertEquals(Integer.valueOf(1), result, "Incorrect result");
      }
    }
  }

  @AfterEach
  public void closeNioLoop() {
    if (this.mockLoop != null) {
      this.mockLoop.close();
    }
  }

  private MockNioLoop mockLoop;

  public class MockNioLoop extends DefaultNioLoop {

    protected volatile boolean isUsed = false;

    public MockNioLoop() {
      NioLoopTest.this.mockLoop = this;
      new Thread(this).start();
    }

    @Override
    public NioService registerNioService(SelectableChannel channel, NioServiceFactory nioServiceFactory)
        throws IOException {
      this.isUsed = true;
      return super.registerNioService(channel, nioServiceFactory);
    }
  }

}