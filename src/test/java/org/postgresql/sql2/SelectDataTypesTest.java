package org.postgresql.sql2;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.sql2.communication.packets.parts.PgAdbaType;
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
  public void selectInteger8AsIntegerTooLarge() throws ExecutionException, InterruptedException, TimeoutException {
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
  public void insertAndSelectByteArray() throws ExecutionException, InterruptedException, TimeoutException {
    byte[] insertData = new byte[] { 0, 1, 2, 3, 4, 5};

    try (Connection conn = ds.getConnection()) {
      get10(conn.rowCountOperation("create table insertAndSelectByteArray(t bytea)").submit().getCompletionStage());
      get10(conn.rowCountOperation("insert into insertAndSelectByteArray(t) values($1)")
          .set("$1", insertData, PgAdbaType.BLOB).submit().getCompletionStage());
      CompletionStage<byte[]> idF = conn.<byte[]>rowOperation("select t from insertAndSelectByteArray")
          .collect(singleCollector(byte[].class))
          .submit()
          .getCompletionStage();
      get10(conn.rowCountOperation("drop table insertAndSelectByteArray").submit().getCompletionStage());

      assertArrayEquals(insertData, get10(idF));
    }
  }

  /*
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
  */

  @Test
  public void ensureMultipleQueriesAreNotAllowed() {
    try (Connection connection = ds.getConnection()) {
      assertThrows(ExecutionException.class, () -> get10(connection.<Integer>rowOperation("select 0 as t;select 1 as t")
          .submit().getCompletionStage()));
    }
  }
}
