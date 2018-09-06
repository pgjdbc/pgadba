package org.postgresql.sql2;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.postgresql.sql2.testutil.CollectorUtils.singleCollector;
import static org.postgresql.sql2.testutil.FutureUtil.get10;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Submission;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.sql2.testutil.CollectorUtils;
import org.postgresql.sql2.testutil.ConnectUtil;
import org.postgresql.sql2.testutil.DatabaseHolder;
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
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select 100 as t")
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), get10(idF));
    }
  }

  @Test
  public void selectInteger4asInteger8() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Long> idF = conn.<Long>rowOperation("select 100 as t")
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), get10(idF));
    }
  }

  @Test
  public void selectInteger4AsShort() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Short> idF = conn.<Short>rowOperation("select 100 as t")
          .collect(singleCollector(Short.class))
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short)100), get10(idF));
    }
  }

  @Test
  public void selectInteger2() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Short> idF = conn.<Short>rowOperation("select 100::int2 as t")
          .collect(singleCollector(Short.class))
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short) 100), get10(idF));
    }
  }

  @Test
  public void selectInteger2AsLong() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Long> idF = conn.<Long>rowOperation("select 100::int2 as t")
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), get10(idF));
    }
  }

  @Test
  public void selectInteger2AsInteger() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select 100::int2 as t")
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), get10(idF));
    }
  }

  @Test
  public void selectInteger8() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Long> idF = conn.<Long>rowOperation("select 100::int8 as t")
          .collect(singleCollector(Long.class))
          .submit()
          .getCompletionStage();

      assertEquals(Long.valueOf(100), get10(idF));
    }
  }

  @Test
  public void selectInteger8AsInteger() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select 100::int8 as t")
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertEquals(Integer.valueOf(100), get10(idF));
    }
  }

  @Test
  public void selectInteger8AsIntegerTooLarge() {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer> idF = conn.<Integer>rowOperation("select " + Long.MAX_VALUE + "::int8 as t")
          .collect(singleCollector(Integer.class))
          .submit()
          .getCompletionStage();

      assertThrows(ExecutionException.class, () -> get10(idF));
    }
  }

  @Test
  public void selectInteger8AsShort() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Short> idF = conn.<Short>rowOperation("select 100::int8 as t")
          .collect(singleCollector(Short.class))
          .submit()
          .getCompletionStage();

      assertEquals(Short.valueOf((short)100), get10(idF));
    }
  }

  @Test
  public void selectNumeric() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<BigDecimal> idF = conn.<BigDecimal>rowOperation("select 100.505::numeric as t")
          .collect(singleCollector(BigDecimal.class))
          .submit()
          .getCompletionStage();

      assertEquals(BigDecimal.valueOf(100.505), get10(idF));
    }
  }

  @Test
  public void selectReal() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Float> idF = conn.<Float>rowOperation("select 100.505::real as t")
          .collect(singleCollector(Float.class))
          .submit()
          .getCompletionStage();

      assertEquals((float) 100.505, get10(idF), 0.5);
    }
  }

  @Test
  public void selectDouble() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Double> idF = conn.<Double>rowOperation("select 100.505::double precision as t")
          .collect(singleCollector(Double.class))
          .submit()
          .getCompletionStage();

      assertEquals((float) 100.505, get10(idF), 0.5);
    }
  }

  @Test
  public void selectMoney() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select 100.505::money as t")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("$100.51", get10(idF));
    }
  }

  @Test
  public void selectVarchar() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select 'Sphinx of black quartz, judge my vow'::varchar as t")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("Sphinx of black quartz, judge my vow", get10(idF));
    }
  }

  @Test
  public void selectCharacter() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character> idF = conn.<Character>rowOperation("select 'H'::character as t")
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('H'), get10(idF));
    }
  }

  @Test
  public void selectText() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String> idF = conn.<String>rowOperation("select 'How vexingly quick daft zebras jump!'::text as t")
          .collect(singleCollector(String.class))
          .submit()
          .getCompletionStage();

      assertEquals("How vexingly quick daft zebras jump!", get10(idF));
    }
  }

  @Test
  public void selectBytea() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<byte[]> idF = conn.<byte[]>rowOperation("select 'DEADBEEF'::bytea as t")
          .collect(singleCollector(byte[].class))
          .submit()
          .getCompletionStage();

      byte[] expected = new byte[]{0x44, 0x45, 0x41, 0x44, 0x42, 0x45, 0x45, 0x46};
      assertArrayEquals(expected, get10(idF));
    }
  }

  @Test
  public void selectTimestampWithoutTimeZone() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDateTime> idF = conn
          .<LocalDateTime>rowOperation("select '2018-04-29 20:55:57.692132'::timestamp without time zone as t")
          .collect(singleCollector(LocalDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(LocalDateTime.of(2018, 4, 29, 20, 55, 57, 692132000), get10(idF));
    }
  }

  @Test
  public void selectTimestampWithTimeZone() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetDateTime> idF = conn
          .<OffsetDateTime>rowOperation("select '2018-04-29 20:55:57.692132'::timestamp with time zone as t")
          .collect(singleCollector(OffsetDateTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(OffsetDateTime.of(2018, 4, 29, 20, 55, 57, 692132000, ZoneOffset.UTC), get10(idF));
    }
  }

  @Test
  public void selectDate() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDate> idF = conn.<LocalDate>rowOperation("select '2018-04-29 20:55:57.692132'::date as t")
          .collect(singleCollector(LocalDate.class))
          .submit()
          .getCompletionStage();

      assertEquals(LocalDate.of(2018, 4, 29), get10(idF));
    }
  }

  @Test
  public void selectDateNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDate> idF = conn.<LocalDate>rowOperation("select null::date as t")
          .collect(singleCollector(LocalDate.class))
          .submit()
          .getCompletionStage();

      assertNull(get10(idF));
    }
  }

  @Test
  public void selectDateAsTime() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalTime> idF = conn.<LocalTime>rowOperation("select '2018-04-29 20:55:57.692132'::timestamp as t")
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(LocalTime.of(20, 55, 57, 692132000), get10(idF));
    }
  }

  @Test
  public void selectTime() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalTime> idF = conn.<LocalTime>rowOperation("select '2018-04-29 20:55:57.692132'::time as t")
          .collect(singleCollector(LocalTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(LocalTime.of(20, 55, 57, 692132000), get10(idF));
    }
  }

  @Test
  public void selectTimeWithTimeZone() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetTime> idF = conn
          .<OffsetTime>rowOperation("select '2018-04-29 20:55:57.692132'::time with time zone as t")
          .collect(singleCollector(OffsetTime.class))
          .submit()
          .getCompletionStage();

      assertEquals(OffsetTime.of(20, 55, 57, 692132000, ZoneOffset.UTC), get10(idF));
    }
  }

  @Test
  public void selectInterval() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Duration> idF = conn.<Duration>rowOperation("select INTERVAL '2 months 3 days' as t")
          .collect(singleCollector(Duration.class))
          .submit()
          .getCompletionStage();

      assertEquals(Duration.of(1512, ChronoUnit.HOURS), get10(idF));
    }
  }

  @Test
  public void selectEnum() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      conn.rowCountOperation("CREATE TYPE happiness AS ENUM ('happy', 'very happy', 'ecstatic');").submit();
      CompletionStage<String[]> idF = conn.<String[]>rowOperation("SELECT unnest(enum_range(NULL::happiness)) as t")
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
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Boolean> idF = conn.<Boolean>rowOperation("select true::bool as t")
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertTrue(get10(idF));
    }
  }

  @Test
  public void selectBooleanNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Boolean> idF = conn.<Boolean>rowOperation("select null::bool as t")
          .collect(singleCollector(Boolean.class))
          .submit()
          .getCompletionStage();

      assertNull(get10(idF));
    }
  }

  @Test
  public void selectUuid() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<UUID> idF = conn.<UUID>rowOperation("select 'a81bc81b-dead-4e5d-abff-90865d1e13b1'::uuid as t")
          .collect(singleCollector(UUID.class))
          .submit()
          .getCompletionStage();

      assertEquals(UUID.fromString("a81bc81b-dead-4e5d-abff-90865d1e13b1"), get10(idF));
    }
  }

  @Test
  public void selectChar() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      // select from one of the systems tables, as ::char returns a bpchar (oid 1042) instead of a char (oid 18)
      CompletionStage<Character> idF = conn.<Character>rowOperation("select aggkind as t from pg_aggregate limit 1")
          .collect(singleCollector(Character.class))
          .submit()
          .getCompletionStage();

      assertEquals(Character.valueOf('n'), get10(idF));
    }
  }

  @Test
  public void selectInteger2Array() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Short[]> idF = conn.<Short[]>rowOperation("select ARRAY[100::int2, 200::int2, 300::int2] as t")
          .collect(singleCollector(Short[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Short[] {100, 200, 300}, get10(idF));
    }
  }

  @Test
  public void selectInteger2ArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Short[]> idF = conn.<Short[]>rowOperation("select ARRAY[]::int2[] as t")
          .collect(singleCollector(Short[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Short[] {}, get10(idF));
    }
  }

  @Test
  public void selectInteger2ArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Short[]> idF = conn.<Short[]>rowOperation("select ARRAY[null::int2] as t")
          .collect(singleCollector(Short[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Short[] {null}, get10(idF));
    }
  }

  @Test
  public void selectInteger4ArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer[]> idF = conn.<Integer[]>rowOperation("select ARRAY[null::int4] as t")
          .collect(singleCollector(Integer[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Integer[] {null}, get10(idF));
    }
  }

  @Test
  public void selectInteger4ArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer[]> idF = conn.<Integer[]>rowOperation("select ARRAY[]::int4[] as t")
          .collect(singleCollector(Integer[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Integer[] {}, get10(idF));
    }
  }

  @Test
  public void selectInteger4Array() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Integer[]> idF = conn.<Integer[]>rowOperation("select ARRAY[100, 200, 300] as t")
          .collect(singleCollector(Integer[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Integer[] {100, 200, 300}, get10(idF));
    }
  }

  @Test
  public void selectInteger8Array() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Long[]> idF = conn.<Long[]>rowOperation("select ARRAY[100::int8, 200::int8, 300::int8] as t")
          .collect(singleCollector(Long[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Long[] {100L, 200L, 300L}, get10(idF));
    }
  }

  @Test
  public void selectInteger8ArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Long[]> idF = conn.<Long[]>rowOperation("select ARRAY[null::int8] as t")
          .collect(singleCollector(Long[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Long[] {null}, get10(idF));
    }
  }

  @Test
  public void selectInteger8ArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Long[]> idF = conn.<Long[]>rowOperation("select ARRAY[]::int8[] as t")
          .collect(singleCollector(Long[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Long[] {}, get10(idF));
    }
  }

  @Test
  public void selectFloatArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Float[]> idF = conn.<Float[]>rowOperation("select ARRAY[100::float4, 200::float4, 300::float4] as t")
          .collect(singleCollector(Float[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Float[] {100.0f, 200.0f, 300.0f}, get10(idF));
    }
  }

  @Test
  public void selectFloatArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Float[]> idF = conn.<Float[]>rowOperation("select ARRAY[null::float4, 200::float4, 300::float4] as t")
          .collect(singleCollector(Float[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Float[] {null, 200.0f, 300.0f}, get10(idF));
    }
  }

  @Test
  public void selectFloatArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Float[]> idF = conn.<Float[]>rowOperation("select ARRAY[]::float4[] as t")
          .collect(singleCollector(Float[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Float[] {}, get10(idF));
    }
  }

  @Test
  public void selectDoubleArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Double[]> idF = conn.<Double[]>rowOperation("select ARRAY[100::float8, 200::float8, 300::float8] as t")
          .collect(singleCollector(Double[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Double[] {100.0, 200.0, 300.0}, get10(idF));
    }
  }

  @Test
  public void selectDoubleArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Double[]> idF = conn.<Double[]>rowOperation("select ARRAY[]::float8[] as t")
          .collect(singleCollector(Double[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Double[] {}, get10(idF));
    }
  }

  @Test
  public void selectDoubleArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Double[]> idF = conn.<Double[]>rowOperation("select ARRAY[null::float8] as t")
          .collect(singleCollector(Double[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Double[] {null}, get10(idF));
    }
  }

  @Test
  public void selectBooleanArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Boolean[]> idF = conn.<Boolean[]>rowOperation("select ARRAY[true, false, true] as t")
          .collect(singleCollector(Boolean[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Boolean[] {true, false, true}, get10(idF));
    }
  }

  @Test
  public void selectBooleanArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Boolean[]> idF = conn.<Boolean[]>rowOperation("select ARRAY[null::boolean] as t")
          .collect(singleCollector(Boolean[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Boolean[] {null}, get10(idF));
    }
  }

  @Test
  public void selectBooleanArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Boolean[]> idF = conn.<Boolean[]>rowOperation("select ARRAY[]::boolean[] as t")
          .collect(singleCollector(Boolean[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Boolean[] {}, get10(idF));
    }
  }

  @Test
  public void selectStringArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String[]> idF = conn.<String[]>rowOperation("select ARRAY['first', '\"second', 'th,ird', 'NULL'] as t")
          .collect(singleCollector(String[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new String[] {"first", "\"second", "th,ird", "NULL"}, get10(idF));
    }
  }

  @Test
  public void selectStringArrayEmptyArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String[]> idF = conn.<String[]>rowOperation("select ARRAY[]::varchar[] as t")
          .collect(singleCollector(String[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new String[] {}, get10(idF));
    }
  }

  @Test
  public void selectStringArrayOnlyNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<String[]> idF = conn.<String[]>rowOperation("select ARRAY[null]::varchar[] as t")
          .collect(singleCollector(String[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new String[] {null}, get10(idF));
    }
  }

  @Test
  public void selectDateArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDate[]> idF = conn.<LocalDate[]>rowOperation("select ARRAY['2018-04-29 20:55:57.692132'::date, "
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
    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDate[]> idF = conn.<LocalDate[]>rowOperation("select ARRAY[null::date, "
          + "'2018-05-30 20:55:57.692132'::date] as t")
          .collect(singleCollector(LocalDate[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new LocalDate[] {null, LocalDate.of(2018, 5, 30)}, get10(idF));
    }
  }

  @Test
  public void selectDateArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDate[]> idF = conn.<LocalDate[]>rowOperation("select ARRAY[]::date[] as t")
          .collect(singleCollector(LocalDate[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new LocalDate[] {}, get10(idF));
    }
  }

  @Test
  public void selectTimeArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalTime[]> idF = conn.<LocalTime[]>rowOperation("select ARRAY['2018-04-29 20:55:57.692132'::time, "
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
    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalTime[]> idF = conn.<LocalTime[]>rowOperation("select ARRAY[null, "
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
    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalTime[]> idF = conn.<LocalTime[]>rowOperation("select ARRAY[]::time[] as t")
          .collect(singleCollector(LocalTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new LocalTime[] {}, get10(idF));
    }
  }

  @Test
  public void selectTimestampWithoutTimeZoneArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDateTime[]> idF = conn
          .<LocalDateTime[]>rowOperation("select ARRAY['2018-04-29 20:55:57.692132'::timestamp without time zone] as t")
          .collect(singleCollector(LocalDateTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new LocalDateTime[] {LocalDateTime.of(2018, 4, 29, 20, 55, 57, 692132000)}, get10(idF));
    }
  }

  @Test
  public void selectTimestampWithoutTimeZoneArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDateTime[]> idF = conn
          .<LocalDateTime[]>rowOperation("select ARRAY[null::timestamp without time zone] as t")
          .collect(singleCollector(LocalDateTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new LocalDateTime[] {null}, get10(idF));
    }
  }

  @Test
  public void selectTimestampWithoutTimeZoneArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<LocalDateTime[]> idF = conn
          .<LocalDateTime[]>rowOperation("select ARRAY[]::timestamp without time zone[] as t")
          .collect(singleCollector(LocalDateTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new LocalDateTime[] {}, get10(idF));
    }
  }

  @Test
  public void selectTimestampWithTimeZoneArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetDateTime[]> idF = conn
          .<OffsetDateTime[]>rowOperation("select ARRAY['2018-04-29 20:55:57.692132'::timestamp with time zone] as t")
          .collect(singleCollector(OffsetDateTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new OffsetDateTime[] {OffsetDateTime.of(2018, 4, 29, 20, 55, 57, 692132000, ZoneOffset.UTC)}, get10(idF));
    }
  }

  @Test
  public void selectTimestampWithTimeZoneArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetDateTime[]> idF = conn
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
    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetDateTime[]> idF = conn
          .<OffsetDateTime[]>rowOperation("select ARRAY[]::timestamp with time zone[] as t")
          .collect(singleCollector(OffsetDateTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new OffsetDateTime[] {}, get10(idF));
    }
  }

  @Test
  public void selectNumericArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<BigDecimal[]> idF = conn.<BigDecimal[]>rowOperation("select ARRAY[100.505::numeric, 200.505::numeric] as t")
          .collect(singleCollector(BigDecimal[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new BigDecimal[] {BigDecimal.valueOf(100.505), BigDecimal.valueOf(200.505)}, get10(idF));
    }
  }

  @Test
  public void selectNumericArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<BigDecimal[]> idF = conn.<BigDecimal[]>rowOperation("select ARRAY[null, 200.505::numeric] as t")
          .collect(singleCollector(BigDecimal[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new BigDecimal[] {null, BigDecimal.valueOf(200.505)}, get10(idF));
    }
  }

  @Test
  public void selectNumericArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<BigDecimal[]> idF = conn.<BigDecimal[]>rowOperation("select ARRAY[]::numeric[] as t")
          .collect(singleCollector(BigDecimal[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new BigDecimal[] {}, get10(idF));
    }
  }

  @Test
  public void selectTimeWithTimeZoneArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetTime[]> idF = conn
          .<OffsetTime[]>rowOperation("select ARRAY['2018-04-29 20:55:57.692132'::time with time zone] as t")
          .collect(singleCollector(OffsetTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new OffsetTime[] {OffsetTime.of(20, 55, 57, 692132000, ZoneOffset.UTC)}, get10(idF));
    }
  }

  @Test
  public void selectTimeWithTimeZoneArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetTime[]> idF = conn
          .<OffsetTime[]>rowOperation("select ARRAY[null, '2018-04-29 20:55:57.692132'::time with time zone] as t")
          .collect(singleCollector(OffsetTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new OffsetTime[] {null, OffsetTime.of(20, 55, 57, 692132000, ZoneOffset.UTC)}, get10(idF));
    }
  }

  @Test
  public void selectTimeWithTimeZoneArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<OffsetTime[]> idF = conn
          .<OffsetTime[]>rowOperation("select ARRAY[]::time with time zone[] as t")
          .collect(singleCollector(OffsetTime[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new OffsetTime[] {}, get10(idF));
    }
  }

  @Test
  public void selectUuidArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<UUID[]> idF = conn.<UUID[]>rowOperation("select ARRAY['a81bc81b-dead-4e5d-abff-90865d1e13b1'::uuid] as t")
          .collect(singleCollector(UUID[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new UUID[] {UUID.fromString("a81bc81b-dead-4e5d-abff-90865d1e13b1")}, get10(idF));
    }
  }

  @Test
  public void selectUuidArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<UUID[]> idF = conn.<UUID[]>rowOperation("select ARRAY[null, "
          + "'a81bc81b-dead-4e5d-abff-90865d1e13b1'::uuid] as t")
          .collect(singleCollector(UUID[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new UUID[] {null, UUID.fromString("a81bc81b-dead-4e5d-abff-90865d1e13b1")}, get10(idF));
    }
  }

  @Test
  public void selectUuidArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<UUID[]> idF = conn.<UUID[]>rowOperation("select ARRAY[]::uuid[] as t")
          .collect(singleCollector(UUID[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new UUID[] {}, get10(idF));
    }
  }

  @Test
  public void selectCharacterArray() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character[]> idF = conn.<Character[]>rowOperation("select ARRAY['H'::character] as t")
          .collect(singleCollector(Character[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Character[] {'H'}, get10(idF));
    }
  }

  @Test
  public void selectCharacterArrayWithNull() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character[]> idF = conn.<Character[]>rowOperation("select ARRAY[null, 'H'::character] as t")
          .collect(singleCollector(Character[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Character[] {null, 'H'}, get10(idF));
    }
  }

  @Test
  public void selectCharacterArrayEmpty() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection conn = ds.getConnection()) {
      CompletionStage<Character[]> idF = conn.<Character[]>rowOperation("select ARRAY[]::character[] as t")
          .collect(singleCollector(Character[].class))
          .submit()
          .getCompletionStage();

      assertArrayEquals(new Character[] {}, get10(idF));
    }
  }

  @Test
  public void selectVeryLargeNumberOfRequests() throws ExecutionException, InterruptedException, TimeoutException {
    try (Connection connection = ds.getConnection()) {

      //Thread.sleep(60000);

      // Run multiple queries over the connection
      final int queryCount = 100000;
      Submission<Integer>[] submissions = new Submission[queryCount];
      for (int i = 0; i < queryCount; i++) {
        submissions[i] = connection.<Integer>rowOperation("SELECT " + i + " as t")
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
    try (Connection connection = ds.getConnection()) {
      assertThrows(ExecutionException.class, () -> get10(connection.<Integer>rowOperation("select 0 as t;select 1 as t")
          .submit().getCompletionStage()));
    }
  }
}
