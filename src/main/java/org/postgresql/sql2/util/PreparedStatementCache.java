package org.postgresql.sql2.util;

import org.postgresql.sql2.communication.packets.parts.ColumnDescription;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PreparedStatementCache {
  private Map<String, String> sqlToName = new ConcurrentHashMap<>();
  private Map<String, ColumnDescription[]> nameToDescription = new ConcurrentHashMap<>();
  private AtomicInteger names = new AtomicInteger(0);

  public String getNameForQuery(String sql) {
    if(sql == null)
      return null;

    return sqlToName.computeIfAbsent(sql, key -> "p" + names.incrementAndGet());
  }

  public void addDescriptionToPortal(String portalName, ColumnDescription[] columnDescriptions) {
    nameToDescription.put(portalName, columnDescriptions);
  }

  public ColumnDescription[] getDescription(String portalName) {
    return nameToDescription.get(portalName);
  }
}
