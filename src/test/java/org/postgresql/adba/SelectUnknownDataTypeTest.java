package org.postgresql.adba;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.postgresql.adba.testutil.CollectorUtils.singleCollector;
import static org.postgresql.adba.testutil.FutureUtil.get10;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.postgresql.adba.testutil.ConnectUtil;
import org.postgresql.adba.testutil.DatabaseHolder;
import org.testcontainers.containers.PostgreSQLContainer;

public class SelectUnknownDataTypeTest {

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
   *
   * @return test data
   */
  public static Collection<Object[]> data() throws UnknownHostException {
    return Arrays.asList(new Object[][]{
        {"some string", "some string", String.class},
        {"123", 123, Integer.class},
        {"321", 321, int.class},
        {"123", (short) 123, Short.class},
        {"321", (short) 321, short.class},
        {"123", (byte) 123, Byte.class},
        {"21", (byte) 21, byte.class},
        {"123", (long) 123, Long.class},
        {"321", (long) 321, long.class},
        {"123.221", (float) 123.221, Float.class},
        {"321.112", (float) 321.112, float.class},
        {"123.221", 123.221, Double.class},
        {"321.112", 321.112, double.class},
        {"true", true, boolean.class},
        {"false", false, Boolean.class},
    });
  }

  /**
   * Data.
   *
   * @return test data
   */
  public static Collection<Object[]> badData() throws UnknownHostException {
    return Arrays.asList(new Object[][]{
        {"not a number", Integer.class},
        {"not a number", int.class},
        {"not a number", Short.class},
        {"not a number", short.class},
        {"not a number", Byte.class},
        {"not a number", byte.class},
        {"not a number", Long.class},
        {"not a number", long.class},
        {"not a number", Float.class},
        {"not a number", float.class},
        {"not a number", Double.class},
        {"not a number", double.class},
    });
  }

  @ParameterizedTest
  @MethodSource("data")
  public <T> void select(String stringValue, T realValue, Class<T> type) throws Exception {
    try (Session session = ds.getSession()) {
      CompletionStage<T> idF = session.<T>rowOperation("select '" + stringValue + "' as t")
          .collect(singleCollector(type))
          .submit()
          .getCompletionStage();

      assertEquals(realValue, get10(idF));
    }
  }

  @ParameterizedTest
  @MethodSource("badData")
  public <T> void selectFailure(String brokenValue, Class<T> type) throws Exception {
    try (Session session = ds.getSession()) {
      CompletionStage<T> idF = session.<T>rowOperation("select '" + brokenValue + "' as t")
          .collect(singleCollector(type))
          .submit()
          .getCompletionStage();

      assertThrows(ExecutionException.class, () -> get10(idF));
    }
  }

}
