package org.postgresql.sql2;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.postgresql.sql2.communication.packets.parts.PGAdbaType;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BindParameterTypesTest {
  @ClassRule
  public static PostgreSQLContainer postgres = new PostgreSQLContainer();

  private static DataSource ds;

  @BeforeClass
  public static void setUp() {
    ds = TestUtil.openDB(postgres);
  }

  @AfterClass
  public static void tearDown() {
    ds.close();
    postgres.close();
  }

  @Test
  public void selectNullAsInteger4() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", null, PGAdbaType.INTEGER)
          .collect(Collector.of(
              () -> new Integer[1],
              (a, r) -> a[0] = r.get("t", Integer.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertNull(idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectInteger4() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", 100, PGAdbaType.INTEGER)
          .collect(Collector.of(
              () -> new int[1],
              (a, r) -> a[0] = r.get("t", Integer.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectInteger2() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int2 as t")
          .set("$1", 100, PGAdbaType.SMALLINT)
          .collect(Collector.of(
              () -> new int[1],
              (a, r) -> a[0] = r.get("t", Short.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), idF.toCompletableFuture().get());
    }
  }

}
