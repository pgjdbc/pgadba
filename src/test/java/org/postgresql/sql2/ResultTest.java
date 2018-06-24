package org.postgresql.sql2;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.sql2.testUtil.ConnectUtil;
import org.postgresql.sql2.testUtil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;

import static org.junit.Assert.assertEquals;

public class ResultTest {
  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

  private static DataSource ds;

  @BeforeClass
  public static void setUp() {
    ds = ConnectUtil.openDB(postgres);
  }

  @AfterClass
  public static void tearDown() {
    ds.close();
  }

  @Test
  public void resultForEach() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      Integer result = conn.<Integer>rowOperation("select 1, 2, 3")
          .collect(Collector.of(
              () -> new Integer[] {0},
              (a, r) -> {
                r.forEach(rc -> {
                  a[0] += rc.get(Integer.class);
                });
              },
              (l, r) -> null,
              a -> a[0]))
          .submit()
          .getCompletionStage()
          .toCompletableFuture()
          .get(10, TimeUnit.SECONDS);

      assertEquals(Integer.valueOf(6), result);
    }
  }
}
