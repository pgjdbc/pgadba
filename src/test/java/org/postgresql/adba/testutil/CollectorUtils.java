package org.postgresql.adba.testutil;

import java.util.List;
import java.util.stream.Collector;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.SqlType;
import org.postgresql.adba.util.PgCount;

public class CollectorUtils {

  /**
   * Returns first element that was collected.
   * @param clazz type of element to fetch
   * @param <T> returning type
   * @return a collector
   */
  public static <T> Collector<Result.RowColumn, T[], T> singleCollector(Class<T> clazz) {
    return Collector.of(
        () -> (T[])new Object[1],
        (a, r) -> a[0] = r.at("t").get(clazz),
        (l, r) -> null,
        a -> a[0]);
  }

  /**
   * Returns sum of elements that was collected.
   * @return a collector
   */
  public static Collector<Integer, Integer[], Integer> summingCollector() {
    return Collector.of(
        () -> new Integer[] {0},
        (a, r) -> a[0] += r,
        (l, r) -> null,
        a -> a[0]);
  }

  /**
   * Returns a sum of the affected rows that was collected.
   * @return a collector
   */
  public static Collector<PgCount, ?, Integer> summingCountCollector() {
    return Collector.of(
        () -> new Integer[] {0},
        (a, r) -> a[0] += (int)r.getCount(),
        (l, r) -> null,
        a -> a[0]);
  }

  /**
   * Collector.
   * @return a collector
   */
  public static Collector<List<PgCount>, ?, Integer> summingCountListCollector() {
    return Collector.of(
        () -> new Integer[] {0},
        (a, r) -> r.stream().forEach(c -> a[0] += (int)c.getCount()),
        (l, r) -> null,
        a -> a[0]);
  }

  /**
   * Returns the java type of the column named "t".
   * @param clazz type of element to fetch
   * @param <T> returning type
   * @return a collector
   */
  public static <T> Collector<Result.RowColumn, Class<T>[], Class<T>> javaTypeCollector(Class<T> clazz) {
    return Collector.of(
        () -> new Class[1],
        (a, r) -> a[0] = r.at("t").javaType(),
        (l, r) -> null,
        a -> a[0]);
  }

  /**
   * Returns the sql type of the column named "t".
   * @return a collector
   */
  public static Collector<Result.RowColumn, SqlType[], SqlType> adbaTypeCollector() {
    return Collector.of(
        () -> new SqlType[1],
        (a, r) -> a[0] = r.at("t").sqlType(),
        (l, r) -> null,
        a -> a[0]);
  }
}
