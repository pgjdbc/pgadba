package org.postgresql.adba.communication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.postgresql.adba.communication.network.Query;

public class PreparedStatementCache {

  /**
   * As only used on networking thread, is thread safe.
   */
  private Map<StatementKey, Query> sqlToQuery = new HashMap<>();

  /**
   * Obtains the {@link Query} for the SQL.
   * 
   * @param sql    SQL.
   * @param params Parameters.
   * @return {@link Query}.
   */
  public Query getQuery(String sql, List<Integer> params) {
    if (sql == null) {
      throw new IllegalArgumentException("No SQL provided");
    }

    // Obtain or create the query
    return sqlToQuery.computeIfAbsent(new StatementKey(sql, params), key -> new Query());
  }

  private class StatementKey {
    private final String sql;
    private final List<Integer> params;

    private StatementKey(String sql, List<Integer> params) {
      this.sql = sql;
      this.params = params;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      StatementKey that = (StatementKey) o;
      return Objects.equals(sql, that.sql) && Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
      return Objects.hash(sql, params);
    }
  }

}
