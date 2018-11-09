package org.postgresql.adba;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.postgresql.adba.testutil.CollectorUtils.singleCollector;
import static org.postgresql.adba.testutil.FutureUtil.get10;

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
import java.util.concurrent.TimeoutException;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.adba.communication.packets.parts.PgAdbaType;
import org.postgresql.adba.testutil.ConnectUtil;
import org.postgresql.adba.testutil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

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
  public void bindInteger4IllegalParameterName() {
    try (Session session = ds.getSession()) {
      assertThrows(IllegalArgumentException.class, () -> session.<Integer>rowOperation("select $1::int4 as t")
          .set("1", null, PgAdbaType.INTEGER));
    }
  }

  @Test
  public void bindNullAsInteger4() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Integer> idF = session.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", null, PgAdbaType.INTEGER)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertNull(get10(idF.toCompletableFuture()));
    }
  }

  @Test
  public void bindFutureNullAsInteger4() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> null);
    try (Session session = ds.getSession()) {
      CompletionStage<Integer> idF = session.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", f, PgAdbaType.INTEGER)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertNull(get10(idF.toCompletableFuture()));
    }
  }

  @Test
  public void bindNullAsInteger4NoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Integer> idF = session.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", null)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertNull(get10(idF));
    }
  }

  @Test
  public void bindFutureNullAsInteger4NoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> null);
    try (Session session = ds.getSession()) {
      CompletionStage<Integer> idF = session.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", f)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertNull(get10(idF));
    }
  }

  @Test
  public void bindInteger4() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Integer> idF = session.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", 100, PgAdbaType.INTEGER)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), get10(idF));
    }
  }

  @Test
  public void bindFutureInteger4() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> 100);
    try (Session session = ds.getSession()) {
      CompletionStage<Integer> idF = session.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", f, PgAdbaType.INTEGER)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), get10(idF));
    }
  }

  @Test
  public void bindInteger4NoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Integer> idF = session.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", 100)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), get10(idF));
    }
  }

  @Test
  public void bindFutureInteger4NoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> 100);
    try (Session session = ds.getSession()) {
      CompletionStage<Integer> idF = session.<Integer>rowOperation("select $1::int4 as t")
          .set("$1", f)
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), get10(idF));
    }
  }

  @Test
  public void bindInteger2() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Short> idF = session.<Short>rowOperation("select $1::int2 as t")
          .set("$1", 100, PgAdbaType.SMALLINT)
          .collect(singleCollector(Short.class))
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short) 100), get10(idF));
    }
  }

  @Test
  public void bindFutureInteger2() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Short> f = CompletableFuture.supplyAsync(() -> (short)100);
    try (Session session = ds.getSession()) {
      CompletionStage<Short> idF = session.<Short>rowOperation("select $1::int2 as t")
          .set("$1", f, PgAdbaType.SMALLINT)
          .collect(singleCollector(Short.class))
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short) 100), get10(idF));
    }
  }

  @Test
  public void bindInteger2NoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Short> idF = session.<Short>rowOperation("select $1::int2 as t")
          .set("$1", 100)
          .collect(singleCollector(Short.class))
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short) 100), get10(idF));
    }
  }

  @Test
  public void bindFutureInteger2NoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Short> f = CompletableFuture.supplyAsync(() -> (short)100);
    try (Session session = ds.getSession()) {
      CompletionStage<Short> idF = session.<Short>rowOperation("select $1::int2 as t")
          .set("$1", f)
          .collect(singleCollector(Short.class))
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short) 100), get10(idF));
    }
  }

  @Test
  public void bindInteger8() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Long> idF = session.<Long>rowOperation("select $1::int8 as t")
          .set("$1", 100, PgAdbaType.BIGINT)
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), get10(idF));
    }
  }

  @Test
  public void bindFutureInteger8() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Long> f = CompletableFuture.supplyAsync(() -> 100L);
    try (Session session = ds.getSession()) {
      CompletionStage<Long> idF = session.<Long>rowOperation("select $1::int8 as t")
          .set("$1", f, PgAdbaType.BIGINT)
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), get10(idF));
    }
  }

  @Test
  public void bindInteger8NoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Long> idF = session.<Long>rowOperation("select $1::int8 as t")
          .set("$1", 100)
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), get10(idF));
    }
  }

  @Test
  public void bindFutureInteger8NoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Long> f = CompletableFuture.supplyAsync(() -> 100L);
    try (Session session = ds.getSession()) {
      CompletionStage<Long> idF = session.<Long>rowOperation("select $1::int8 as t")
          .set("$1", f)
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), get10(idF));
    }
  }

  @Test
  public void bindVarchar() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("select $1::varchar as t")
          .set("$1", "a text I wrote", PgAdbaType.VARCHAR)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("a text I wrote", get10(idF));
    }
  }

  @Test
  public void bindFutureVarchar() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "a text I wrote");
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("select $1::varchar as t")
          .set("$1", f, PgAdbaType.VARCHAR)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("a text I wrote", get10(idF));
    }
  }

  @Test
  public void bindVarcharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("select $1::varchar as t")
          .set("$1", "a text I wrote")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("a text I wrote", get10(idF));
    }
  }

  @Test
  public void bindFutureVarcharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "a text I wrote");
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("select $1::varchar as t")
          .set("$1", f)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("a text I wrote", get10(idF));
    }
  }

  @Test
  public void bindVarCharNorwegianChar() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("select $1::varchar as t")
          .set("$1", "Brød har lenge vore ein viktig del av norsk kosthald.", PgAdbaType.VARCHAR)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Brød har lenge vore ein viktig del av norsk kosthald.", get10(idF));
    }
  }

  @Test
  public void bindFutureVarCharNorwegianChar() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "Brød har lenge vore ein viktig del av norsk kosthald.");
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("select $1::varchar as t")
          .set("$1", f, PgAdbaType.VARCHAR)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Brød har lenge vore ein viktig del av norsk kosthald.", get10(idF));
    }
  }

  @Test
  public void bindVarCharNorwegianCharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("select $1::varchar as t")
          .set("$1", "Brød har lenge vore ein viktig del av norsk kosthald.")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Brød har lenge vore ein viktig del av norsk kosthald.", get10(idF));
    }
  }

  @Test
  public void bindFutureVarCharNorwegianCharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "Brød har lenge vore ein viktig del av norsk kosthald.");
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("select $1::varchar as t")
          .set("$1", f)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Brød har lenge vore ein viktig del av norsk kosthald.", get10(idF));
    }
  }

  @Test
  public void bindChar() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Character> idF = session.<Character>rowOperation("select $1::char as t")
          .set("$1", 'R', PgAdbaType.CHAR)
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('R'), get10(idF));
    }
  }

  @Test
  public void bindFutureChar() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Character> f = CompletableFuture.supplyAsync(() -> 'R');
    try (Session session = ds.getSession()) {
      CompletionStage<Character> idF = session.<Character>rowOperation("select $1::char as t")
          .set("$1", f, PgAdbaType.CHAR)
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('R'), get10(idF));
    }
  }

  @Test
  public void bindCharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Character> idF = session.<Character>rowOperation("select $1::char as t")
          .set("$1", 'R')
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('R'), get10(idF));
    }
  }

  @Test
  public void bindFutureCharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Character> f = CompletableFuture.supplyAsync(() -> 'R');
    try (Session session = ds.getSession()) {
      CompletionStage<Character> idF = session.<Character>rowOperation("select $1::char as t")
          .set("$1", f)
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('R'), get10(idF));
    }
  }

  @Test
  public void bindCharNorwegianChar() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Character> idF = session.<Character>rowOperation("select $1::char as t")
          .set("$1", 'Ø', PgAdbaType.CHAR)
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('Ø'), get10(idF));
    }
  }

  @Test
  public void bindFutureCharNorwegianChar() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Character> f = CompletableFuture.supplyAsync(() -> 'Ø');
    try (Session session = ds.getSession()) {
      CompletionStage<Character> idF = session.<Character>rowOperation("select $1::char as t")
          .set("$1", f, PgAdbaType.CHAR)
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('Ø'), get10(idF));
    }
  }

  @Test
  public void bindCharNorwegianCharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Character> idF = session.<Character>rowOperation("select $1::char as t")
          .set("$1", 'Ø')
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('Ø'), get10(idF));
    }
  }

  @Test
  public void bindFutureCharNorwegianCharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Character> f = CompletableFuture.supplyAsync(() -> 'Ø');
    try (Session session = ds.getSession()) {
      CompletionStage<Character> idF = session.<Character>rowOperation("select $1::char as t")
          .set("$1", f)
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('Ø'), get10(idF));
    }
  }

  @Test
  public void bindLongVarCharNorwegianChar() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("select $1::text as t")
          .set("$1", "Som regel i form av smørbrød til frukost og lunsj.", PgAdbaType.LONGVARCHAR)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Som regel i form av smørbrød til frukost og lunsj.", get10(idF));
    }
  }

  @Test
  public void bindFutureLongVarCharNorwegianChar() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "Som regel i form av smørbrød til frukost og lunsj.");
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("select $1::text as t")
          .set("$1", f, PgAdbaType.LONGVARCHAR)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Som regel i form av smørbrød til frukost og lunsj.", get10(idF));
    }
  }

  @Test
  public void bindLongVarCharNorwegianCharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("select $1::text as t")
          .set("$1", "Som regel i form av smørbrød til frukost og lunsj.")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Som regel i form av smørbrød til frukost og lunsj.", get10(idF));
    }
  }

  @Test
  public void bindFutureLongVarCharNorwegianCharNoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "Som regel i form av smørbrød til frukost og lunsj.");
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("select $1::text as t")
          .set("$1", f)
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Som regel i form av smørbrød til frukost og lunsj.", get10(idF));
    }
  }

  @Test
  public void bindDate() throws ExecutionException, InterruptedException, TimeoutException {
    LocalDate d = LocalDate.of(2018, 3, 11);

    try (Session session = ds.getSession()) {
      CompletionStage<LocalDate> idF = session.<LocalDate>rowOperation("select $1::date as t")
          .set("$1", d, PgAdbaType.DATE)
          .collect(singleCollector(LocalDate.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindFutureDate() throws ExecutionException, InterruptedException, TimeoutException {
    LocalDate d = LocalDate.of(2018, 3, 12);
    CompletableFuture<LocalDate> f = CompletableFuture.supplyAsync(() -> d);

    try (Session session = ds.getSession()) {
      CompletionStage<LocalDate> idF = session.<LocalDate>rowOperation("select $1::date as t")
          .set("$1", f, PgAdbaType.DATE)
          .collect(singleCollector(LocalDate.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindDateNoType() throws ExecutionException, InterruptedException, TimeoutException {
    LocalDate d = LocalDate.of(2018, 2, 11);

    try (Session session = ds.getSession()) {
      CompletionStage<LocalDate> idF = session.<LocalDate>rowOperation("select $1::date as t")
          .set("$1", d)
          .collect(singleCollector(LocalDate.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindFutureDateNoType() throws ExecutionException, InterruptedException, TimeoutException {
    LocalDate d = LocalDate.of(2018, 5, 9);
    CompletableFuture<LocalDate> f = CompletableFuture.supplyAsync(() -> d);

    try (Session session = ds.getSession()) {
      CompletionStage<LocalDate> idF = session.<LocalDate>rowOperation("select $1::date as t")
          .set("$1", f)
          .collect(singleCollector(LocalDate.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindTime() throws ExecutionException, InterruptedException, TimeoutException {
    LocalTime d = LocalTime.of(14, 55, 32, 123456000);

    try (Session session = ds.getSession()) {
      CompletionStage<LocalTime> idF = session.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", d, PgAdbaType.TIME)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindFutureTime() throws ExecutionException, InterruptedException, TimeoutException {
    LocalTime d = LocalTime.of(11, 55, 13, 123456000);
    CompletableFuture<LocalTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Session session = ds.getSession()) {
      CompletionStage<LocalTime> idF = session.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", f, PgAdbaType.TIME)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindTimeNoType() throws ExecutionException, InterruptedException, TimeoutException {
    LocalTime d = LocalTime.of(19, 12, 40, 654321000);

    try (Session session = ds.getSession()) {
      CompletionStage<LocalTime> idF = session.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", d)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindFutureTimeNoType() throws ExecutionException, InterruptedException, TimeoutException {
    LocalTime d = LocalTime.of(3, 20, 22, 2000);
    CompletableFuture<LocalTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Session session = ds.getSession()) {
      CompletionStage<LocalTime> idF = session.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", f)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindTimeWithTimeZone() throws ExecutionException, InterruptedException, TimeoutException {
    OffsetTime d = OffsetTime.of(13, 33, 11, 34000, ZoneOffset.UTC);

    try (Session session = ds.getSession()) {
      CompletionStage<OffsetTime> idF = session.<OffsetTime>rowOperation("select $1::time with time zone as t")
          .set("$1", d, PgAdbaType.TIME_WITH_TIME_ZONE)
          .collect(singleCollector(OffsetTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindFutureTimeWithTimeZone() throws ExecutionException, InterruptedException, TimeoutException {
    OffsetTime d = OffsetTime.of(7, 26, 11, 987000, ZoneOffset.UTC);
    CompletableFuture<OffsetTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Session session = ds.getSession()) {
      CompletionStage<OffsetTime> idF = session.<OffsetTime>rowOperation("select $1::time with time zone as t")
          .set("$1", f, PgAdbaType.TIME_WITH_TIME_ZONE)
          .collect(singleCollector(OffsetTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindTimeWithTimeZoneNoType() throws ExecutionException, InterruptedException, TimeoutException {
    OffsetTime d = OffsetTime.of(3, 10, 1, 220000, ZoneOffset.UTC);

    try (Session session = ds.getSession()) {
      CompletionStage<OffsetTime> idF = session.<OffsetTime>rowOperation("select $1::time with time zone as t")
          .set("$1", d)
          .collect(singleCollector(OffsetTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindFutureTimeWithTimeZoneNoType() throws ExecutionException, InterruptedException, TimeoutException {
    OffsetTime d = OffsetTime.of(12, 22, 33, 440000, ZoneOffset.UTC);
    CompletableFuture<OffsetTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Session session = ds.getSession()) {
      CompletionStage<OffsetTime> idF = session.<OffsetTime>rowOperation("select $1::time with time zone as t")
          .set("$1", f)
          .collect(singleCollector(OffsetTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindTimestamp() throws ExecutionException, InterruptedException, TimeoutException {
    LocalDateTime d = LocalDateTime.of(2018, 1, 2, 3, 4, 5, 6000);

    try (Session session = ds.getSession()) {
      CompletionStage<LocalDateTime> idF = session.<LocalDateTime>rowOperation("select $1::timestamp as t")
          .set("$1", d, PgAdbaType.TIMESTAMP)
          .collect(singleCollector(LocalDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindFutureTimestamp() throws ExecutionException, InterruptedException, TimeoutException {
    LocalDateTime d = LocalDateTime.of(2018, 3, 22, 13, 55, 2, 88000);
    CompletableFuture<LocalDateTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Session session = ds.getSession()) {
      CompletionStage<LocalDateTime> idF = session.<LocalDateTime>rowOperation("select $1::timestamp as t")
          .set("$1", f, PgAdbaType.TIMESTAMP)
          .collect(singleCollector(LocalDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindTimestampNoType() throws ExecutionException, InterruptedException, TimeoutException {
    LocalDateTime d = LocalDateTime.of(2018, 6, 6, 4, 45, 0, 722000);

    try (Session session = ds.getSession()) {
      CompletionStage<LocalDateTime> idF = session.<LocalDateTime>rowOperation("select $1::timestamp as t")
          .set("$1", d)
          .collect(singleCollector(LocalDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindFutureTimestampNoType() throws ExecutionException, InterruptedException, TimeoutException {
    LocalDateTime d = LocalDateTime.of(2018, 12, 10, 5, 4, 3, 2000);
    CompletableFuture<LocalDateTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Session session = ds.getSession()) {
      CompletionStage<LocalDateTime> idF = session.<LocalDateTime>rowOperation("select $1::timestamp as t")
          .set("$1", f)
          .collect(singleCollector(LocalDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindTimestampWithTimeZone() throws ExecutionException, InterruptedException, TimeoutException {
    OffsetDateTime d = OffsetDateTime.of(2018, 3, 9, 12, 22, 33, 45000, ZoneOffset.UTC);

    try (Session session = ds.getSession()) {
      CompletionStage<OffsetDateTime> idF = session.<OffsetDateTime>rowOperation("select $1::timestamp with time zone as t")
          .set("$1", d, PgAdbaType.TIMESTAMP_WITH_TIME_ZONE)
          .collect(singleCollector(OffsetDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindFutureTimestampWithTimeZone() throws ExecutionException, InterruptedException, TimeoutException {
    OffsetDateTime d = OffsetDateTime.of(2018, 1, 1, 13, 44, 38, 411000, ZoneOffset.UTC);
    CompletableFuture<OffsetDateTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Session session = ds.getSession()) {
      CompletionStage<OffsetDateTime> idF = session.<OffsetDateTime>rowOperation("select $1::timestamp with time zone as t")
          .set("$1", f, PgAdbaType.TIMESTAMP_WITH_TIME_ZONE)
          .collect(singleCollector(OffsetDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindTimestampWithTimeZoneNoType() throws ExecutionException, InterruptedException, TimeoutException {
    OffsetDateTime d = OffsetDateTime.of(2018, 7, 3, 22, 43, 22, 67000, ZoneOffset.UTC);

    try (Session session = ds.getSession()) {
      CompletionStage<OffsetDateTime> idF = session.<OffsetDateTime>rowOperation("select $1::timestamp with time zone as t")
          .set("$1", d)
          .collect(singleCollector(OffsetDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindFutureTimestampWithTimeZoneNoType() throws ExecutionException, InterruptedException, TimeoutException {
    OffsetDateTime d = OffsetDateTime.of(2018, 11, 2, 20, 20, 20, 321000, ZoneOffset.UTC);
    CompletableFuture<OffsetDateTime> f = CompletableFuture.supplyAsync(() -> d);

    try (Session session = ds.getSession()) {
      CompletionStage<OffsetDateTime> idF = session.<OffsetDateTime>rowOperation("select $1::timestamp with time zone as t")
          .set("$1", f)
          .collect(singleCollector(OffsetDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindNumerical() throws ExecutionException, InterruptedException, TimeoutException {
    BigDecimal d = BigDecimal.TEN;

    try (Session session = ds.getSession()) {
      CompletionStage<BigDecimal> idF = session.<BigDecimal>rowOperation("select $1::numeric as t")
          .set("$1", d, PgAdbaType.NUMERIC)
          .collect(singleCollector(BigDecimal.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindFutureNumerical() throws ExecutionException, InterruptedException, TimeoutException {
    BigDecimal d = BigDecimal.TEN;
    CompletableFuture<BigDecimal> f = CompletableFuture.supplyAsync(() -> d);

    try (Session session = ds.getSession()) {
      CompletionStage<BigDecimal> idF = session.<BigDecimal>rowOperation("select $1::numeric as t")
          .set("$1", f, PgAdbaType.NUMERIC)
          .collect(singleCollector(BigDecimal.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindNumericalNoType() throws ExecutionException, InterruptedException, TimeoutException {
    BigDecimal d = BigDecimal.TEN;

    try (Session session = ds.getSession()) {
      CompletionStage<BigDecimal> idF = session.<BigDecimal>rowOperation("select $1::numeric as t")
          .set("$1", d)
          .collect(singleCollector(BigDecimal.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindFutureNumericalNoType() throws ExecutionException, InterruptedException, TimeoutException {
    BigDecimal d = BigDecimal.TEN;
    CompletableFuture<BigDecimal> f = CompletableFuture.supplyAsync(() -> d);

    try (Session session = ds.getSession()) {
      CompletionStage<BigDecimal> idF = session.<BigDecimal>rowOperation("select $1::numeric as t")
          .set("$1", f)
          .collect(singleCollector(BigDecimal.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));
    }
  }

  @Test
  public void bindFloat() throws ExecutionException, InterruptedException, TimeoutException {
    Float d = (float) 100.155;

    try (Session session = ds.getSession()) {
      CompletionStage<Float> idF = session.<Float>rowOperation("select $1::real as t")
          .set("$1", d, PgAdbaType.REAL)
          .collect(singleCollector(Float.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF), 0.0001);
    }
  }

  @Test
  public void bindFutureFloat() throws ExecutionException, InterruptedException, TimeoutException {
    Float d = (float) 100.155;
    CompletableFuture<Float> f = CompletableFuture.supplyAsync(() -> d);

    try (Session session = ds.getSession()) {
      CompletionStage<Float> idF = session.<Float>rowOperation("select $1::real as t")
          .set("$1", f, PgAdbaType.REAL)
          .collect(singleCollector(Float.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF), 0.0001);
    }
  }

  @Test
  public void bindFloatNoType() throws ExecutionException, InterruptedException, TimeoutException {
    Float d = (float) 100.155;

    try (Session session = ds.getSession()) {
      CompletionStage<Float> idF = session.<Float>rowOperation("select $1::real as t")
          .set("$1", d)
          .collect(singleCollector(Float.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF), 0.0001);
    }
  }

  @Test
  public void bindFutureFloatNoType() throws ExecutionException, InterruptedException, TimeoutException {
    Float d = (float) 100.155;
    CompletableFuture<Float> f = CompletableFuture.supplyAsync(() -> d);

    try (Session session = ds.getSession()) {
      CompletionStage<Float> idF = session.<Float>rowOperation("select $1::real as t")
          .set("$1", f)
          .collect(singleCollector(Float.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF), 0.0001);
    }
  }

  @Test
  public void bindDouble() throws ExecutionException, InterruptedException, TimeoutException {
    Double d = 100.155666;

    try (Session session = ds.getSession()) {
      CompletionStage<Double> idF = session.<Double>rowOperation("select $1::double precision as t")
          .set("$1", d, PgAdbaType.DOUBLE)
          .collect(singleCollector(Double.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF), 0.0000001);
    }
  }

  @Test
  public void bindFutureDouble() throws ExecutionException, InterruptedException, TimeoutException {
    Double d = 100.155666;
    CompletableFuture<Double> f = CompletableFuture.supplyAsync(() -> d);

    try (Session session = ds.getSession()) {
      CompletionStage<Double> idF = session.<Double>rowOperation("select $1::double precision as t")
          .set("$1", f, PgAdbaType.DOUBLE)
          .collect(singleCollector(Double.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF), 0.0000001);
    }
  }

  @Test
  public void bindDoubleNoType() throws ExecutionException, InterruptedException, TimeoutException {
    Double d = 100.155666;

    try (Session session = ds.getSession()) {
      CompletionStage<Double> idF = session.<Double>rowOperation("select $1::double precision as t")
          .set("$1", d)
          .collect(singleCollector(Double.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF), 0.0000001);
    }
  }

  @Test
  public void bindFutureDoubleNoType() throws ExecutionException, InterruptedException, TimeoutException {
    Double d = 100.155666;
    CompletableFuture<Double> f = CompletableFuture.supplyAsync(() -> d);

    try (Session session = ds.getSession()) {
      CompletionStage<Double> idF = session.<Double>rowOperation("select $1::double precision as t")
          .set("$1", f)
          .collect(singleCollector(Double.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF), 0.0000001);
    }
  }

  @Test
  public void bindBoolean() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Boolean> idF = session.<Boolean>rowOperation("select $1::boolean as t")
          .set("$1", true, PgAdbaType.BOOLEAN)
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertTrue(get10(idF));
    }
  }

  @Test
  public void bindFutureBoolean() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Boolean> f = CompletableFuture.supplyAsync(() -> true);
    try (Session session = ds.getSession()) {
      CompletionStage<Boolean> idF = session.<Boolean>rowOperation("select $1::boolean as t")
          .set("$1", f, PgAdbaType.BOOLEAN)
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertTrue(get10(idF));
    }
  }

  @Test
  public void bindBooleanNoType() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Boolean> idF = session.<Boolean>rowOperation("select $1::boolean as t")
          .set("$1", true)
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertTrue(get10(idF));
    }
  }

  @Test
  public void bindFutureBooleanNoType() throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Boolean> f = CompletableFuture.supplyAsync(() -> true);
    try (Session session = ds.getSession()) {
      CompletionStage<Boolean> idF = session.<Boolean>rowOperation("select $1::boolean as t")
          .set("$1", f)
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertTrue(get10(idF));
    }
  }

  @Test
  public void bindTimeMultipleUsages() throws ExecutionException, InterruptedException, TimeoutException {
    LocalTime d = LocalTime.of(17, 30, 54, 45000);
    CompletableFuture<LocalTime> f = CompletableFuture.supplyAsync(() -> d);
    CompletableFuture<LocalTime> f2 = CompletableFuture.supplyAsync(() -> d);

    try (Session session = ds.getSession()) {
      CompletionStage<LocalTime> idF = session.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", d, PgAdbaType.TIME)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF));

      CompletionStage<LocalTime> idF1 = session.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", f, PgAdbaType.TIME)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF1));

      CompletionStage<LocalTime> idF2 = session.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", d)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF2));

      CompletionStage<LocalTime> idF3 = session.<LocalTime>rowOperation("select $1::time as t")
          .set("$1", f2)
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(d, get10(idF3));
    }
  }


}
