package org.postgresql.sql2.util;

import org.postgresql.sql2.communication.packets.parts.ColumnDescription;
import org.postgresql.sql2.operations.helpers.ParameterHolder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class PreparedStatementCache {
  private class StatementKey {
    private final String sql;
    private final List<Integer> params;

    private StatementKey(String sql, List<Integer> params) {
      this.sql = sql;
      this.params = params;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      StatementKey that = (StatementKey) o;
      return Objects.equals(sql, that.sql) && Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {

      return Objects.hash(sql, params);
    }
  }

  private Map<StatementKey, String> sqlToName = new ConcurrentHashMap<>();
  private Map<StatementKey, String> sqlToPortalName = new ConcurrentHashMap<>();
  private Map<String, ColumnDescription[]> nameToDescription = new ConcurrentHashMap<>();
  private AtomicInteger names = new AtomicInteger(0);

  public String getNameForQuery(String sql, List<Integer> params) {
    if(sql == null)
      return null;

    StatementKey sk = new StatementKey(sql, params);

    return sqlToName.computeIfAbsent(sk, key -> "q" + names.incrementAndGet());
  }

  public String getNameForPortal(String sql, List<Integer> params) {
    if(sql == null)
      return null;

    StatementKey sk = new StatementKey(sql, params);

    return sqlToPortalName.computeIfAbsent(sk, key -> "p" + names.incrementAndGet());
  }


  public void addDescriptionToPortal(String portalName, ColumnDescription[] columnDescriptions) {
    nameToDescription.put(portalName, columnDescriptions);
  }

  public ColumnDescription[] getDescription(String portalName) {
    return nameToDescription.get(portalName);
  }

  public boolean sqlNotPreparedBefore(ParameterHolder holder, String sql) throws ExecutionException, InterruptedException {
    return !sqlToName.containsKey(new StatementKey(sql, holder.getParamTypes()));
  }
}
