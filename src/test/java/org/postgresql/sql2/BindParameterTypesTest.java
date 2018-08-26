package org.postgresql.sql2;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.sql2.communication.packets.parts.PgAdbaType;
import org.postgresql.sql2.testutil.ConnectUtil;
import org.postgresql.sql2.testutil.DatabaseHolder;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.postgresql.sql2.testutil.CollectorUtils.singleCollector;

public class BindParameterTypesTest {
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
  public void bindNullAsInteger4() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", null, PgAdbaType.INTEGER)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertNull(idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureNullAsInteger4() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> null);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", f, PgAdbaType.INTEGER)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertNull(idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindNullAsInteger4NoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", null)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertNull(idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureNullAsInteger4NoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> null);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", f)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertNull(idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindInteger4() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", 100, PgAdbaType.INTEGER)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureInteger4() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> 100);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", f, PgAdbaType.INTEGER)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindInteger4NoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", 100)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureInteger4NoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> 100);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", f)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindInteger2() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Short> idF = conn.<Short>rowOperation("select $1::int2 as t")
          .set("$1", 100, PgAdbaType.SMALLINT)
          .collect(singleCollector(Short.class))
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short) 100), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureInteger2() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Short> f = CompletableFuture.supplyAsync(() -> (short)100);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Short> idF = conn.<Short>rowOperation("select $1::int2 as t")
          .set("$1", f, PgAdbaType.SMALLINT)
          .collect(singleCollector(Short.class))
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short) 100), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindInteger2NoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Short> idF = conn.<Short>rowOperation("select $1::int2 as t")
          .set("$1", 100)
          .collect(singleCollector(Short.class))
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short) 100), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureInteger2NoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Short> f = CompletableFuture.supplyAsync(() -> (short)100);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Short> idF = conn.<Short>rowOperation("select $1::int2 as t")
          .set("$1", f)
          .collect(singleCollector(Short.class))
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short) 100), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindInteger8() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Long> idF = conn.<Long>rowOperation("select $1::int8 as t")
          .set("$1", 100, PgAdbaType.BIGINT)
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureInteger8() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Long> f = CompletableFuture.supplyAsync(() -> 100L);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Long> idF = conn.<Long>rowOperation("select $1::int8 as t")
          .set("$1", f, PgAdbaType.BIGINT)
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindInteger8NoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Long> idF = conn.<Long>rowOperation("select $1::int8 as t")
          .set("$1", 100)
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureInteger8NoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Long> f = CompletableFuture.supplyAsync(() -> 100L);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Long> idF = conn.<Long>rowOperation("select $1::int8 as t")
          .set("$1", f)
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindVarchar() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::varchar as t")
          .set("$1", "a text I wrote", PgAdbaType.VARCHAR)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("a text I wrote", idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureVarchar() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "a text I wrote");
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::varchar as t")
          .set("$1", f, PgAdbaType.VARCHAR)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("a text I wrote", idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindVarcharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::varchar as t")
          .set("$1", "a text I wrote")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("a text I wrote", idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureVarcharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "a text I wrote");
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::varchar as t")
          .set("$1", f)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("a text I wrote", idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindVarCharNorwegianChar() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::varchar as t")
          .set("$1", "Brød har lenge vore ein viktig del av norsk kosthald.", PgAdbaType.VARCHAR)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Brød har lenge vore ein viktig del av norsk kosthald.", idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureVarCharNorwegianChar() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "Brød har lenge vore ein viktig del av norsk kosthald.");
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::varchar as t")
          .set("$1", f, PgAdbaType.VARCHAR)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Brød har lenge vore ein viktig del av norsk kosthald.", idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindVarCharNorwegianCharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::varchar as t")
          .set("$1", "Brød har lenge vore ein viktig del av norsk kosthald.")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Brød har lenge vore ein viktig del av norsk kosthald.", idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureVarCharNorwegianCharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "Brød har lenge vore ein viktig del av norsk kosthald.");
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::varchar as t")
          .set("$1", f)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Brød har lenge vore ein viktig del av norsk kosthald.", idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindChar() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select $1::char as t")
          .set("$1", 'R', PgAdbaType.CHAR)
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('R'), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureChar() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Character> f = CompletableFuture.supplyAsync(() -> 'R');
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select $1::char as t")
          .set("$1", f, PgAdbaType.CHAR)
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('R'), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindCharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select $1::char as t")
          .set("$1", 'R')
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('R'), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureCharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Character> f = CompletableFuture.supplyAsync(() -> 'R');
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select $1::char as t")
          .set("$1", f)
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('R'), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindCharNorwegianChar() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select $1::char as t")
          .set("$1", 'Ø', PgAdbaType.CHAR)
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('Ø'), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureCharNorwegianChar() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Character> f = CompletableFuture.supplyAsync(() -> 'Ø');
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select $1::char as t")
          .set("$1", f, PgAdbaType.CHAR)
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('Ø'), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindCharNorwegianCharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select $1::char as t")
          .set("$1", 'Ø')
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('Ø'), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureCharNorwegianCharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Character> f = CompletableFuture.supplyAsync(() -> 'Ø');
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select $1::char as t")
          .set("$1", f)
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('Ø'), idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindLongVarCharNorwegianChar() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::text as t")
          .set("$1", "Som regel i form av smørbrød til frukost og lunsj.", PgAdbaType.LONGVARCHAR)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Som regel i form av smørbrød til frukost og lunsj.", idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureLongVarCharNorwegianChar() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "Som regel i form av smørbrød til frukost og lunsj.");
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::text as t")
          .set("$1", f, PgAdbaType.LONGVARCHAR)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Som regel i form av smørbrød til frukost og lunsj.", idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindLongVarCharNorwegianCharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::text as t")
          .set("$1", "Som regel i form av smørbrød til frukost og lunsj.")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Som regel i form av smørbrød til frukost og lunsj.", idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureLongVarCharNorwegianCharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "Som regel i form av smørbrød til frukost og lunsj.");
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select $1::text as t")
          .set("$1", f)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Som regel i form av smørbrød til frukost og lunsj.", idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindDate() throws ExecutionException, InterruptedException, TimeoutException {
    LocalDate d = LocalDate.of(2018, 3, 11);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDate> idF = conn.<LocalDate>rowOperation("select $1::date as t")
          .set("$1", d, PgAdbaType.DATE)
          .collect(singleCollector(LocalDate.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureDate() throws ExecutionException, InterruptedException, TimeoutException {
    LocalDate d = LocalDate.of(2018, 3, 12);
    CompletableFuture<LocalDate> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDate> idF = conn.<LocalDate>rowOperation("select $1::date as t")
          .set("$1", f, PgAdbaType.DATE)
          .collect(singleCollector(LocalDate.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindDateNoType() throws ExecutionException, InterruptedException, TimeoutException {
    LocalDate d = LocalDate.of(2018, 2, 11);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDate> idF = conn.<LocalDate>rowOperation("select $1::date as t")
          .set("$1", d)
          .collect(singleCollector(LocalDate.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureDateNoType() throws ExecutionException, InterruptedException, TimeoutException {
    LocalDate d = LocalDate.of(2018, 5, 9);
    CompletableFuture<LocalDate> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDate> idF = conn.<LocalDate>rowOperation("select $1::date as t")
          .set("$1", f)
          .collect(singleCollector(LocalDate.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindTime() throws ExecutionException, InterruptedException, TimeoutException {
    LocalTime d = LocalTime.of(14, 55, 32, 123456000);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalTime> idF = conn.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", d, PgAdbaType.TIME)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureTime() throws ExecutionException, InterruptedException, TimeoutException {
    LocalTime d = LocalTime.of(11, 55, 13, 123456000);
    CompletableFuture<LocalTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalTime> idF = conn.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", f, PgAdbaType.TIME)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindTimeNoType() throws ExecutionException, InterruptedException, TimeoutException {
    LocalTime d = LocalTime.of(19, 12, 40, 654321000);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalTime> idF = conn.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", d)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureTimeNoType() throws ExecutionException, InterruptedException, TimeoutException {
    LocalTime d = LocalTime.of(3, 20, 22, 2000);
    CompletableFuture<LocalTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalTime> idF = conn.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", f)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindTimeWithTimeZone() throws ExecutionException, InterruptedException, TimeoutException {
    OffsetTime d = OffsetTime.of(13, 33, 11, 34000, ZoneOffset.UTC);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetTime> idF = conn.<OffsetTime>rowOperation("select $1::time with time zone as t")
          .set("$1", d, PgAdbaType.TIME_WITH_TIME_ZONE)
          .collect(singleCollector(OffsetTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureTimeWithTimeZone() throws ExecutionException, InterruptedException, TimeoutException {
    OffsetTime d = OffsetTime.of(7, 26, 11, 987000, ZoneOffset.UTC);
    CompletableFuture<OffsetTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetTime> idF = conn.<OffsetTime>rowOperation("select $1::time with time zone as t")
          .set("$1", f, PgAdbaType.TIME_WITH_TIME_ZONE)
          .collect(singleCollector(OffsetTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindTimeWithTimeZoneNoType() throws ExecutionException, InterruptedException, TimeoutException {
    OffsetTime d = OffsetTime.of(3, 10, 1, 220000, ZoneOffset.UTC);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetTime> idF = conn.<OffsetTime>rowOperation("select $1::time with time zone as t")
          .set("$1", d)
          .collect(singleCollector(OffsetTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureTimeWithTimeZoneNoType() throws ExecutionException, InterruptedException, TimeoutException {
    OffsetTime d = OffsetTime.of(12, 22, 33, 440000, ZoneOffset.UTC);
    CompletableFuture<OffsetTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetTime> idF = conn.<OffsetTime>rowOperation("select $1::time with time zone as t")
          .set("$1", f)
          .collect(singleCollector(OffsetTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindTimestamp() throws ExecutionException, InterruptedException, TimeoutException {
    LocalDateTime d = LocalDateTime.of(2018, 1, 2, 3, 4, 5, 6000);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDateTime> idF = conn.<LocalDateTime>rowOperation("select $1::timestamp as t")
          .set("$1", d, PgAdbaType.TIMESTAMP)
          .collect(singleCollector(LocalDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureTimestamp() throws ExecutionException, InterruptedException, TimeoutException {
    LocalDateTime d = LocalDateTime.of(2018, 3, 22, 13, 55, 2, 88000);
    CompletableFuture<LocalDateTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDateTime> idF = conn.<LocalDateTime>rowOperation("select $1::timestamp as t")
          .set("$1", f, PgAdbaType.TIMESTAMP)
          .collect(singleCollector(LocalDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindTimestampNoType() throws ExecutionException, InterruptedException, TimeoutException {
    LocalDateTime d = LocalDateTime.of(2018, 6, 6, 4, 45, 0, 722000);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDateTime> idF = conn.<LocalDateTime>rowOperation("select $1::timestamp as t")
          .set("$1", d)
          .collect(singleCollector(LocalDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureTimestampNoType() throws ExecutionException, InterruptedException, TimeoutException {
    LocalDateTime d = LocalDateTime.of(2018, 12, 10, 5, 4, 3, 2000);
    CompletableFuture<LocalDateTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDateTime> idF = conn.<LocalDateTime>rowOperation("select $1::timestamp as t")
          .set("$1", f)
          .collect(singleCollector(LocalDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindTimestampWithTimeZone() throws ExecutionException, InterruptedException, TimeoutException {
    OffsetDateTime d = OffsetDateTime.of(2018, 3, 9, 12, 22, 33, 45000, ZoneOffset.UTC);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetDateTime> idF = conn.<OffsetDateTime>rowOperation("select $1::timestamp with time zone as t")
          .set("$1", d, PgAdbaType.TIMESTAMP_WITH_TIME_ZONE)
          .collect(singleCollector(OffsetDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureTimestampWithTimeZone() throws ExecutionException, InterruptedException, TimeoutException {
    OffsetDateTime d = OffsetDateTime.of(2018, 1, 1, 13, 44, 38, 411000, ZoneOffset.UTC);
    CompletableFuture<OffsetDateTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetDateTime> idF = conn.<OffsetDateTime>rowOperation("select $1::timestamp with time zone as t")
          .set("$1", f, PgAdbaType.TIMESTAMP_WITH_TIME_ZONE)
          .collect(singleCollector(OffsetDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindTimestampWithTimeZoneNoType() throws ExecutionException, InterruptedException, TimeoutException {
    OffsetDateTime d = OffsetDateTime.of(2018, 7, 3, 22, 43, 22, 67000, ZoneOffset.UTC);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetDateTime> idF = conn.<OffsetDateTime>rowOperation("select $1::timestamp with time zone as t")
          .set("$1", d)
          .collect(singleCollector(OffsetDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureTimestampWithTimeZoneNoType() throws ExecutionException, InterruptedException, TimeoutException {
    OffsetDateTime d = OffsetDateTime.of(2018, 11, 2, 20, 20, 20, 321000, ZoneOffset.UTC);
    CompletableFuture<OffsetDateTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetDateTime> idF = conn.<OffsetDateTime>rowOperation("select $1::timestamp with time zone as t")
          .set("$1", f)
          .collect(singleCollector(OffsetDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindNumerical() throws ExecutionException, InterruptedException, TimeoutException {
    BigDecimal d = BigDecimal.TEN;

    try (Connection conn = ds.getConnection()) {
      CompletionStage<BigDecimal> idF = conn.<BigDecimal>rowOperation("select $1::numeric as t")
          .set("$1", d, PgAdbaType.NUMERIC)
          .collect(singleCollector(BigDecimal.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureNumerical() throws ExecutionException, InterruptedException, TimeoutException {
    BigDecimal d = BigDecimal.TEN;
    CompletableFuture<BigDecimal> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<BigDecimal> idF = conn.<BigDecimal>rowOperation("select $1::numeric as t")
          .set("$1", f, PgAdbaType.NUMERIC)
          .collect(singleCollector(BigDecimal.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindNumericalNoType() throws ExecutionException, InterruptedException, TimeoutException {
    BigDecimal d = BigDecimal.TEN;

    try (Connection conn = ds.getConnection()) {
      CompletionStage<BigDecimal> idF = conn.<BigDecimal>rowOperation("select $1::numeric as t")
          .set("$1", d)
          .collect(singleCollector(BigDecimal.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureNumericalNoType() throws ExecutionException, InterruptedException, TimeoutException {
    BigDecimal d = BigDecimal.TEN;
    CompletableFuture<BigDecimal> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<BigDecimal> idF = conn.<BigDecimal>rowOperation("select $1::numeric as t")
          .set("$1", f)
          .collect(singleCollector(BigDecimal.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFloat() throws ExecutionException, InterruptedException, TimeoutException {
    Float d = (float) 100.155;

    try (Connection conn = ds.getConnection()) {
      CompletionStage<Float> idF = conn.<Float>rowOperation("select $1::real as t")
          .set("$1", d, PgAdbaType.REAL)
          .collect(singleCollector(Float.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS), 0.0001);
    }
  }

  @Test
  public void bindFutureFloat() throws ExecutionException, InterruptedException, TimeoutException {
    Float d = (float) 100.155;
    CompletableFuture<Float> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<Float> idF = conn.<Float>rowOperation("select $1::real as t")
          .set("$1", f, PgAdbaType.REAL)
          .collect(singleCollector(Float.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS), 0.0001);
    }
  }

  @Test
  public void bindFloatNoType() throws ExecutionException, InterruptedException, TimeoutException {
    Float d = (float) 100.155;

    try (Connection conn = ds.getConnection()) {
      CompletionStage<Float> idF = conn.<Float>rowOperation("select $1::real as t")
          .set("$1", d)
          .collect(singleCollector(Float.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS), 0.0001);
    }
  }

  @Test
  public void bindFutureFloatNoType() throws ExecutionException, InterruptedException, TimeoutException {
    Float d = (float) 100.155;
    CompletableFuture<Float> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<Float> idF = conn.<Float>rowOperation("select $1::real as t")
          .set("$1", f)
          .collect(singleCollector(Float.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS), 0.0001);
    }
  }

  @Test
  public void bindDouble() throws ExecutionException, InterruptedException, TimeoutException {
    Double d = 100.155666;

    try (Connection conn = ds.getConnection()) {
      CompletionStage<Double> idF = conn.<Double>rowOperation("select $1::double precision as t")
          .set("$1", d, PgAdbaType.DOUBLE)
          .collect(singleCollector(Double.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS), 0.0000001);
    }
  }

  @Test
  public void bindFutureDouble() throws ExecutionException, InterruptedException, TimeoutException {
    Double d = 100.155666;
    CompletableFuture<Double> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<Double> idF = conn.<Double>rowOperation("select $1::double precision as t")
          .set("$1", f, PgAdbaType.DOUBLE)
          .collect(singleCollector(Double.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS), 0.0000001);
    }
  }

  @Test
  public void bindDoubleNoType() throws ExecutionException, InterruptedException, TimeoutException {
    Double d = 100.155666;

    try (Connection conn = ds.getConnection()) {
      CompletionStage<Double> idF = conn.<Double>rowOperation("select $1::double precision as t")
          .set("$1", d)
          .collect(singleCollector(Double.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS), 0.0000001);
    }
  }

  @Test
  public void bindFutureDoubleNoType() throws ExecutionException, InterruptedException, TimeoutException {
    Double d = 100.155666;
    CompletableFuture<Double> f = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<Double> idF = conn.<Double>rowOperation("select $1::double precision as t")
          .set("$1", f)
          .collect(singleCollector(Double.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS), 0.0000001);
    }
  }

  @Test
  public void bindBoolean() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Boolean> idF = conn.<Boolean>rowOperation("select $1::boolean as t")
          .set("$1", true, PgAdbaType.BOOLEAN)
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertTrue(idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureBoolean() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Boolean> f = CompletableFuture.supplyAsync(() -> true);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Boolean> idF = conn.<Boolean>rowOperation("select $1::boolean as t")
          .set("$1", f, PgAdbaType.BOOLEAN)
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertTrue(idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindBooleanNoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Boolean> idF = conn.<Boolean>rowOperation("select $1::boolean as t")
          .set("$1", true)
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertTrue(idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindFutureBooleanNoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Boolean> f = CompletableFuture.supplyAsync(() -> true);
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Boolean> idF = conn.<Boolean>rowOperation("select $1::boolean as t")
          .set("$1", f)
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertTrue(idF.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void bindTimeMultipleUsages() throws ExecutionException, InterruptedException, TimeoutException {
    LocalTime d = LocalTime.of(17, 30, 54, 45000);
    CompletableFuture<LocalTime> f = CompletableFuture.supplyAsync(() -> d);
    CompletableFuture<LocalTime> f2 = CompletableFuture.supplyAsync(() -> d);

    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalTime> idF = conn.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", d, PgAdbaType.TIME)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF.toCompletableFuture().get(10, TimeUnit.SECONDS));

      CompletionStage<LocalTime> idF1 = conn.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", f, PgAdbaType.TIME)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF1.toCompletableFuture().get(10, TimeUnit.SECONDS));

      CompletionStage<LocalTime> idF2 = conn.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", d)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF2.toCompletableFuture().get(10, TimeUnit.SECONDS));

      CompletionStage<LocalTime> idF3 = conn.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", f2)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, idF3.toCompletableFuture().get(10, TimeUnit.SECONDS));
    }
  }


}
