package org.postgresql.sql2;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.SqlType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.sql2.communication.packets.parts.PGAdbaType;
import org.postgresql.sql2.testUtil.ConnectUtil;
import org.postgresql.sql2.testUtil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

  @Test
  public void resultIteration() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      Integer result = conn.<Integer>rowOperation("select 1, 2, 3")
          .collect(Collector.of(
              () -> new Integer[] {0},
              (a, r) -> {
                for(Result.Column rc : r) {
                  a[0] += rc.get(Integer.class);
                };
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

  @Test
  public void resultGetThrowsOnBeforeColumn() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      try {
        conn.rowOperation("select 1, 2, 3")
            .collect(Collector.of(
                () -> new Integer[]{0},
                (a, r) -> {
                  r.get(Integer.class);
                },
                (l, r) -> null,
                a -> a[0]))
            .submit()
            .getCompletionStage()
            .toCompletableFuture()
            .get(10, TimeUnit.SECONDS);

      } catch (ExecutionException ee) {
        assertEquals("no column with position 0", ee.getCause().getMessage());
      }
    }
  }

  @Test
  public void resultIdentifierUnnamedColumn() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      String result = conn.<String>rowOperation("select 1")
          .collect(Collector.of(
              () -> new String[] {""},
              (a, r) -> {
                a[0] = r.at(1).identifier();
              },
              (l, r) -> null,
              a -> a[0]))
          .submit()
          .getCompletionStage()
          .toCompletableFuture()
          .get(10, TimeUnit.SECONDS);

      assertEquals("?column?", result);
    }
  }

  @Test
  public void resultIdentifierNamedColumn() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      String result = conn.<String>rowOperation("select 1 as t")
          .collect(Collector.of(
              () -> new String[] {""},
              (a, r) -> {
                a[0] = r.at(1).identifier();
              },
              (l, r) -> null,
              a -> a[0]))
          .submit()
          .getCompletionStage()
          .toCompletableFuture()
          .get(10, TimeUnit.SECONDS);

      assertEquals("t", result);
    }
  }

  @Test
  public void resultSqlType() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      SqlType result = conn.<SqlType>rowOperation("select 1 as t")
          .collect(Collector.of(
              () -> new SqlType[] {null},
              (a, r) -> {
                a[0] = r.at(1).sqlType();
              },
              (l, r) -> null,
              a -> a[0]))
          .submit()
          .getCompletionStage()
          .toCompletableFuture()
          .get(10, TimeUnit.SECONDS);

      assertEquals(PGAdbaType.INTEGER, result);
    }
  }

  @Test
  public void resultSqlTypeIllegalColumn() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      try {
        conn.<SqlType>rowOperation("select 1 as t")
            .collect(Collector.of(
                () -> new SqlType[]{null},
                (a, r) -> {
                  a[0] = r.sqlType();
                },
                (l, r) -> null,
                a -> a[0]))
            .submit()
            .getCompletionStage()
            .toCompletableFuture()
            .get(10, TimeUnit.SECONDS);

        fail("an ExecutionException should have been thrown");
      } catch (ExecutionException ee) {
        assertEquals("no column with id 0", ee.getCause().getMessage());
      }
    }
  }

  @Test
  public void resultJavaType() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      Class result = conn.<Class>rowOperation("select 1 as t")
          .collect(Collector.of(
              () -> new Class[] {null},
              (a, r) -> {
                a[0] = r.at(1).javaType();
              },
              (l, r) -> null,
              a -> a[0]))
          .submit()
          .getCompletionStage()
          .toCompletableFuture()
          .get(10, TimeUnit.SECONDS);

      assertEquals(Integer.class, result);
    }
  }

  @Test
  public void resultJavaTypeIllegalColumn() throws InterruptedException, ExecutionException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      try {
        conn.<Class>rowOperation("select 1 as t")
            .collect(Collector.of(
                () -> new Class[]{null},
                (a, r) -> {
                  a[0] = r.javaType();
                },
                (l, r) -> null,
                a -> a[0]))
            .submit()
            .getCompletionStage()
            .toCompletableFuture()
            .get(10, TimeUnit.SECONDS);

        fail("an ExecutionException should have been thrown");
      } catch (ExecutionException ee) {
        assertEquals("no column with id 0", ee.getCause().getMessage());
      }
    }
  }
}
