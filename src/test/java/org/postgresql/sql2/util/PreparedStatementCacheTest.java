package org.postgresql.sql2.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class PreparedStatementCacheTest {

  @Test
  public void getNameForQuery() {
    PreparedStatementCache cache = new PreparedStatementCache();

    assertEquals("p1", cache.getNameForQuery("select 1", Arrays.asList(1, 2)));
    assertEquals("p1", cache.getNameForQuery("select 1", Arrays.asList(1, 2)));
  }

  @Test
  public void getNameForQueryNull() {
    PreparedStatementCache cache = new PreparedStatementCache();

    assertNull(cache.getNameForQuery(null, Arrays.asList(1, 2)));
  }
}