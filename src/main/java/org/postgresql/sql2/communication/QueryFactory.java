package org.postgresql.sql2.communication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.postgresql.sql2.PgSubmission;
import org.postgresql.sql2.communication.network.Query;
import org.postgresql.sql2.communication.network.QueryReuse;

public class QueryFactory {

  /**
   * As only used on networking thread, is thread safe.
   */
  private Map<StatementKey, QueryReuse> sqlToReuse = new HashMap<>();

  /**
   * Obtains the {@link Query} for the SQL.
   * 
   * @param sql    SQL.
   * @param params Parameters.
   * @return {@link Query}.
   */
  public Query createQuery(PgSubmission<?> submission) throws InterruptedException, ExecutionException {

    // Obtain the details
    String sql = submission.getSql();
    List<Integer> parameters = submission.getParamTypes();
    if (sql == null) {
      throw new IllegalArgumentException("No SQL provided");
    }

    // Obtain or create the query re-use
    StatementKey key = new StatementKey(sql, parameters);
    QueryReuse reuse = this.sqlToReuse.computeIfAbsent(key, (absentKey) -> new QueryReuse());

    // Return the query
    return new Query(submission, reuse);
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
      return Objects.equals(sql, that.sql) ; //&& Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
      return Objects.hash(sql, params);
    }
  }

}