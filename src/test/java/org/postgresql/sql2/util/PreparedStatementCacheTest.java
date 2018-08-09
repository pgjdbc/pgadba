package org.postgresql.sql2.util;

import org.junit.jupiter.api.Test;
import org.postgresql.sql2.communication.PreparedStatementCache;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PreparedStatementCacheTest {

  @Test
  public void getNameForQuery() {
    PreparedStatementCache cache = new PreparedStatementCache();

    assertEquals("q1", cache.getQuery("select 1", Arrays.asList(1, 2)).getQueryName());
    assertEquals("q1", cache.getQuery("select 1", Arrays.asList(1, 2)).getQueryName());
  }

  @Test
  public void getNameForQueryNull() {
    PreparedStatementCache cache = new PreparedStatementCache();

    assertNull(cache.getQuery(null, Arrays.asList(1, 2)));
  }
}