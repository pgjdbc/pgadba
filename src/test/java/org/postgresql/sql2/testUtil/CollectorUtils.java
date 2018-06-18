package org.postgresql.sql2.testUtil;

import jdk.incubator.sql2.Result;
import org.postgresql.sql2.util.PGCount;

import java.util.stream.Collector;

public class CollectorUtils {
  public static<T> Collector<Result.Row, T[], T> singleCollector(Class<T> clazz) {
    return Collector.of(
        () -> (T[])new Object[1],
        (a, r) -> a[0] = r.get("t", clazz),
        (l, r) -> null,
        a -> a[0]);
  }

  public static Collector<Integer, Integer[], Integer> summingCollector() {
    return Collector.of(
        () -> new Integer[] {0},
        (a, r) -> a[0] += r,
        (l, r) -> null,
        a -> a[0]);
  }

  public static Collector<PGCount, ?, Integer> summingCountCollector() {
    return Collector.of(
        () -> new Integer[] {0},
        (a, r) -> a[0] += (int)r.getCount(),
        (l, r) -> null,
        a -> a[0]);
  }
}
