package org.postgresql.sql2.util;

import org.junit.jupiter.api.Test;
import org.postgresql.sql2.communication.PreparedStatementCache;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PreparedStatementCacheTest {

  @Test
  public void getNameForQuery() {
    PreparedStatementCache cache = new PreparedStatementCache();

    String name = cache.getQuery("select 1", Arrays.asList(1, 2)).getQueryName();
    assertEquals(name, cache.getQuery("select 1", Arrays.asList(1, 2)).getQueryName());
    assertEquals(name, cache.getQuery("select 1", Arrays.asList(1, 2)).getQueryName());
  }

  @Test
  public void getNameForQueryNull() {
    PreparedStatementCache cache = new PreparedStatementCache();

    assertThrows(IllegalArgumentException.class, () -> cache.getQuery(null, Arrays.asList(1, 2)));
  }
}