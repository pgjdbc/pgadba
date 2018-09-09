package org.postgresql.sql2;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.postgresql.sql2.testutil.CollectorUtils.singleCollector;
import static org.postgresql.sql2.testutil.FutureUtil.get10;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.postgresql.sql2.communication.packets.parts.PgAdbaType;
import org.postgresql.sql2.testutil.ConnectUtil;
import org.postgresql.sql2.testutil.DatabaseHolder;
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
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {"bool", true, boolean.class, PgAdbaType.BOOLEAN, boolean[].class, new Boolean[] {true, false, true}},
        {"bytea", new byte[] { 0, 1, 2, 3, 4, 5}, byte[].class, PgAdbaType.BLOB, byte[][].class,
            new byte[][] {{0, 1, 2, 3, 4, 5}, {0, 1, 2, 3, 4, 5}}},
        {"int2", (short) 21, short.class, PgAdbaType.SMALLINT, int[].class, new Short[] {21, 22}},
        {"integer", 42, int.class, PgAdbaType.INTEGER, int[].class, new Integer[] {42, 43}},
        {"int8", 42L, long.class, PgAdbaType.BIGINT, long[].class, new Long[] {84L, 85L}},
        {"float4", 42.0f, float.class, PgAdbaType.FLOAT, float[].class, new Float[] {42.0f, 43.0f}},
        {"float8", 84.0d, double.class, PgAdbaType.DOUBLE, double[].class, new Double[] {84.0d, 85.0d}},
    });
  }

  private static final AtomicInteger table = new AtomicInteger(0);

  @ParameterizedTest
  @MethodSource("data")
  public <T> void insertAndSelect(String dataTypeName, T insertData, Class<T> type) throws Exception {
    try (Connection conn = ds.getConnection()) {
      String tableName = "insertAndSelect" + table.incrementAndGet();
      get10(conn.rowCountOperation("create table " + tableName + "(t " + dataTypeName + ")")
          .submit().getCompletionStage());
      get10(conn.rowCountOperation("insert into " + tableName + "(t) values($1)")
          .set("$1", insertData).submit().getCompletionStage());
      CompletionStage<T> idF = conn.<T>rowOperation("select t from " + tableName)
          .collect(singleCollector(type))
          .submit()
          .getCompletionStage();
      get10(conn.rowCountOperation("drop table " + tableName).submit().getCompletionStage());

      if (type.isArray()) {
        assertArrayEquals((byte[])insertData, (byte[])get10(idF));
      } else {
        assertEquals(insertData, get10(idF));
      }
    }
  }

  @ParameterizedTest
  @MethodSource("data")
  public <T> void insertAndSelectWithTypeHinting(String dataTypeName, T insertData, Class<T> type, PgAdbaType adbaType)
      throws Exception {
    try (Connection conn = ds.getConnection()) {
      String tableName = "insertAndSelect" + table.incrementAndGet();
      get10(conn.rowCountOperation("create table " + tableName + "(t " + dataTypeName + ")")
          .submit().getCompletionStage());
      get10(conn.rowCountOperation("insert into " + tableName + "(t) values($1)")
          .set("$1", insertData, adbaType).submit().getCompletionStage());
      CompletionStage<T> idF = conn.<T>rowOperation("select t from " + tableName)
          .collect(singleCollector(type))
          .submit()
          .getCompletionStage();
      get10(conn.rowCountOperation("drop table " + tableName).submit().getCompletionStage());

      if (type.isArray()) {
        assertArrayEquals((byte[])insertData, (byte[])get10(idF));
      } else {
        assertEquals(insertData, get10(idF));
      }
    }
  }

  @ParameterizedTest
  @MethodSource("data")
  public <T> void insertAndSelectArray(String dataTypeName, T insertData, Class<T> type, PgAdbaType adbaType,
      Class<T[]> arrayType, T[] testArrayData) throws Exception {
    try (Connection conn = ds.getConnection()) {
      String tableName = "insertAndSelect" + table.incrementAndGet();
      get10(conn.rowCountOperation("create table " + tableName + "(t " + dataTypeName + "[])")
          .submit().getCompletionStage());
      get10(conn.rowCountOperation("insert into " + tableName + "(t) values($1)")
          .set("$1", testArrayData).submit().getCompletionStage());
      CompletionStage<T[]> idF = conn.<T[]>rowOperation("select t from " + tableName)
          .collect(singleCollector(arrayType))
          .submit()
          .getCompletionStage();
      get10(conn.rowCountOperation("drop table " + tableName).submit().getCompletionStage());

      T[] result = get10(idF);
      assertArrayEquals(testArrayData, result);
    }
  }

}
