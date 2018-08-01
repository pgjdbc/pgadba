package org.postgresql.sql2.execution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.postgresql.sql2.PGConnectionProperties;
import org.postgresql.sql2.testUtil.CollectorUtils;
import org.postgresql.sql2.testUtil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSource.Builder;
import jdk.incubator.sql2.DataSourceFactory;
import jdk.incubator.sql2.Submission;

/**
 * Ensures {@link NioLoop} handles communication.
 * 
 * @author Daniel Sagenschneider
 */
public class NioLoopTest {

  public static PostgreSQLContainer<?> postgres = DatabaseHolder.getCached();

  private static Builder createDataSource() {
    return DataSourceFactory
        .newFactory("org.postgresql.sql2.PGDataSourceFactory").builder().url("jdbc:postgresql://"
            + postgres.getContainerIpAddress() + ":" + postgres.getMappedPort(5432) + "/" + postgres.getDatabaseName())
        .username(postgres.getUsername()).password(postgres.getPassword());
  }

  @Test
  public void ensureDefaultNioLoop() throws Exception {
    try (DataSource dataSource = createDataSource().build()) {
      Connection connection = dataSource.getConnection();
      Submission<Integer> submission = connection.<Integer>rowOperation("SELECT 1 as t")
          .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      Integer result = submission.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
      assertEquals("Incorrect result", Integer.valueOf(1), result);
    }
  }

  @Test
  public void provideNioLoop() throws Exception {
    MockNioLoop loop = new MockNioLoop();
    try (DataSource dataSource = createDataSource().connectionProperty(PGConnectionProperties.NIO_LOOP, loop).build()) {
      Connection connection = dataSource.getConnection();

      // Undertake single request
      Submission<Integer> submission = connection.<Integer>rowOperation("SELECT 1 as t")
          .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      Integer result = submission.getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
      assertEquals("Incorrect result", Integer.valueOf(1), result);

      // Ensure provided NioLoop used
      assertTrue("Should use provided loop", loop.isUsed);
    }
  }

  @Test
  public void pipelineQueries() throws Exception {
    try (DataSource dataSource = createDataSource().build()) {
      Connection connection = dataSource.getConnection();

      // Run multiple queries over the connection
      final int QUERY_COUNT = 10;
      Submission<Integer>[] submissions = new Submission[QUERY_COUNT];
      for (int i = 0; i < QUERY_COUNT; i++) {
        submissions[i] = connection.<Integer>rowOperation("SELECT 1 as t")
            .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      }

      // Ensure obtain all results
      for (int i = 0; i < QUERY_COUNT; i++) {
        Integer result = submissions[i].getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals("Incorrect result", Integer.valueOf(1), result);
      }
    }
  }

  @Test
  public void reuseNioLoopBetweenConnections() throws Exception {
    MockNioLoop loop = new MockNioLoop();
    try (DataSource dataSource = createDataSource().connectionProperty(PGConnectionProperties.NIO_LOOP, loop).build()) {

      // Run queries on multiple connections
      final int CONNECTION_COUNT = 10;
      Submission<Integer>[] submissions = new Submission[CONNECTION_COUNT];
      for (int i = 0; i < CONNECTION_COUNT; i++) {
        Connection connection = dataSource.getConnection();
        submissions[i] = connection.<Integer>rowOperation("SELECT 1 as t")
            .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      }

      // Ensure obtain all results
      for (int i = 0; i < CONNECTION_COUNT; i++) {
        Integer result = submissions[i].getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals("Incorrect result", Integer.valueOf(1), result);
      }
    }
  }

  @Test
  public void reuseNioLoopBetweenDataSources() throws Exception {
    MockNioLoop loop = new MockNioLoop();
    try (
        DataSource dataSourceOne = createDataSource().connectionProperty(PGConnectionProperties.NIO_LOOP, loop).build();
        DataSource dataSourceTwo = createDataSource().connectionProperty(PGConnectionProperties.NIO_LOOP, loop)
            .build()) {

      // Run query via each data source
      DataSource[] dataSources = new DataSource[] { dataSourceOne, dataSourceTwo };
      Submission<Integer>[] submissions = new Submission[dataSources.length];
      for (int i = 0; i < dataSources.length; i++) {
        Connection connection = dataSources[i].getConnection();
        submissions[i] = connection.<Integer>rowOperation("SELECT 1 as t")
            .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      }

      // Ensure obtain all results
      for (int i = 0; i < dataSources.length; i++) {
        Integer result = submissions[i].getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals("Incorrect result", Integer.valueOf(1), result);
      }
    }
  }

  public static class MockNioLoop implements NioLoop {

    protected volatile boolean isUsed = false;

    @Override
    public void registerNioService(SelectableChannel channel, NioServiceFactory nioServiceFactory) throws IOException {
      // TODO Auto-generated method stub

    }
  }

}