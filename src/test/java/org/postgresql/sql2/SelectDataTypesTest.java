package org.postgresql.sql2;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collector;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SelectDataTypesTest {
  @ClassRule
  public static PostgreSQLContainer postgres = new PostgreSQLContainer();

  private static DataSource ds;

  @BeforeClass
  public static void setUp() {
    ds = TestUtil.openDB(postgres);

    TestUtil.createTable(ds, "tab",
        "id int", "name varchar(100)", "answer int");
  }

  @AfterClass
  public static void tearDown() {
    ds.close();
    postgres.close();
  }

  @Test
  public void selectInteger4() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select 100 as t")
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
      CompletionStage<Short> idF = conn.<Short>rowOperation("select 100::int2 as t")
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
      CompletionStage<Long> idF = conn.<Long>rowOperation("select 100::int8 as t")
          .collect(Collector.of(
              () -> new long[1],
              (a, r) -> a[0] = r.get("t", Long.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf((short) 100), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectNumeric() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<BigDecimal> idF = conn.<BigDecimal>rowOperation("select 100.505::numeric as t")
          .collect(Collector.of(
              () -> new BigDecimal[1],
              (a, r) -> a[0] = r.get("t", BigDecimal.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(BigDecimal.valueOf(100.505), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectReal() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Float> idF = conn.<Float>rowOperation("select 100.505::real as t")
          .collect(Collector.of(
              () -> new float[1],
              (a, r) -> a[0] = r.get("t", Float.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals((float) 100.505, idF.toCompletableFuture().get(), 0.5);
    }
  }

  @Test
  public void selectDouble() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Double> idF = conn.<Double>rowOperation("select 100.505::double precision as t")
          .collect(Collector.of(
              () -> new double[1],
              (a, r) -> a[0] = r.get("t", Double.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals((float) 100.505, idF.toCompletableFuture().get(), 0.5);
    }
  }

  @Test
  public void selectMoney() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select 100.505::money as t")
          .collect(Collector.of(
              () -> new String[1],
              (a, r) -> a[0] = r.get("t", String.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals("$100.51", idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectVarchar() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select 'Sphinx of black quartz, judge my vow'::varchar as t")
          .collect(Collector.of(
              () -> new String[1],
              (a, r) -> a[0] = r.get("t", String.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals("Sphinx of black quartz, judge my vow", idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectCharacter() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select 'H'::character as t")
          .collect(Collector.of(
              () -> new Character[1],
              (a, r) -> a[0] = r.get("t", Character.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('H'), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectText() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select 'How vexingly quick daft zebras jump!'::text as t")
          .collect(Collector.of(
              () -> new String[1],
              (a, r) -> a[0] = r.get("t", String.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals("How vexingly quick daft zebras jump!", idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectBytea() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<byte[]> idF = conn.<byte[]>rowOperation("select 'DEADBEEF'::bytea as t")
          .collect(Collector.of(
              () -> new byte[][]{new byte[0]},
              (a, r) -> a[0] = r.get("t", byte[].class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      byte[] expected = new byte[]{0x44, 0x45, 0x41, 0x44, 0x42, 0x45, 0x45, 0x46};
      assertArrayEquals(expected, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectTimestampWithoutTimeZone() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDateTime> idF = conn.<LocalDateTime>rowOperation("select '2018-04-29 20:55:57.692132'::timestamp without time zone as t")
          .collect(Collector.of(
              () -> new LocalDateTime[1],
              (a, r) -> a[0] = r.get("t", LocalDateTime.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(LocalDateTime.of(2018, 4, 29, 20, 55, 57, 692132000), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectTimestampWithTimeZone() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetDateTime> idF = conn.<OffsetDateTime>rowOperation("select '2018-04-29 20:55:57.692132'::timestamp with time zone as t")
          .collect(Collector.of(
              () -> new OffsetDateTime[1],
              (a, r) -> a[0] = r.get("t", OffsetDateTime.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(OffsetDateTime.of(2018, 4, 29, 20, 55, 57, 692132000, ZoneOffset.UTC), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectDate() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDate> idF = conn.<LocalDate>rowOperation("select '2018-04-29 20:55:57.692132'::date as t")
          .collect(Collector.of(
              () -> new LocalDate[1],
              (a, r) -> a[0] = r.get("t", LocalDate.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(LocalDate.of(2018, 4, 29), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectTime() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalTime> idF = conn.<LocalTime>rowOperation("select '2018-04-29 20:55:57.692132'::time as t")
          .collect(Collector.of(
              () -> new LocalTime[1],
              (a, r) -> a[0] = r.get("t", LocalTime.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(LocalTime.of(20, 55, 57, 692132000), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectTimeWithTimeZone() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetTime> idF = conn.<OffsetTime>rowOperation("select '2018-04-29 20:55:57.692132'::time with time zone as t")
          .collect(Collector.of(
              () -> new OffsetTime[1],
              (a, r) -> a[0] = r.get("t", OffsetTime.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(OffsetTime.of(20, 55, 57, 692132000, ZoneOffset.UTC), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectInterval() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Duration> idF = conn.<Duration>rowOperation("select INTERVAL '2 months 3 days' as t")
          .collect(Collector.of(
              () -> new Duration[1],
              (a, r) -> a[0] = r.get("t", Duration.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertEquals(Duration.of(1512, ChronoUnit.HOURS), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectEnum() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      conn.countOperation("CREATE TYPE happiness AS ENUM ('happy', 'very happy', 'ecstatic');").submit();
      CompletionStage<String[]> idF = conn.<String[]>rowOperation("SELECT unnest(enum_range(NULL::happiness)) as t")
          .collect(Collector.of(
              () -> new String[3],
              (a, r) -> a[(int) r.rowNumber()] = r.get("t", String.class),
              (l, r) -> null,
              a -> a)
          )
          .submit()
          .getCompletionStage();

      assertArrayEquals(new String[] {"happy", "very happy", "ecstatic"}, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectBoolean() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Boolean> idF = conn.<Boolean>rowOperation("select true::bool as t")
          .collect(Collector.of(
              () -> new boolean[1],
              (a, r) -> a[0] = r.get("t", Boolean.class),
              (l, r) -> null,
              a -> a[0])
          )
          .submit()
          .getCompletionStage();

      assertTrue(idF.toCompletableFuture().get());
    }
  }
}
