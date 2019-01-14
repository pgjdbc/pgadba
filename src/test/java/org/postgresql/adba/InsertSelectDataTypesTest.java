package org.postgresql.adba;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.postgresql.adba.communication.packets.parts.PgAdbaType.BLOB;
import static org.postgresql.adba.communication.packets.parts.PgAdbaType.BOOLEAN;
import static org.postgresql.adba.communication.packets.parts.PgAdbaType.BOOLEAN_ARRAY;
import static org.postgresql.adba.communication.packets.parts.PgAdbaType.BYTEA_ARRAY;
import static org.postgresql.adba.communication.packets.parts.PgAdbaType.DATE_ARRAY;
import static org.postgresql.adba.communication.packets.parts.PgAdbaType.INTEGER;
import static org.postgresql.adba.communication.packets.parts.PgAdbaType.INTEGER_ARRAY;
import static org.postgresql.adba.communication.packets.parts.PgAdbaType.SHORT_ARRAY;
import static org.postgresql.adba.communication.packets.parts.PgAdbaType.SMALLINT;
import static org.postgresql.adba.communication.packets.parts.PgAdbaType.STRING_ARRAY;
import static org.postgresql.adba.communication.packets.parts.PgAdbaType.UUID_ARRAY;
import static org.postgresql.adba.testutil.CollectorUtils.singleCollector;
import static org.postgresql.adba.testutil.FutureUtil.get10;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.postgresql.adba.communication.packets.parts.PgAdbaType;
import org.postgresql.adba.pgdatatypes.Box;
import org.postgresql.adba.pgdatatypes.Circle;
import org.postgresql.adba.pgdatatypes.IntegerRange;
import org.postgresql.adba.pgdatatypes.Line;
import org.postgresql.adba.pgdatatypes.LineSegment;
import org.postgresql.adba.pgdatatypes.LongRange;
import org.postgresql.adba.pgdatatypes.NumericRange;
import org.postgresql.adba.pgdatatypes.OffsetDateTimeRange;
import org.postgresql.adba.pgdatatypes.Path;
import org.postgresql.adba.pgdatatypes.Point;
import org.postgresql.adba.pgdatatypes.Polygon;
import org.postgresql.adba.testutil.ConnectUtil;
import org.postgresql.adba.testutil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

public class InsertSelectDataTypesTest {
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

