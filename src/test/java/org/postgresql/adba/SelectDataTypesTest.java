package org.postgresql.adba;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.postgresql.adba.testutil.CollectorUtils.singleCollector;
import static org.postgresql.adba.testutil.FutureUtil.get10;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;
import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Session;
import jdk.incubator.sql2.Submission;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.adba.communication.packets.parts.PgAdbaType;
import org.postgresql.adba.pgdatatypes.LongRange;
import org.postgresql.adba.testutil.CollectorUtils;
import org.postgresql.adba.testutil.ConnectUtil;
import org.postgresql.adba.testutil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

public class SelectDataTypesTest {
  public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

  private static DataSource ds;

  @BeforeAll
  public static void setUp() {
    ds = ConnectUtil.openDb(postgres);

    ConnectUtil.createTable(ds, "tab",
        "id int", "name varchar(100)", "answer int");
  }

  @AfterAll
  public static void tearDown() {
    ds.close();
  }

  @Test
  public void selectInteger4() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Integer> idF = session.<Integer>rowOperation("select 100 as t")
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), get10(idF));
    }
  }

  @Test
  public void selectIntegerExample() throws ExecutionException, InterruptedException, TimeoutException {
    List<Integer> result = new ArrayList<>();
    try (Session session = ds.getSession()) {
      Submission<List<Integer>> sub = session.<List<Integer>>rowOperation("select $1 as id")
          .set("$1", 1, AdbaType.INTEGER)
          .collect(() -> result,
              (list, row) -> list.add(row.at("id").get(Integer.class)))
          .submit();
      get10(sub.getCompletionStage());
    }
    assertEquals(1, result.size());
    assertEquals(Integer.valueOf(1), result.get(0));
  }

  @Test
  public void selectInteger4asInteger8() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Long> idF = session.<Long>rowOperation("select 100 as t")
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), get10(idF));
    }
  }

  @Test
  public void selectInteger4AsShort() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Short> idF = session.<Short>rowOperation("select 100 as t")
          .collect(singleCollector(Short.class))
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short)100), get10(idF));
    }
  }

  @Test
  public void selectInteger2() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Short> idF = session.<Short>rowOperation("select 100::int2 as t")
          .collect(singleCollector(Short.class))
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short) 100), get10(idF));
    }
  }

  @Test
  public void selectInteger2AsLong() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Long> idF = session.<Long>rowOperation("select 100::int2 as t")
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), get10(idF));
    }
  }

  @Test
  public void selectInteger2AsInteger() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Integer> idF = session.<Integer>rowOperation("select 100::int2 as t")
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), get10(idF));
    }
  }

  @Test
  public void selectInteger8() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Long> idF = session.<Long>rowOperation("select 100::int8 as t")
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), get10(idF));
    }
  }

  @Test
  public void selectInteger8AsInteger() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Integer> idF = session.<Integer>rowOperation("select 100::int8 as t")
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), get10(idF));
    }
  }

  @Test
  public void selectInteger8AsIntegerTooLarge() {
    try (Session session = ds.getSession()) {
      CompletionStage<Integer> idF = session.<Integer>rowOperation("select " + Long.MAX_VALUE + "::int8 as t")
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertThrows(ExecutionException.class, () -> get10(idF));
    }
  }

  @Test
  public void selectInteger8AsShort() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Short> idF = session.<Short>rowOperation("select 100::int8 as t")
          .collect(singleCollector(Short.class))
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short)100), get10(idF));
    }
  }

  @Test
  public void selectNumeric() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<BigDecimal> idF = session.<BigDecimal>rowOperation("select 100.505::numeric as t")
          .collect(singleCollector(BigDecimal.class))
          .submit()
          .getCompletionStage();

      assertEquals(BigDecimal.valueOf(100.505), get10(idF));
    }
  }

  @Test
  public void selectReal() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Float> idF = session.<Float>rowOperation("select 100.505::real as t")
          .collect(singleCollector(Float.class))
          .submit()
          .getCompletionStage();

      assertEquals((float) 100.505, get10(idF), 0.5);
    }
  }

  @Test
  public void selectDouble() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Double> idF = session.<Double>rowOperation("select 100.505::double precision as t")
          .collect(singleCollector(Double.class))
          .submit()
          .getCompletionStage();

      assertEquals((float) 100.505, get10(idF), 0.5);
    }
  }

  @Test
  public void selectMoney() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("select 100.505::money as t")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("$100.51", get10(idF));
    }
  }

  @Test
  public void selectVarchar() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("select 'Sphinx of black quartz, judge my vow'::varchar as t")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Sphinx of black quartz, judge my vow", get10(idF));
    }
  }

  @Test
  public void selectCharacter() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Character> idF = session.<Character>rowOperation("select 'H'::character as t")
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('H'), get10(idF));
    }
  }

  @Test
  public void selectText() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("select 'How vexingly quick daft zebras jump!'::text as t")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("How vexingly quick daft zebras jump!", get10(idF));
    }
  }

  @Test
  public void selectBytea() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<byte[]> idF = session.<byte[]>rowOperation("select 'DEADBEEF'::bytea as t")
          .collect(singleCollector(byte[].class))
          .submit()
          .getCompletionStage();

      byte[] expected = new byte[]{0x44, 0x45, 0x41, 0x44, 0x42, 0x45, 0x45, 0x46};
      assertArrayEquals(expected, get10(idF));
    }
  }

  @Test
  public void selectTimestampWithoutTimeZone() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<LocalDateTime> idF = session
          .<LocalDateTime>rowOperation("select '2018-04-29 20:55:57.692132'::timestamp without time zone as t")
          .collect(singleCollector(LocalDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(LocalDateTime.of(2018, 4, 29, 20, 55, 57, 692132000), get10(idF));
    }
  }

  @Test
  public void selectTimestampWithTimeZone() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<OffsetDateTime> idF = session
          .<OffsetDateTime>rowOperation("select '2018-04-29 20:55:57.692132'::timestamp with time zone as t")
          .collect(singleCollector(OffsetDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(OffsetDateTime.of(2018, 4, 29, 20, 55, 57, 692132000, ZoneOffset.UTC), get10(idF));
    }
  }

  @Test
  public void selectDate() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<LocalDate> idF = session.<LocalDate>rowOperation("select '2018-04-29 20:55:57.692132'::date as t")
          .collect(singleCollector(LocalDate.class))
          .submit()
          .getCompletionStage();

      assertEquals(LocalDate.of(2018, 4, 29), get10(idF));
    }
  }

  @Test
  public void selectDateNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<LocalDate> idF = session.<LocalDate>rowOperation("select null::date as t")
          .collect(singleCollector(LocalDate.class))
          .submit()
          .getCompletionStage();

      assertNull(get10(idF));
    }
  }

  @Test
  public void selectDateAsTime() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<LocalTime> idF = session.<LocalTime>rowOperation("select '2018-04-29 20:55:57.692132'::timestamp as t")
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(LocalTime.of(20, 55, 57, 692132000), get10(idF));
    }
  }

  @Test
  public void selectTime() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<LocalTime> idF = session.<LocalTime>rowOperation("select '2018-04-29 20:55:57.692132'::time as t")
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(LocalTime.of(20, 55, 57, 692132000), get10(idF));
    }
  }

  @Test
  public void selectTimeWithTimeZone() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<OffsetTime> idF = session
          .<OffsetTime>rowOperation("select '2018-04-29 20:55:57.692132'::time with time zone as t")
          .collect(singleCollector(OffsetTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(OffsetTime.of(20, 55, 57, 692132000, ZoneOffset.UTC), get10(idF));
    }
  }

  @Test
  public void selectInterval() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Duration> idF = session.<Duration>rowOperation("select INTERVAL '2 months 3 days' as t")
          .collect(singleCollector(Duration.class))
          .submit()
          .getCompletionStage();

      assertEquals(Duration.of(1512, ChronoUnit.HOURS), get10(idF));
    }
  }

  @Test
  public void selectEnum() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      session.rowCountOperation("CREATE TYPE happiness AS ENUM ('happy', 'very happy', 'ecstatic');").submit();
      CompletionStage<String[]> idF = session.<String[]>rowOperation("SELECT unnest(enum_range(NULL::happiness)) as t")
          .collect(Collector.of(
              () -> new String[3],
              (a, r) -> a[(int) r.rowNumber()] = r.at("t").get(String.class),
              (l, r) -> null,
              a -> a)
          )
          .submit()
          .getCompletionStage();

      assertArrayEquals(new String[] {"happy", "very happy", "ecstatic"}, get10(idF));
    }
  }

  @Test
  public void selectBoolean() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Boolean> idF = session.<Boolean>rowOperation("select true::bool as t")
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertTrue(get10(idF));
    }
  }

  @Test
  public void selectBooleanNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Boolean> idF = session.<Boolean>rowOperation("select null::bool as t")
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertNull(get10(idF));
    }
  }

  @Test
  public void selectUuid() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<UUID> idF = session.<UUID>rowOperation("select 'a81bc81b-dead-4e5d-abff-90865d1e13b1'::uuid as t")
          .collect(singleCollector(UUID.class))
          .submit()
          .getCompletionStage();

      assertEquals(UUID.fromString("a81bc81b-dead-4e5d-abff-90865d1e13b1"), get10(idF));
    }
  }

  @Test
  public void selectChar() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      // select from one of the systems tables, as ::char returns a bpchar (oid 1042) instead of a char (oid 18)
      CompletionStage<Character> idF = session.<Character>rowOperation("select aggkind as t from pg_aggregate limit 1")
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('n'), get10(idF));
    }
  }

  @Test
  public void selectInteger2Array() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Short[]> idF = session.<Short[]>rowOperation("select ARRAY[100::int2, 200::int2, 300::int2] as t")
          .collect(singleCollector(Short[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Short[] {100, 200, 300}, get10(idF));
    }
  }

  @Test
  public void selectInteger2ArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Short[]> idF = session.<Short[]>rowOperation("select ARRAY[]::int2[] as t")
          .collect(singleCollector(Short[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Short[] {}, get10(idF));
    }
  }

  @Test
  public void selectInteger2ArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Short[]> idF = session.<Short[]>rowOperation("select ARRAY[null::int2] as t")
          .collect(singleCollector(Short[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Short[] {null}, get10(idF));
    }
  }

  @Test
  public void selectInteger4ArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Integer[]> idF = session.<Integer[]>rowOperation("select ARRAY[null::int4] as t")
          .collect(singleCollector(Integer[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Integer[] {null}, get10(idF));
    }
  }

  @Test
  public void selectInteger4ArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Integer[]> idF = session.<Integer[]>rowOperation("select ARRAY[]::int4[] as t")
          .collect(singleCollector(Integer[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Integer[] {}, get10(idF));
    }
  }

  @Test
  public void selectInteger4Array() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Integer[]> idF = session.<Integer[]>rowOperation("select ARRAY[100, 200, 300] as t")
          .collect(singleCollector(Integer[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Integer[] {100, 200, 300}, get10(idF));
    }
  }

  @Test
  public void selectInteger8Array() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Long[]> idF = session.<Long[]>rowOperation("select ARRAY[100::int8, 200::int8, 300::int8] as t")
          .collect(singleCollector(Long[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Long[] {100L, 200L, 300L}, get10(idF));
    }
  }

  @Test
  public void selectInteger8ArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Long[]> idF = session.<Long[]>rowOperation("select ARRAY[null::int8] as t")
          .collect(singleCollector(Long[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Long[] {null}, get10(idF));
    }
  }

  @Test
  public void selectInteger8ArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Long[]> idF = session.<Long[]>rowOperation("select ARRAY[]::int8[] as t")
          .collect(singleCollector(Long[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Long[] {}, get10(idF));
    }
  }

  @Test
  public void selectFloatArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Float[]> idF = session.<Float[]>rowOperation("select ARRAY[100::float4, 200::float4, 300::float4] as t")
          .collect(singleCollector(Float[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Float[] {100.0f, 200.0f, 300.0f}, get10(idF));
    }
  }

  @Test
  public void selectFloatArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Float[]> idF = session.<Float[]>rowOperation("select ARRAY[null::float4, 200::float4, 300::float4] as t")
          .collect(singleCollector(Float[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Float[] {null, 200.0f, 300.0f}, get10(idF));
    }
  }

  @Test
  public void selectFloatArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Float[]> idF = session.<Float[]>rowOperation("select ARRAY[]::float4[] as t")
          .collect(singleCollector(Float[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Float[] {}, get10(idF));
    }
  }

  @Test
  public void selectDoubleArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Double[]> idF = session.<Double[]>rowOperation("select ARRAY[100::float8, 200::float8, 300::float8] as t")
          .collect(singleCollector(Double[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Double[] {100.0, 200.0, 300.0}, get10(idF));
    }
  }

  @Test
  public void selectDoubleArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Double[]> idF = session.<Double[]>rowOperation("select ARRAY[]::float8[] as t")
          .collect(singleCollector(Double[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Double[] {}, get10(idF));
    }
  }

  @Test
  public void selectDoubleArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Double[]> idF = session.<Double[]>rowOperation("select ARRAY[null::float8] as t")
          .collect(singleCollector(Double[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Double[] {null}, get10(idF));
    }
  }

  @Test
  public void selectBooleanArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Boolean[]> idF = session.<Boolean[]>rowOperation("select ARRAY[true, false, true] as t")
          .collect(singleCollector(Boolean[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Boolean[] {true, false, true}, get10(idF));
    }
  }

  @Test
  public void selectBooleanArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Boolean[]> idF = session.<Boolean[]>rowOperation("select ARRAY[null::boolean] as t")
          .collect(singleCollector(Boolean[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Boolean[] {null}, get10(idF));
    }
  }

  @Test
  public void selectBooleanArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Boolean[]> idF = session.<Boolean[]>rowOperation("select ARRAY[]::boolean[] as t")
          .collect(singleCollector(Boolean[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Boolean[] {}, get10(idF));
    }
  }

  @Test
  public void selectStringArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<String[]> idF = session.<String[]>rowOperation("select ARRAY['first', '\"second', 'th,ird', 'NULL'] as t")
          .collect(singleCollector(String[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new String[] {"first", "\"second", "th,ird", "NULL"}, get10(idF));
    }
  }

  @Test
  public void selectStringArrayEmptyArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<String[]> idF = session.<String[]>rowOperation("select ARRAY[]::varchar[] as t")
          .collect(singleCollector(String[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new String[] {}, get10(idF));
    }
  }

  @Test
  public void selectStringArrayOnlyNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<String[]> idF = session.<String[]>rowOperation("select ARRAY[null]::varchar[] as t")
          .collect(singleCollector(String[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new String[] {null}, get10(idF));
    }
  }

  @Test
  public void selectDateArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<LocalDate[]> idF = session.<LocalDate[]>rowOperation("select ARRAY['2018-04-29 20:55:57.692132'::date, "
          + "'2018-05-30 20:55:57.692132'::date] as t")
          .collect(singleCollector(LocalDate[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new LocalDate[] {LocalDate.of(2018, 4, 29),
          LocalDate.of(2018, 5, 30)}, get10(idF));
    }
  }

  @Test
  public void selectDateArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<LocalDate[]> idF = session.<LocalDate[]>rowOperation("select ARRAY[null::date, "
          + "'2018-05-30 20:55:57.692132'::date] as t")
          .collect(singleCollector(LocalDate[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new LocalDate[] {null, LocalDate.of(2018, 5, 30)}, get10(idF));
    }
  }

  @Test
  public void selectDateArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<LocalDate[]> idF = session.<LocalDate[]>rowOperation("select ARRAY[]::date[] as t")
          .collect(singleCollector(LocalDate[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new LocalDate[] {}, get10(idF));
    }
  }

  @Test
  public void selectTimeArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<LocalTime[]> idF = session.<LocalTime[]>rowOperation("select ARRAY['2018-04-29 20:55:57.692132'::time, "
          + "'2018-04-29 08:15:00.223344'::time] as t")
          .collect(singleCollector(LocalTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new LocalTime[] {LocalTime.of(20, 55, 57, 692132000),
          LocalTime.of(8, 15, 0, 223344000)}, get10(idF));
    }
  }

  @Test
  public void selectTimeArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<LocalTime[]> idF = session.<LocalTime[]>rowOperation("select ARRAY[null, "
          + "'2018-04-29 08:15:00.223344'::time] as t")
          .collect(singleCollector(LocalTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new LocalTime[] {null,
          LocalTime.of(8, 15, 0, 223344000)}, get10(idF));
    }
  }

  @Test
  public void selectTimeArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<LocalTime[]> idF = session.<LocalTime[]>rowOperation("select ARRAY[]::time[] as t")
          .collect(singleCollector(LocalTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new LocalTime[] {}, get10(idF));
    }
  }

  @Test
  public void selectTimestampWithoutTimeZoneArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<LocalDateTime[]> idF = session
          .<LocalDateTime[]>rowOperation("select ARRAY['2018-04-29 20:55:57.692132'::timestamp without time zone] as t")
          .collect(singleCollector(LocalDateTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new LocalDateTime[] {LocalDateTime.of(2018, 4, 29, 20, 55, 57, 692132000)}, get10(idF));
    }
  }

  @Test
  public void selectTimestampWithoutTimeZoneArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<LocalDateTime[]> idF = session
          .<LocalDateTime[]>rowOperation("select ARRAY[null::timestamp without time zone] as t")
          .collect(singleCollector(LocalDateTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new LocalDateTime[] {null}, get10(idF));
    }
  }

  @Test
  public void selectTimestampWithoutTimeZoneArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<LocalDateTime[]> idF = session
          .<LocalDateTime[]>rowOperation("select ARRAY[]::timestamp without time zone[] as t")
          .collect(singleCollector(LocalDateTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new LocalDateTime[] {}, get10(idF));
    }
  }

  @Test
  public void selectTimestampWithTimeZoneArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<OffsetDateTime[]> idF = session
          .<OffsetDateTime[]>rowOperation("select ARRAY['2018-04-29 20:55:57.692132'::timestamp with time zone] as t")
          .collect(singleCollector(OffsetDateTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new OffsetDateTime[] {OffsetDateTime.of(2018, 4, 29, 20, 55, 57, 692132000, ZoneOffset.UTC)}, get10(idF));
    }
  }

  @Test
  public void selectTimestampWithTimeZoneArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<OffsetDateTime[]> idF = session
          .<OffsetDateTime[]>rowOperation("select ARRAY[null, '2018-04-29 20:55:57.692132'::timestamp with time zone] as t")
          .collect(singleCollector(OffsetDateTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new OffsetDateTime[] {null,
          OffsetDateTime.of(2018, 4, 29, 20, 55, 57, 692132000, ZoneOffset.UTC)}, get10(idF));
    }
  }

  @Test
  public void selectTimestampWithTimeZoneArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<OffsetDateTime[]> idF = session
          .<OffsetDateTime[]>rowOperation("select ARRAY[]::timestamp with time zone[] as t")
          .collect(singleCollector(OffsetDateTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new OffsetDateTime[] {}, get10(idF));
    }
  }

  @Test
  public void selectNumericArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<BigDecimal[]> idF = session
          .<BigDecimal[]>rowOperation("select ARRAY[100.505::numeric, 200.505::numeric] as t")
          .collect(singleCollector(BigDecimal[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new BigDecimal[] {BigDecimal.valueOf(100.505), BigDecimal.valueOf(200.505)}, get10(idF));
    }
  }

  @Test
  public void selectNumericArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<BigDecimal[]> idF = session.<BigDecimal[]>rowOperation("select ARRAY[null, 200.505::numeric] as t")
          .collect(singleCollector(BigDecimal[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new BigDecimal[] {null, BigDecimal.valueOf(200.505)}, get10(idF));
    }
  }

  @Test
  public void selectNumericArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<BigDecimal[]> idF = session.<BigDecimal[]>rowOperation("select ARRAY[]::numeric[] as t")
          .collect(singleCollector(BigDecimal[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new BigDecimal[] {}, get10(idF));
    }
  }

  @Test
  public void selectTimeWithTimeZoneArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<OffsetTime[]> idF = session
          .<OffsetTime[]>rowOperation("select ARRAY['2018-04-29 20:55:57.692132'::time with time zone] as t")
          .collect(singleCollector(OffsetTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new OffsetTime[] {OffsetTime.of(20, 55, 57, 692132000, ZoneOffset.UTC)}, get10(idF));
    }
  }

  @Test
  public void selectTimeWithTimeZoneArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<OffsetTime[]> idF = session
          .<OffsetTime[]>rowOperation("select ARRAY[null, '2018-04-29 20:55:57.692132'::time with time zone] as t")
          .collect(singleCollector(OffsetTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new OffsetTime[] {null, OffsetTime.of(20, 55, 57, 692132000, ZoneOffset.UTC)}, get10(idF));
    }
  }

  @Test
  public void selectTimeWithTimeZoneArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<OffsetTime[]> idF = session
          .<OffsetTime[]>rowOperation("select ARRAY[]::time with time zone[] as t")
          .collect(singleCollector(OffsetTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new OffsetTime[] {}, get10(idF));
    }
  }

  @Test
  public void selectUuidArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<UUID[]> idF = session
          .<UUID[]>rowOperation("select ARRAY['a81bc81b-dead-4e5d-abff-90865d1e13b1'::uuid] as t")
          .collect(singleCollector(UUID[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new UUID[] {UUID.fromString("a81bc81b-dead-4e5d-abff-90865d1e13b1")}, get10(idF));
    }
  }

  @Test
  public void selectUuidArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<UUID[]> idF = session.<UUID[]>rowOperation("select ARRAY[null, "
          + "'a81bc81b-dead-4e5d-abff-90865d1e13b1'::uuid] as t")
          .collect(singleCollector(UUID[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new UUID[] {null, UUID.fromString("a81bc81b-dead-4e5d-abff-90865d1e13b1")}, get10(idF));
    }
  }

  @Test
  public void selectUuidArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<UUID[]> idF = session.<UUID[]>rowOperation("select ARRAY[]::uuid[] as t")
          .collect(singleCollector(UUID[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new UUID[] {}, get10(idF));
    }
  }

  @Test
  public void selectCharacterArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Character[]> idF = session.<Character[]>rowOperation("select ARRAY['H'::character] as t")
          .collect(singleCollector(Character[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Character[] {'H'}, get10(idF));
    }
  }

  @Test
  public void selectBitOne() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Boolean> idF = session.<Boolean>rowOperation("select B'1' as t")
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertTrue(get10(idF));
    }
  }

  @Test
  public void selectBitZero() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Boolean> idF = session.<Boolean>rowOperation("select B'0' as t")
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertFalse(get10(idF));
    }
  }

  @Test
  public void selectBitMultiple() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<byte[]> idF = session.<byte[]>rowOperation("select B'10101' as t")
          .collect(singleCollector(byte[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new byte[] {21}, get10(idF));
    }
  }

  @Test
  public void selectBitMultipleLong() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<byte[]> idF = session.<byte[]>rowOperation("select B'100000000' as t")
          .collect(singleCollector(byte[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new byte[] {1, 0}, get10(idF));
    }
  }

  @Test
  public void selectBitNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Boolean> idF = session.<Boolean>rowOperation("select null::bit as t")
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertNull(get10(idF));
    }
  }

  @Test
  public void selectBitOneFixedSize() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Boolean> idF = session.<Boolean>rowOperation("select '1'::bit(1) as t")
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertTrue(get10(idF));
    }
  }

  @Test
  public void selectBitOneAsBytes() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<byte[]> idF = session.<byte[]>rowOperation("select '1'::bit(1) as t")
          .collect(singleCollector(byte[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new byte[] {1}, get10(idF));
    }
  }

  @Test
  public void selectBitVaryingOneFixedSize() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Boolean> idF = session.<Boolean>rowOperation("select '1'::bit varying(1) as t")
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertTrue(get10(idF));
    }
  }

  @Test
  public void insertAndSelectBit() throws Exception {
    try (Session session = ds.getSession()) {
      get10(session.rowCountOperation("create table insertAndSelectBit(t bit)")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into insertAndSelectBit(t) values($1)")
          .set("$1", true, PgAdbaType.BIT).submit().getCompletionStage());
      CompletionStage<Boolean> idF = session.<Boolean>rowOperation("select t from insertAndSelectBit")
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table insertAndSelectBit").submit().getCompletionStage());

      assertTrue(get10(idF));
    }
  }

  @Test
  public void insertAndSelectBitByteArray() throws Exception {
    try (Session session = ds.getSession()) {
      get10(session.rowCountOperation("create table insertAndSelectBit(t bit(16))")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into insertAndSelectBit(t) values($1)")
          .set("$1", new byte[] {21, 12}, PgAdbaType.BIT).submit().getCompletionStage());
      CompletionStage<byte[]> idF = session.<byte[]>rowOperation("select t from insertAndSelectBit")
          .collect(singleCollector(byte[].class))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table insertAndSelectBit").submit().getCompletionStage());

      byte[] result = get10(idF);
      assertArrayEquals(new byte[] {21, 12}, result);
    }
  }

  @Test
  public void insertAndSelectBitVaryingByteArray() throws Exception {
    try (Session session = ds.getSession()) {
      get10(session.rowCountOperation("create table insertAndSelectBitVarying(t bit varying)")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into insertAndSelectBitVarying(t) values($1)")
          .set("$1", new byte[] {21, 12}, PgAdbaType.BIT).submit().getCompletionStage());
      CompletionStage<byte[]> idF = session.<byte[]>rowOperation("select t from insertAndSelectBitVarying")
          .collect(singleCollector(byte[].class))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table insertAndSelectBitVarying").submit().getCompletionStage());

      byte[] result = get10(idF);
      assertArrayEquals(new byte[] {21, 12}, result);
    }
  }

  @Test
  public void insertAndSelectBitArrayByteArray() throws Exception {
    try (Session session = ds.getSession()) {
      get10(session.rowCountOperation("create table insertAndSelectBit(t bit varying[])")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into insertAndSelectBit(t) values($1)")
          .set("$1", new byte[][] {new byte[]{21}, new byte[]{12}}, PgAdbaType.BIT_ARRAY).submit().getCompletionStage());
      CompletionStage<byte[][]> idF = session.<byte[][]>rowOperation("select t from insertAndSelectBit")
          .collect(singleCollector(byte[][].class))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table insertAndSelectBit").submit().getCompletionStage());

      byte[][] result = get10(idF);
      assertArrayEquals(new byte[][] {new byte[]{21}, new byte[]{12}}, result);
    }
  }

  @Test
  public void selectCharacterArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Character[]> idF = session.<Character[]>rowOperation("select ARRAY[null, 'H'::character] as t")
          .collect(singleCollector(Character[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Character[] {null, 'H'}, get10(idF));
    }
  }

  @Test
  public void selectCharacterArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Character[]> idF = session.<Character[]>rowOperation("select ARRAY[]::character[] as t")
          .collect(singleCollector(Character[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Character[] {}, get10(idF));
    }
  }

  @Test
  public void selectJson() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("select '{\"a\":[1,2,3],\"b\":[4,5,6]}'::json as t")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("{\"a\":[1,2,3],\"b\":[4,5,6]}", get10(idF));
    }
  }

  @Test
  public void insertAndSelectJson() throws Exception {
    try (Session session = ds.getSession()) {
      get10(session.rowCountOperation("create table insertAndSelectBit(t json)")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into insertAndSelectBit(t) values($1)")
          .set("$1", "{\"a\":[1,2,3],\"b\":[4,5,6]}", PgAdbaType.JSON).submit().getCompletionStage());
      CompletionStage<String> idF = session.<String>rowOperation("select t from insertAndSelectBit")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table insertAndSelectBit").submit().getCompletionStage());

      String result = get10(idF);
      assertEquals("{\"a\":[1,2,3],\"b\":[4,5,6]}", result);
    }
  }

  @Test
  public void selectJsonArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<String[]> idF = session
          .<String[]>rowOperation("select ARRAY['{\"a\":[1,2,3],\"b\":[4,5,6]}','{\"a\":\"b\",\"c\":null}']::json[] as t")
          .collect(singleCollector(String[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new String[] {"{\"a\":[1,2,3],\"b\":[4,5,6]}", "{\"a\":\"b\",\"c\":null}"}, get10(idF));
    }
  }

  @Test
  public void insertAndSelectJsonArray() throws Exception {
    try (Session session = ds.getSession()) {
      get10(session.rowCountOperation("create table insertAndSelectBit(t json[])")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into insertAndSelectBit(t) values($1)")
          .set("$1", new String[] {"{\"a\":[1,2,3],\"b\":[4,5,6]}", "{\"a\":\"b\",\"c\":null}"},
              PgAdbaType.JSON_ARRAY).submit().getCompletionStage());
      CompletionStage<String[]> idF = session.<String[]>rowOperation("select t from insertAndSelectBit")
          .collect(singleCollector(String[].class))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table insertAndSelectBit").submit().getCompletionStage());

      String[] result = get10(idF);
      assertArrayEquals(new String[] {"{\"a\":[1,2,3],\"b\":[4,5,6]}", "{\"a\":\"b\",\"c\":null}"}, result);
    }
  }

  @Test
  public void selectVeryLargeNumberOfRequests() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {

      //Thread.sleep(60000);

      // Run multiple queries over the connection
      final int queryCount = 100000;
      Submission<Integer>[] submissions = new Submission[queryCount];
      for (int i = 0; i < queryCount; i++) {
        submissions[i] = session.<Integer>rowOperation("SELECT " + i + " as t")
            .collect(CollectorUtils.singleCollector(Integer.class)).submit();
      }

      // Ensure obtain all results
      for (int i = 0; i < queryCount; i++) {
        Integer result = get10(submissions[i].getCompletionStage());
        assertEquals(Integer.valueOf(i), result, "Incorrect result");
      }
    }
  }

  @Test
  public void ensureMultipleQueriesAreNotAllowed() {
    try (Session session = ds.getSession()) {
      assertThrows(ExecutionException.class, () -> get10(session.<Integer>rowOperation("select 0 as t;select 1 as t")
          .submit().getCompletionStage()));
    }
  }

  @Test
  public void selectUnknownDataType() throws InterruptedException, ExecutionException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("select '1' as t")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("1", get10(idF));
    }
  }

  @Test
  public void selectOid() throws InterruptedException, ExecutionException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<Long> idF = session.<Long>rowOperation("SELECT oid as t FROM pg_class limit 1")
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertNotNull(get10(idF));
    }
  }

  @Test
  public <T> void insertAndSelectOid() throws Exception {
    try (Session session = ds.getSession()) {
      get10(session.rowCountOperation("create table insertAndSelectOid(t oid)")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into insertAndSelectOid(t) values($1)")
          .set("$1", 242L, PgAdbaType.OID).submit().getCompletionStage());
      CompletionStage<Long> idF = session.<Long>rowOperation("select t from insertAndSelectOid")
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table insertAndSelectOid").submit().getCompletionStage());

      assertEquals(Long.valueOf(242), get10(idF));
    }
  }

  @Test
  public <T> void insertAndSelectOidArray() throws Exception {
    try (Session session = ds.getSession()) {
      get10(session.rowCountOperation("create table insertAndSelectOidArray(t oid[])")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into insertAndSelectOidArray(t) values($1)")
          .set("$1", new Long[] {242L, 424L}, PgAdbaType.OID_ARRAY).submit().getCompletionStage());
      CompletionStage<Long[]> idF = session.<Long[]>rowOperation("select t from insertAndSelectOidArray")
          .collect(singleCollector(Long[].class))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table insertAndSelectOidArray").submit().getCompletionStage());

      assertArrayEquals(new Long[] {242L, 424L}, get10(idF));
    }
  }

  @Test
  public void selectXml() throws InterruptedException, ExecutionException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("SELECT '<x/>'::xml as t")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertNotNull(get10(idF));
    }
  }

  @Test
  public <T> void insertAndSelectXml() throws Exception {
    try (Session session = ds.getSession()) {
      get10(session.rowCountOperation("create table insertAndSelectOid(t xml)")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into insertAndSelectOid(t) values($1)")
          .set("$1", "<x/>", PgAdbaType.SQLXML).submit().getCompletionStage());
      CompletionStage<String> idF = session.<String>rowOperation("select t from insertAndSelectOid")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table insertAndSelectOid").submit().getCompletionStage());

      assertEquals("<x/>", get10(idF));
    }
  }

  @Test
  public <T> void insertAndSelectXmlArray() throws Exception {
    try (Session session = ds.getSession()) {
      get10(session.rowCountOperation("create table insertAndSelectXmlArray(t xml[])")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into insertAndSelectXmlArray(t) values($1)")
          .set("$1", new String[] {"<x/>", "<x/>"}, PgAdbaType.SQLXML_ARRAY).submit().getCompletionStage());
      CompletionStage<String[]> idF = session.<String[]>rowOperation("select t from insertAndSelectXmlArray")
          .collect(singleCollector(String[].class))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table insertAndSelectXmlArray").submit().getCompletionStage());

      assertArrayEquals(new String[] {"<x/>", "<x/>"}, get10(idF));
    }
  }

  @Test
  public void selectJsonb() throws InterruptedException, ExecutionException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<String> idF = session.<String>rowOperation("SELECT '{}'::jsonb as t")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertNotNull(get10(idF));
    }
  }

  @Test
  public <T> void insertAndSelectJsonb() throws Exception {
    try (Session session = ds.getSession()) {
      get10(session.rowCountOperation("create table insertAndSelectJsonb(t jsonb)")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into insertAndSelectJsonb(t) values($1)")
          .set("$1", "{}", PgAdbaType.JSONB).submit().getCompletionStage());
      CompletionStage<String> idF = session.<String>rowOperation("select t from insertAndSelectJsonb")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table insertAndSelectJsonb").submit().getCompletionStage());

      assertEquals("{}", get10(idF));
    }
  }

  @Test
  public <T> void insertAndSelectJsonbArray() throws Exception {
    try (Session session = ds.getSession()) {
      get10(session.rowCountOperation("create table insertAndSelectJsonbArray(t jsonb[])")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into insertAndSelectJsonbArray(t) values($1)")
          .set("$1", new String[] {"{}", "{}"}, PgAdbaType.JSONB_ARRAY).submit().getCompletionStage());
      CompletionStage<String[]> idF = session.<String[]>rowOperation("select t from insertAndSelectJsonbArray")
          .collect(singleCollector(String[].class))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table insertAndSelectJsonbArray").submit().getCompletionStage());

      assertArrayEquals(new String[] {"{}", "{}"}, get10(idF));
    }
  }

  @Test
  public void selectEmptyInt8Range() throws ExecutionException, InterruptedException, TimeoutException {
    try (Session session = ds.getSession()) {
      CompletionStage<LongRange> idF = session
          .<LongRange>rowOperation("select 'empty'::int8range as t")
          .collect(singleCollector(LongRange.class))
          .submit()
          .getCompletionStage();

      assertEquals(new LongRange(0L, 0L, true, true), get10(idF));
    }
  }
}
