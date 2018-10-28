package org.postgresql.sql2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.Session;
import jdk.incubator.sql2.SqlType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.sql2.communication.packets.parts.PgAdbaType;
import org.postgresql.sql2.testutil.ConnectUtil;
import org.postgresql.sql2.testutil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

public class ResultTest {
  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

  private static DataSource ds;

  @BeforeAll
  public static void setUp() {
    ds = ConnectUtil.openDb(postgres);
  }

  @AfterAll
  public static void tearDown() {
    ds.close();
  }

  @Test
  public void resultForEach() throws InterruptedException, ExecutionException, TimeoutException {
    try (Session session = ds.getSession()) {
      Integer result = session.<Integer>rowOperation("select 1, 2, 3")
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
          .get(1000, TimeUnit.SECONDS);

      assertEquals(Integer.valueOf(6), result);
    }
  }

  @Test
  public void resultIteration() throws InterruptedException, ExecutionException, TimeoutException {
    try (Session session = ds.getSession()) {
      Integer result = session.<Integer>rowOperation("select 1, 2, 3")
          .collect(Collector.of(
              () -> new Integer[] {0},
              (a, r) -> {
                for (Result.Column rc : r) {
                  a[0] += rc.get(Integer.class);
                }
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
    try (Session session = ds.getSession()) {
      try {
        session.rowOperation("select 1, 2, 3")
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
    try (Session session = ds.getSession()) {
      String result = session.<String>rowOperation("select 1")
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
    try (Session session = ds.getSession()) {
      String result = session.<String>rowOperation("select 1 as t")
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
    try (Session session = ds.getSession()) {
      SqlType result = session.<SqlType>rowOperation("select 1 as t")
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

      assertEquals(PgAdbaType.INTEGER, result);
    }
  }

  @Test
  public void resultIllegalColumn() throws InterruptedException, ExecutionException, TimeoutException {
    try (Session session = ds.getSession()) {
      try {
        session.<SqlType>rowOperation("select 1 as t")
            .collect(Collector.of(
                () -> new SqlType[]{null},
                (a, r) -> {
                  r.at("0");
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
    try (Session session = ds.getSession()) {
      Class result = session.<Class>rowOperation("select 1 as t")
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
  public void resultClone() throws InterruptedException, ExecutionException, TimeoutException {
    try (Session session = ds.getSession()) {
      Integer result = session.<Integer>rowOperation("select 1 union all select 2 union all select 3")
          .collect(Collector.of(
              () -> new Integer[]{0},
              (a, r) -> {
                a[0] += r.at(1).get(Integer.class);
                a[0] += r.clone().get(Integer.class);
              },
              (l, r) -> null,
              a -> a[0]))
          .submit()
          .getCompletionStage()
          .toCompletableFuture()
          .get(10, TimeUnit.SECONDS);

      assertEquals(Integer.valueOf(12), result);
    }
  }
}
