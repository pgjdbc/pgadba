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
      CompletionStage<Short> idF = conn.<Short>rowOperation("select $1::int2 as t")
          .set("$1", 100, PGAdbaType.SMALLINT)
          .collect(Collector.of(
              () -> new short[1],
              (a, r) -> a[0] = r.get("t", Short.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short) 100), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectInteger8() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Long> idF = conn.<Long>rowOperation("select $1::int8 as t")
          .set("$1", 100, PGAdbaType.BIGINT)
          .collect(Collector.of(
              () -> new long[1],
              (a, r) -> a[0] = r.get("t", Long.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectVarchar() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::varchar as t")
          .set("$1", "a text I wrote", PGAdbaType.VARCHAR)
          .collect(Collector.of(
              () -> new String[1],
              (a, r) -> a[0] = r.get("t", String.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals("a text I wrote", idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectChar() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select $1::char as t")
          .set("$1", 'R', PGAdbaType.CHAR)
          .collect(Collector.of(
              () -> new Character[1],
              (a, r) -> a[0] = r.get("t", Character.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('R'), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectCharNorwegianChar() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select $1::char as t")
          .set("$1", 'Ø', PGAdbaType.CHAR)
          .collect(Collector.of(
              () -> new Character[1],
              (a, r) -> a[0] = r.get("t", Character.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('Ø'), idF.toCompletableFuture().get());
    }
  }

}
