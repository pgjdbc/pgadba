package org.postgresql.sql2.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PreparedStatementCache {
  private Map<String, String> sqlToName = new ConcurrentHashMap<>();
  private AtomicInteger names = new AtomicInteger(0);

  public String getNameForQuery(String sql) {
    return sqlToName.computeIfAbsent(sql, key -> "p" + names.incrementAndGet());
  }
}