  /**
   * Data.
   * @return test data
   */
  public static Collection<Object[]> data() throws UnknownHostException {
    return Arrays.asList(new Object[][] {
        {"bool", true, boolean.class, BOOLEAN, boolean[].class, new Boolean[] {true, false, true}, BOOLEAN_ARRAY,
            new Boolean[] {}, new Boolean[] {null}},
        {"bytea", new byte[] { 0, 1, 2, 3, 4, 5}, byte[].class, BLOB, byte[][].class,
            new byte[][] {{0, 1, 2, 3, 4, 5}, {0, 1, 2, 3, 4, 5}}, BYTEA_ARRAY, new byte[][] {}, new byte[][] {null}},
        {"int2", (short) 21, short.class, SMALLINT, int[].class, new Short[] {21, 22}, SHORT_ARRAY,
            new Short[] {}, new Short[] {null}},
        {"integer", 42, int.class, INTEGER, int[].class, new Integer[] {42, 43}, INTEGER_ARRAY,
            new Integer[] {}, new Integer[] {null}},
        {"int8", 42L, long.class, PgAdbaType.BIGINT, long[].class, new Long[] {84L, 85L}, PgAdbaType.LONG_ARRAY,
            new Long[] {}, new Long[] {null}},
        {"float4", 42.0f, float.class, PgAdbaType.FLOAT, float[].class, new Float[] {42.0f, 43.0f}, PgAdbaType.FLOAT_ARRAY,
            new Float[] {}, new Float[] {null}},
        {"float8", 84.0d, double.class, PgAdbaType.DOUBLE, double[].class, new Double[] {84.0d, 85.0d}, PgAdbaType.DOUBLE_ARRAY,
            new Double[] {}, new Double[] {null}},
        {"bpchar", 'a', char.class, PgAdbaType.CHAR, char[].class, new Character[] {'a', 'ö'}, PgAdbaType.CHAR_ARRAY,
            new Character[] {}, new Character[] {null}},
        {"varchar", "small string", String.class, PgAdbaType.VARCHAR, String[].class, new String[] {"a1", "ö2"}, STRING_ARRAY,
            new String[] {}, new String[] {null}},
        {"uuid", new UUID(1, 1), UUID.class, PgAdbaType.UUID, UUID[].class, new UUID[] {new UUID(1, 1), new UUID(1, 1)},
            UUID_ARRAY, new UUID[] {}, new UUID[] {null}},
        {"date", LocalDate.of(2018, 1, 12), LocalDate.class, PgAdbaType.DATE, LocalDate[].class,
            new LocalDate[] {LocalDate.of(2011, 2, 3), LocalDate.of(2031, 12, 25)}, DATE_ARRAY,
            new LocalDate[] {}, new LocalDate[] {null}},
        {"time", LocalTime.of(14, 1, 12), LocalTime.class, PgAdbaType.TIME, LocalTime[].class,
            new LocalTime[] {LocalTime.of(11, 2, 3), LocalTime.of(22, 12, 25)}, PgAdbaType.TIME_ARRAY,
            new LocalTime[] {}, new LocalTime[] {null}},
        {"timestamp", LocalDateTime.of(2011, 2, 3, 14, 1, 12), LocalDateTime.class,
            PgAdbaType.TIMESTAMP, LocalDateTime[].class, new LocalDateTime[]
            {LocalDateTime.of(2011, 2, 3, 11, 2, 3), LocalDateTime.of(2011, 2, 3, 22, 12, 25)}, PgAdbaType.TIMESTAMP_ARRAY,
            new LocalDateTime[] {}, new LocalDateTime[] {null}},
        {"timestamptz", OffsetDateTime.of(2011, 2, 3, 14, 1, 12, 0, UTC), OffsetDateTime.class,
            PgAdbaType.TIMESTAMP_WITH_TIME_ZONE, OffsetDateTime[].class, new OffsetDateTime[]
            {OffsetDateTime.of(2011, 2, 3, 11, 2, 3, 0, UTC), OffsetDateTime.of(2011, 2, 3, 22, 12, 25, 0, UTC)},
            PgAdbaType.TIMESTAMP_WITH_TIME_ZONE_ARRAY, new OffsetDateTime[] {}, new OffsetDateTime[] {null}},
        {"timetz", OffsetTime.of(14, 1, 12, 0, UTC), OffsetTime.class,
            PgAdbaType.TIME_WITH_TIME_ZONE, OffsetTime[].class, new OffsetTime[]
            {OffsetTime.of(11, 2, 3, 0, UTC), OffsetTime.of(22, 12, 25, 0, UTC)}, PgAdbaType.TIME_WITH_TIME_ZONE_ARRAY,
            new OffsetTime[] {}, new OffsetTime[] {null}},
        {"numeric", BigDecimal.ONE, BigDecimal.class,
            PgAdbaType.NUMERIC, BigDecimal[].class, new BigDecimal[]
            {new BigDecimal("15.22"), new BigDecimal("20.002")}, PgAdbaType.NUMERIC_ARRAY,
            new BigDecimal[] {}, new BigDecimal[] {null}},
        {"interval", Duration.of(10, SECONDS), Duration.class,
            PgAdbaType.INTERVAL, BigDecimal[].class, new Duration[]
            {Duration.of(10, SECONDS), Duration.of(15, MINUTES)}, PgAdbaType.INTERVAL_ARRAY,
            new Duration[] {}, new Duration[] {null}},
        {"cidr", InetAddress.getByAddress(new byte[] {127, 0, 0, 1}), InetAddress.class,
            PgAdbaType.CIDR, InetAddress[].class, new InetAddress[]
            {InetAddress.getByAddress(new byte[] {127, 0, 0, 1}),
                InetAddress.getByAddress(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1})},
            PgAdbaType.CIDR_ARRAY, new InetAddress[] {}, new InetAddress[] {null}},
        {"point", new Point(1, 2), Point.class, PgAdbaType.POINT, Point[].class, new Point[]
            {new Point(0, 1), new Point(2, 3)},
            PgAdbaType.POINT_ARRAY, new Point[] {}, new Point[] {null}},
        {"line", new Line(1, 2, 3), Line.class, PgAdbaType.LINE, Line[].class, new Line[]
            {new Line(0, 1, 3), new Line(2, 3, 5)},
            PgAdbaType.LINE_ARRAY, new Line[] {}, new Line[] {null}},
        {"lseg", new LineSegment(1, 2, 3, 4), LineSegment.class, PgAdbaType.LINE_SEGMENT, LineSegment[].class,
            new LineSegment[] {new LineSegment(0, 1, 3, 4), new LineSegment(2, 3, 5, 6)},
            PgAdbaType.LINE_SEGMENT_ARRAY, new LineSegment[] {}, new LineSegment[] {null}},
        {"box", new Box(1, 2, 3, 4), Box.class, PgAdbaType.BOX, Box[].class,
            new Box[] {new Box(0, 1, 3, 4), new Box(2, 3, 5, 6)},
            PgAdbaType.BOX_ARRAY, new Box[] {}, new Box[] {null}},
        {"path", new Path(true, new Point(1, 2), new Point(3, 4)), Path.class, PgAdbaType.PATH, Path[].class,
            new Path[] {new Path(false, new Point(0, 1), new Point(3, 4)),
                new Path(true, new Point(2, 3), new Point(5, 6))},
            PgAdbaType.PATH_ARRAY, new Path[] {}, new Path[] {null}},
        {"polygon", new Polygon(new Point(1, 2), new Point(3, 4)), Polygon.class, PgAdbaType.POLYGON,
            Polygon[].class, new Polygon[] {new Polygon(new Point(0, 1), new Point(3, 4)),
                new Polygon(new Point(2, 3), new Point(5, 6))},
            PgAdbaType.POLYGON_ARRAY, new Polygon[] {}, new Polygon[] {null}},
        {"circle", new Circle(1, 2, 3), Circle.class, PgAdbaType.CIRCLE,
            Circle[].class, new Circle[] {new Circle(0, 1, 3), new Circle(2, 3, 5)},
            PgAdbaType.CIRCLE_ARRAY, new Circle[] {}, new Circle[] {null}},
        {"int8range", new LongRange(1L, 2L, true, false), LongRange.class, PgAdbaType.LONG_RANGE,
            LongRange[].class, new LongRange[] {new LongRange(null, null, false, true),
            new LongRange(0L, 0L, true, true), new LongRange()}, PgAdbaType.LONG_RANGE_ARRAY,
            new LongRange[] {}, new LongRange[] {null}},
        {"int4range", new IntegerRange(1, 2, true, false), IntegerRange.class, PgAdbaType.INTEGER_RANGE,
            IntegerRange[].class, new IntegerRange[] {new IntegerRange(null, null, false, true),
            new IntegerRange(0, 0, true, true), new IntegerRange()}, PgAdbaType.INTEGER_RANGE_ARRAY,
            new IntegerRange[] {}, new IntegerRange[] {null}},
        {"numrange", new NumericRange(new BigDecimal(2.6), new BigDecimal(3.3), true, false),
            NumericRange.class, PgAdbaType.NUMERIC_RANGE, NumericRange[].class, new NumericRange[]
            {new NumericRange(null, null, false, true),
            new NumericRange(BigDecimal.ZERO, BigDecimal.ZERO, true, true), new NumericRange()},
            PgAdbaType.NUMERIC_RANGE_ARRAY, new NumericRange[] {}, new NumericRange[] {null}},
        {"tstzrange", new OffsetDateTimeRange(OffsetDateTime.of(2019, 1, 2, 3, 4, 5, 0, UTC),
            OffsetDateTime.of(2020, 1, 2, 3, 4, 5, 0, UTC), true, false),
            OffsetDateTimeRange.class, PgAdbaType.OFFSET_DATE_TIME_RANGE, OffsetDateTimeRange[].class, new OffsetDateTimeRange[]
            {new OffsetDateTimeRange(null, null, false, true),
            new OffsetDateTimeRange(OffsetDateTime.of(2021, 1, 2, 3, 4, 5, 0, UTC),
                OffsetDateTime.of(2021, 1, 2, 3, 4, 5, 0, UTC), true, true), new OffsetDateTimeRange()},
            PgAdbaType.OFFSET_DATE_TIME_RANGE_ARRAY, new OffsetDateTimeRange[] {}, new OffsetDateTimeRange[] {null}},
    });
  }

  private static final AtomicInteger table = new AtomicInteger(0);

  @ParameterizedTest
  @MethodSource("data")
  public <T> void insertAndSelect(String dataTypeName, T insertData, Class<T> type) throws Exception {
    try (Session session = ds.getSession()) {
      String tableName = "insertAndSelect" + table.incrementAndGet();
      get10(session.rowCountOperation("create table " + tableName + "(t " + dataTypeName + ")")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into " + tableName + "(t) values($1)")
          .set("$1", insertData).submit().getCompletionStage());
      CompletionStage<T> idF = session.<T>rowOperation("select t from " + tableName)
          .collect(singleCollector(type))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table " + tableName).submit().getCompletionStage());

      if (type.isArray()) {
        assertArrayEquals((byte[])insertData, (byte[])get10(idF));
      } else {
        T t = get10(idF);
        assertEquals(insertData, t);
      }
    }
  }

  @ParameterizedTest
  @MethodSource("data")
  public <T> void insertAndSelectWithTypeHinting(String dataTypeName, T insertData, Class<T> type, PgAdbaType adbaType)
      throws Exception {
    try (Session session = ds.getSession()) {
      String tableName = "insertAndSelect" + table.incrementAndGet();
      get10(session.rowCountOperation("create table " + tableName + "(t " + dataTypeName + ")")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into " + tableName + "(t) values($1)")
          .set("$1", insertData, adbaType).submit().getCompletionStage());
      CompletionStage<T> idF = session.<T>rowOperation("select t from " + tableName)
          .collect(singleCollector(type))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table " + tableName).submit().getCompletionStage());

      if (type.isArray()) {
        assertArrayEquals((byte[])insertData, (byte[])get10(idF));
      } else {
        assertEquals(insertData, get10(idF));
      }
    }
  }

  @ParameterizedTest
  @MethodSource("data")
  public <T> void insertAndSelectNullWithTypeHinting(String dataTypeName, T insertData, Class<T> type, PgAdbaType adbaType)
      throws Exception {
    try (Session session = ds.getSession()) {
      String tableName = "insertAndSelect" + table.incrementAndGet();
      get10(session.rowCountOperation("create table " + tableName + "(t " + dataTypeName + ")")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into " + tableName + "(t) values($1)")
          .set("$1", null, adbaType).submit().getCompletionStage());
      CompletionStage<T> idF = session.<T>rowOperation("select t from " + tableName)
          .collect(singleCollector(type))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table " + tableName).submit().getCompletionStage());

      assertNull(get10(idF));
    }
  }

  @ParameterizedTest
  @MethodSource("data")
  public <T> void insertAndSelectArray(String dataTypeName, T insertData, Class<T> type, PgAdbaType adbaType,
      Class<T[]> arrayType, T[] testArrayData) throws Exception {
    try (Session session = ds.getSession()) {
      String tableName = "insertAndSelect" + table.incrementAndGet();
      get10(session.rowCountOperation("create table " + tableName + "(t " + dataTypeName + "[])")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into " + tableName + "(t) values($1)")
          .set("$1", testArrayData).submit().getCompletionStage());
      CompletionStage<T[]> idF = session.<T[]>rowOperation("select t from " + tableName)
          .collect(singleCollector(arrayType))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table " + tableName).submit().getCompletionStage());

      T[] result = get10(idF);
      assertArrayEquals(testArrayData, result);
    }
  }

  @ParameterizedTest
  @MethodSource("data")
  public <T> void insertAndSelectArrayWithTypeHinting(String dataTypeName, T insertData, Class<T> type, PgAdbaType adbaType,
      Class<T[]> arrayType, T[] testArrayData, PgAdbaType arrayAdbaType) throws Exception {
    try (Session session = ds.getSession()) {
      String tableName = "insertAndSelect" + table.incrementAndGet();
      get10(session.rowCountOperation("create table " + tableName + "(t " + dataTypeName + "[])")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into " + tableName + "(t) values($1)")
          .set("$1", testArrayData, arrayAdbaType).submit().getCompletionStage());
      CompletionStage<T[]> idF = session.<T[]>rowOperation("select t from " + tableName)
          .collect(singleCollector(arrayType))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table " + tableName).submit().getCompletionStage());

      T[] result = get10(idF);
      assertArrayEquals(testArrayData, result);
    }
  }

  @ParameterizedTest
  @MethodSource("data")
  public <T> void insertAndSelectEmptyArray(String dataTypeName, T insertData, Class<T> type, PgAdbaType adbaType,
      Class<T[]> arrayType, T[] testArrayData, PgAdbaType arrayAdbaType, T[] emptyArrayData) throws Exception {
    try (Session session = ds.getSession()) {
      String tableName = "insertAndSelect" + table.incrementAndGet();
      get10(session.rowCountOperation("create table " + tableName + "(t " + dataTypeName + "[])")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into " + tableName + "(t) values($1)")
          .set("$1", emptyArrayData, arrayAdbaType).submit().getCompletionStage());
      CompletionStage<T[]> idF = session.<T[]>rowOperation("select t from " + tableName)
          .collect(singleCollector(arrayType))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table " + tableName).submit().getCompletionStage());

      T[] result = get10(idF);
      assertArrayEquals(emptyArrayData, result);
    }
  }

  @ParameterizedTest
  @MethodSource("data")
  public <T> void insertAndSelectArrayWithNull(String dataTypeName, T insertData, Class<T> type, PgAdbaType adbaType,
      Class<T[]> arrayType, T[] testArrayData, PgAdbaType arrayAdbaType, T[] emptyArrayData, T[] arrayDataWithNull)
      throws Exception {
    try (Session session = ds.getSession()) {
      String tableName = "insertAndSelect" + table.incrementAndGet();
      get10(session.rowCountOperation("create table " + tableName + "(t " + dataTypeName + "[])")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into " + tableName + "(t) values($1)")
          .set("$1", arrayDataWithNull, arrayAdbaType).submit().getCompletionStage());
      CompletionStage<T[]> idF = session.<T[]>rowOperation("select t from " + tableName)
          .collect(singleCollector(arrayType))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table " + tableName).submit().getCompletionStage());

      T[] result = get10(idF);
      assertArrayEquals(arrayDataWithNull, result);
    }
  }

  @ParameterizedTest
  @MethodSource("data")
  public <T> void insertAndSelectNullArray(String dataTypeName, T insertData, Class<T> type, PgAdbaType adbaType,
      Class<T[]> arrayType, T[] testArrayData, PgAdbaType arrayAdbaType) throws Exception {
    try (Session session = ds.getSession()) {
      String tableName = "insertAndSelect" + table.incrementAndGet();
      get10(session.rowCountOperation("create table " + tableName + "(t " + dataTypeName + "[])")
          .submit().getCompletionStage());
      get10(session.rowCountOperation("insert into " + tableName + "(t) values($1)")
          .set("$1", null, arrayAdbaType).submit().getCompletionStage());
      CompletionStage<T[]> idF = session.<T[]>rowOperation("select t from " + tableName)
          .collect(singleCollector(arrayType))
          .submit()
          .getCompletionStage();
      get10(session.rowCountOperation("drop table " + tableName).submit().getCompletionStage());

      assertNull(get10(idF));
    }
  }

}
