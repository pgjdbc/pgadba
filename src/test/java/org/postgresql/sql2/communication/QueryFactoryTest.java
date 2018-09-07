package org.postgresql.sql2.communication;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.postgresql.sql2.PgSubmission;
import org.postgresql.sql2.communication.network.Query;
import org.postgresql.sql2.operations.helpers.ParameterHolder;
import org.postgresql.sql2.submissions.RowSubmission;

/**
 * Tests the {@link QueryFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class QueryFactoryTest {

  private final QueryFactory queryFactory = new QueryFactory();

  @Test
  public void ensureDifferentQueriesNotSameReuse() throws Exception {
    Query queryOne = this.queryFactory.createQuery(createSubmission("SELECT 1 as t"));
    Query queryTwo = this.queryFactory.createQuery(createSubmission("SELECT 2 as t"));
    assertNotSame("Should not re-use as different queries", queryOne.getReuse(), queryTwo.getReuse());
  }

  @Test
  public void ensureSameQueryReuse() throws Exception {
    Query queryOne = this.queryFactory.createQuery(createSubmission("SELECT 1 as t"));
    Query queryTwo = this.queryFactory.createQuery(createSubmission("SELECT 1 as t"));
    assertSame("Should re-use as same queries", queryOne.getReuse(), queryTwo.getReuse());
  }

  private static PgSubmission<?> createSubmission(String sql) {
    ParameterHolder holder = new ParameterHolder();
    return new RowSubmission<>(() -> Boolean.FALSE, (ex) -> {
    }, holder, null, sql);
  }

}