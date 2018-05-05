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
import java.util.concurrent.CompletableFuture;
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
  public void bindNullAsInteger4() throws ExecutionException, InterruptedException {
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
  public void bindFutureNullAsInteger4() throws ExecutionException, InterruptedException {
    CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> null);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", f, PGAdbaType.INTEGER)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertNull(idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindNullAsInteger4NoType() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", null)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertNull(idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindFutureNullAsInteger4NoType() throws ExecutionException, InterruptedException {
    CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> null);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", f)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertNull(idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindInteger4() throws ExecutionException, InterruptedException {
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
  public void bindFutureInteger4() throws ExecutionException, InterruptedException {
    CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> 100);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", f, PGAdbaType.INTEGER)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindInteger4NoType() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", 100)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindFutureInteger4NoType() throws ExecutionException, InterruptedException {
    CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> 100);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", f)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindInteger2() throws ExecutionException, InterruptedException {
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
  public void bindFutureInteger2() throws ExecutionException, InterruptedException {
    CompletableFuture<Short> f = CompletableFuture.supplyAsync(() -> (short)100);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Short> idF = conn.<Short>rowOperation("select $1::int2 as t")
          .set("$1", f, PGAdbaType.SMALLINT)
          .collect(singleCollector(Short.class))
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short) 100), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindInteger2NoType() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Short> idF = conn.<Short>rowOperation("select $1::int2 as t")
          .set("$1", 100)
          .collect(singleCollector(Short.class))
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short) 100), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindFutureInteger2NoType() throws ExecutionException, InterruptedException {
    CompletableFuture<Short> f = CompletableFuture.supplyAsync(() -> (short)100);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Short> idF = conn.<Short>rowOperation("select $1::int2 as t")
          .set("$1", f)
          .collect(singleCollector(Short.class))
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short) 100), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindInteger8() throws ExecutionException, InterruptedException {
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
  public void bindFutureInteger8() throws ExecutionException, InterruptedException {
    CompletableFuture<Long> f = CompletableFuture.supplyAsync(() -> 100L);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Long> idF = conn.<Long>rowOperation("select $1::int8 as t")
          .set("$1", f, PGAdbaType.BIGINT)
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindInteger8NoType() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Long> idF = conn.<Long>rowOperation("select $1::int8 as t")
          .set("$1", 100)
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindFutureInteger8NoType() throws ExecutionException, InterruptedException {
    CompletableFuture<Long> f = CompletableFuture.supplyAsync(() -> 100L);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Long> idF = conn.<Long>rowOperation("select $1::int8 as t")
          .set("$1", f)
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindVarchar() throws ExecutionException, InterruptedException {
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
  public void bindFutureVarchar() throws ExecutionException, InterruptedException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "a text I wrote");
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::varchar as t")
          .set("$1", f, PGAdbaType.VARCHAR)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("a text I wrote", idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindVarcharNoType() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::varchar as t")
          .set("$1", "a text I wrote")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("a text I wrote", idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindFutureVarcharNoType() throws ExecutionException, InterruptedException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "a text I wrote");
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::varchar as t")
          .set("$1", f)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("a text I wrote", idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindVarCharNorwegianChar() throws ExecutionException, InterruptedException {
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
  public void bindFutureVarCharNorwegianChar() throws ExecutionException, InterruptedException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "Brød har lenge vore ein viktig del av norsk kosthald.");
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::varchar as t")
          .set("$1", f, PGAdbaType.VARCHAR)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Brød har lenge vore ein viktig del av norsk kosthald.", idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindVarCharNorwegianCharNoType() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::varchar as t")
          .set("$1", "Brød har lenge vore ein viktig del av norsk kosthald.")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Brød har lenge vore ein viktig del av norsk kosthald.", idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindFutureVarCharNorwegianCharNoType() throws ExecutionException, InterruptedException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "Brød har lenge vore ein viktig del av norsk kosthald.");
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::varchar as t")
          .set("$1", f)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Brød har lenge vore ein viktig del av norsk kosthald.", idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindChar() throws ExecutionException, InterruptedException {
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
  public void bindFutureChar() throws ExecutionException, InterruptedException {
    CompletableFuture<Character> f = CompletableFuture.supplyAsync(() -> 'R');
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select $1::char as t")
          .set("$1", f, PGAdbaType.CHAR)
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('R'), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindCharNoType() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select $1::char as t")
          .set("$1", 'R')
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('R'), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindFutureCharNoType() throws ExecutionException, InterruptedException {
    CompletableFuture<Character> f = CompletableFuture.supplyAsync(() -> 'R');
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select $1::char as t")
          .set("$1", f)
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('R'), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindCharNorwegianChar() throws ExecutionException, InterruptedException {
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
  public void bindFutureCharNorwegianChar() throws ExecutionException, InterruptedException {
    CompletableFuture<Character> f = CompletableFuture.supplyAsync(() -> 'Ø');
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select $1::char as t")
          .set("$1", f, PGAdbaType.CHAR)
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('Ø'), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindCharNorwegianCharNoType() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select $1::char as t")
          .set("$1", 'Ø')
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('Ø'), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindFutureCharNorwegianCharNoType() throws ExecutionException, InterruptedException {
    CompletableFuture<Character> f = CompletableFuture.supplyAsync(() -> 'Ø');
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select $1::char as t")
          .set("$1", f)
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('Ø'), idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindLongVarCharNorwegianChar() throws ExecutionException, InterruptedException {
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
  public void bindFutureLongVarCharNorwegianChar() throws ExecutionException, InterruptedException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "Som regel i form av smørbrød til frukost og lunsj.");
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::text as t")
          .set("$1", f, PGAdbaType.LONGVARCHAR)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Som regel i form av smørbrød til frukost og lunsj.", idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindLongVarCharNorwegianCharNoType() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::text as t")
          .set("$1", "Som regel i form av smørbrød til frukost og lunsj.")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Som regel i form av smørbrød til frukost og lunsj.", idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindFutureLongVarCharNorwegianCharNoType() throws ExecutionException, InterruptedException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "Som regel i form av smørbrød til frukost og lunsj.");
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::text as t")
          .set("$1", f)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Som regel i form av smørbrød til frukost og lunsj.", idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindDate() throws ExecutionException, InterruptedException {
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
  public void bindFutureDate() throws ExecutionException, InterruptedException {
    LocalDate d = LocalDate.now();
    CompletableFuture<LocalDate> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDate> idF = conn.<LocalDate>rowOperation("select $1::date as t")
          .set("$1", f, PGAdbaType.DATE)
          .collect(singleCollector(LocalDate.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindDateNoType() throws ExecutionException, InterruptedException {
    LocalDate d = LocalDate.now();

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDate> idF = conn.<LocalDate>rowOperation("select $1::date as t")
          .set("$1", d)
          .collect(singleCollector(LocalDate.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindFutureDateNoType() throws ExecutionException, InterruptedException {
    LocalDate d = LocalDate.now();
    CompletableFuture<LocalDate> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDate> idF = conn.<LocalDate>rowOperation("select $1::date as t")
          .set("$1", f)
          .collect(singleCollector(LocalDate.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindTime() throws ExecutionException, InterruptedException {
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
  public void bindFutureTime() throws ExecutionException, InterruptedException {
    LocalTime d = LocalTime.now();
    CompletableFuture<LocalTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalTime> idF = conn.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", f, PGAdbaType.TIME)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindTimeNoType() throws ExecutionException, InterruptedException {
    LocalTime d = LocalTime.now();

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalTime> idF = conn.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", d)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindFutureTimeNoType() throws ExecutionException, InterruptedException {
    LocalTime d = LocalTime.now();
    CompletableFuture<LocalTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalTime> idF = conn.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", f)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindTimeWithTimeZone() throws ExecutionException, InterruptedException {
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
  public void bindFutureTimeWithTimeZone() throws ExecutionException, InterruptedException {
    OffsetTime d = OffsetTime.now(ZoneOffset.UTC);
    CompletableFuture<OffsetTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetTime> idF = conn.<OffsetTime>rowOperation("select $1::time with time zone as t")
          .set("$1", f, PGAdbaType.TIME_WITH_TIME_ZONE)
          .collect(singleCollector(OffsetTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindTimeWithTimeZoneNoType() throws ExecutionException, InterruptedException {
    OffsetTime d = OffsetTime.now(ZoneOffset.UTC);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetTime> idF = conn.<OffsetTime>rowOperation("select $1::time with time zone as t")
          .set("$1", d)
          .collect(singleCollector(OffsetTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindFutureTimeWithTimeZoneNoType() throws ExecutionException, InterruptedException {
    OffsetTime d = OffsetTime.now(ZoneOffset.UTC);
    CompletableFuture<OffsetTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetTime> idF = conn.<OffsetTime>rowOperation("select $1::time with time zone as t")
          .set("$1", f)
          .collect(singleCollector(OffsetTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindTimestamp() throws ExecutionException, InterruptedException {
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
  public void bindFutureTimestamp() throws ExecutionException, InterruptedException {
    LocalDateTime d = LocalDateTime.now();
    CompletableFuture<LocalDateTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDateTime> idF = conn.<LocalDateTime>rowOperation("select $1::timestamp as t")
          .set("$1", f, PGAdbaType.TIMESTAMP)
          .collect(singleCollector(LocalDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindTimestampNoType() throws ExecutionException, InterruptedException {
    LocalDateTime d = LocalDateTime.now();

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDateTime> idF = conn.<LocalDateTime>rowOperation("select $1::timestamp as t")
          .set("$1", d)
          .collect(singleCollector(LocalDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindFutureTimestampNoType() throws ExecutionException, InterruptedException {
    LocalDateTime d = LocalDateTime.now();
    CompletableFuture<LocalDateTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDateTime> idF = conn.<LocalDateTime>rowOperation("select $1::timestamp as t")
          .set("$1", f)
          .collect(singleCollector(LocalDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindTimestampWithTimeZone() throws ExecutionException, InterruptedException {
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
  public void bindFutureTimestampWithTimeZone() throws ExecutionException, InterruptedException {
    OffsetDateTime d = OffsetDateTime.now(ZoneOffset.UTC);
    CompletableFuture<OffsetDateTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetDateTime> idF = conn.<OffsetDateTime>rowOperation("select $1::timestamp with time zone as t")
          .set("$1", f, PGAdbaType.TIMESTAMP_WITH_TIME_ZONE)
          .collect(singleCollector(OffsetDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindTimestampWithTimeZoneNoType() throws ExecutionException, InterruptedException {
    OffsetDateTime d = OffsetDateTime.now(ZoneOffset.UTC);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetDateTime> idF = conn.<OffsetDateTime>rowOperation("select $1::timestamp with time zone as t")
          .set("$1", d)
          .collect(singleCollector(OffsetDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindFutureTimestampWithTimeZoneNoType() throws ExecutionException, InterruptedException {
    OffsetDateTime d = OffsetDateTime.now(ZoneOffset.UTC);
    CompletableFuture<OffsetDateTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetDateTime> idF = conn.<OffsetDateTime>rowOperation("select $1::timestamp with time zone as t")
          .set("$1", f)
          .collect(singleCollector(OffsetDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindNumerical() throws ExecutionException, InterruptedException {
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
  public void bindFutureNumerical() throws ExecutionException, InterruptedException {
    BigDecimal d = BigDecimal.TEN;
    CompletableFuture<BigDecimal> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<BigDecimal> idF = conn.<BigDecimal>rowOperation("select $1::numeric as t")
          .set("$1", f, PGAdbaType.NUMERIC)
          .collect(singleCollector(BigDecimal.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindNumericalNoType() throws ExecutionException, InterruptedException {
    BigDecimal d = BigDecimal.TEN;

    try (Connection conn = ds.getConnection()) {
      CompletionStage<BigDecimal> idF = conn.<BigDecimal>rowOperation("select $1::numeric as t")
          .set("$1", d)
          .collect(singleCollector(BigDecimal.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindFutureNumericalNoType() throws ExecutionException, InterruptedException {
    BigDecimal d = BigDecimal.TEN;
    CompletableFuture<BigDecimal> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<BigDecimal> idF = conn.<BigDecimal>rowOperation("select $1::numeric as t")
          .set("$1", f)
          .collect(singleCollector(BigDecimal.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindFloat() throws ExecutionException, InterruptedException {
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
  public void bindFutureFloat() throws ExecutionException, InterruptedException {
    Float d = (float) 100.155;
    CompletableFuture<Float> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<Float> idF = conn.<Float>rowOperation("select $1::real as t")
          .set("$1", f, PGAdbaType.REAL)
          .collect(singleCollector(Float.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(), 0.0001);
    }
  }

  @Test
  public void bindFloatNoType() throws ExecutionException, InterruptedException {
    Float d = (float) 100.155;

    try (Connection conn = ds.getConnection()) {
      CompletionStage<Float> idF = conn.<Float>rowOperation("select $1::real as t")
          .set("$1", d)
          .collect(singleCollector(Float.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(), 0.0001);
    }
  }

  @Test
  public void bindFutureFloatNoType() throws ExecutionException, InterruptedException {
    Float d = (float) 100.155;
    CompletableFuture<Float> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<Float> idF = conn.<Float>rowOperation("select $1::real as t")
          .set("$1", f)
          .collect(singleCollector(Float.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(), 0.0001);
    }
  }

  @Test
  public void bindDouble() throws ExecutionException, InterruptedException {
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
  public void bindFutureDouble() throws ExecutionException, InterruptedException {
    Double d = 100.155666;
    CompletableFuture<Double> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<Double> idF = conn.<Double>rowOperation("select $1::double precision as t")
          .set("$1", f, PGAdbaType.DOUBLE)
          .collect(singleCollector(Double.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(), 0.0000001);
    }
  }

  @Test
  public void bindDoubleNoType() throws ExecutionException, InterruptedException {
    Double d = 100.155666;

    try (Connection conn = ds.getConnection()) {
      CompletionStage<Double> idF = conn.<Double>rowOperation("select $1::double precision as t")
          .set("$1", d)
          .collect(singleCollector(Double.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(), 0.0000001);
    }
  }

  @Test
  public void bindFutureDoubleNoType() throws ExecutionException, InterruptedException {
    Double d = 100.155666;
    CompletableFuture<Double> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<Double> idF = conn.<Double>rowOperation("select $1::double precision as t")
          .set("$1", f)
          .collect(singleCollector(Double.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(), 0.0000001);
    }
  }

  @Test
  public void bindBoolean() throws ExecutionException, InterruptedException {
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
  public void bindFutureBoolean() throws ExecutionException, InterruptedException {
    CompletableFuture<Boolean> f = CompletableFuture.supplyAsync(() -> true);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Boolean> idF = conn.<Boolean>rowOperation("select $1::boolean as t")
          .set("$1", f, PGAdbaType.BOOLEAN)
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertTrue(idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindBooleanNoType() throws ExecutionException, InterruptedException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Boolean> idF = conn.<Boolean>rowOperation("select $1::boolean as t")
          .set("$1", true)
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertTrue(idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindFutureBooleanNoType() throws ExecutionException, InterruptedException {
    CompletableFuture<Boolean> f = CompletableFuture.supplyAsync(() -> true);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Boolean> idF = conn.<Boolean>rowOperation("select $1::boolean as t")
          .set("$1", f)
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertTrue(idF.toCompletableFuture().get());
    }
  }

  @Test
  public void bindTimeMultipleUsages() throws ExecutionException, InterruptedException {
    LocalTime d = LocalTime.now();
    CompletableFuture<LocalTime> f = CompletableFuture.supplyAsync(() -> d);
    CompletableFuture<LocalTime> f2 = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalTime> idF = conn.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", d, PGAdbaType.TIME)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get());

      CompletionStage<LocalTime> idF1 = conn.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", f, PGAdbaType.TIME)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF1.toCompletableFuture().get());

      CompletionStage<LocalTime> idF2 = conn.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", d)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF2.toCompletableFuture().get());

      CompletionStage<LocalTime> idF3 = conn.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", f2)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF3.toCompletableFuture().get());
    }
  }


}
