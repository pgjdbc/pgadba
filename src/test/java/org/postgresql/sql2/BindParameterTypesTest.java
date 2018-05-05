package org.postgresql.sql2;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.postgresql.sql2.communication.packets.parts.PGAdbaType;
import org.postgresql.sql2.testUtil.ConnectUtil;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.postgresql.sql2.testUtil.CollectorUtils.singleCollector;

public class BindParameterTypesTest {
  @ClassRule
  public static PostgreSQLContainer postgres = new PostgreSQLContainer();

  private static DataSource ds;

  @BeforeClass
  public static void setUp() {
    ds = ConnectUtil.openDB(postgres);
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
          .collect(singleCollector(Integer.class))
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
          .collect(singleCollector(Integer.class))
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
          .collect(singleCollector(Short.class))
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
          .collect(singleCollector(Long.class))
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
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("a text I wrote", idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectVarCharNorwegianChar() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::varchar as t")
          .set("$1", "Brød har lenge vore ein viktig del av norsk kosthald.", PGAdbaType.VARCHAR)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Brød har lenge vore ein viktig del av norsk kosthald.", idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectChar() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select $1::char as t")
          .set("$1", 'R', PGAdbaType.CHAR)
          .collect(singleCollector(Character.class))
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
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('Ø'), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectLongVarCharNorwegianChar() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::text as t")
          .set("$1", "Som regel i form av smørbrød til frukost og lunsj.", PGAdbaType.LONGVARCHAR)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Som regel i form av smørbrød til frukost og lunsj.", idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectDate() throws ExecutionException, InterruptedException {
    LocalDate d = LocalDate.now();

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDate> idF = conn.<LocalDate>rowOperation("select $1::date as t")
          .set("$1", d, PGAdbaType.DATE)
          .collect(singleCollector(LocalDate.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectTime() throws ExecutionException, InterruptedException {
    LocalTime d = LocalTime.now();

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalTime> idF = conn.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", d, PGAdbaType.TIME)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectTimeWithTimeZone() throws ExecutionException, InterruptedException {
    OffsetTime d = OffsetTime.now(ZoneOffset.UTC);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetTime> idF = conn.<OffsetTime>rowOperation("select $1::time with time zone as t")
          .set("$1", d, PGAdbaType.TIME_WITH_TIME_ZONE)
          .collect(singleCollector(OffsetTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectTimestamp() throws ExecutionException, InterruptedException {
    LocalDateTime d = LocalDateTime.now();

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDateTime> idF = conn.<LocalDateTime>rowOperation("select $1::timestamp as t")
          .set("$1", d, PGAdbaType.TIMESTAMP)
          .collect(singleCollector(LocalDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectTimestampWithTimeZone() throws ExecutionException, InterruptedException {
    OffsetDateTime d = OffsetDateTime.now(ZoneOffset.UTC);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetDateTime> idF = conn.<OffsetDateTime>rowOperation("select $1::timestamp with time zone as t")
          .set("$1", d, PGAdbaType.TIMESTAMP_WITH_TIME_ZONE)
          .collect(singleCollector(OffsetDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectNumerical() throws ExecutionException, InterruptedException {
    BigDecimal d = BigDecimal.TEN;

    try (Connection conn = ds.getConnection()) {
      CompletionStage<BigDecimal> idF = conn.<BigDecimal>rowOperation("select $1::numeric as t")
          .set("$1", d, PGAdbaType.NUMERIC)
          .collect(singleCollector(BigDecimal.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectFloat() throws ExecutionException, InterruptedException {
    Float d = (float) 100.155;

    try (Connection conn = ds.getConnection()) {
      CompletionStage<Float> idF = conn.<Float>rowOperation("select $1::real as t")
          .set("$1", d, PGAdbaType.REAL)
          .collect(singleCollector(Float.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(), 0.0001);
    }
  }

  @Test
  public void selectDouble() throws ExecutionException, InterruptedException {
    Double d = 100.155666;

    try (Connection conn = ds.getConnection()) {
      CompletionStage<Double> idF = conn.<Double>rowOperation("select $1::double precision as t")
          .set("$1", d, PGAdbaType.DOUBLE)
          .collect(singleCollector(Double.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(), 0.0000001);
    }
  }

  @Test
  public void selectBoolean() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Boolean> idF = conn.<Boolean>rowOperation("select $1::boolean as t")
          .set("$1", true, PGAdbaType.BOOLEAN)
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertTrue(idF.toCompletableFuture().get());
    }
  }

  @Test
  public void selectNull() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", null, PGAdbaType.INTEGER)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertNull(idF.toCompletableFuture().get());
    }
  }
}
