package org.postgresql.sql2.testutil;

import jdk.incubator.sql2.Result;
import org.postgresql.sql2.util.PgCount;

import java.util.List;
import java.util.stream.Collector;

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
}
