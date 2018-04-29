package org.postgresql.sql2.util;

import jdk.incubator.sql2.Result;

public class PGCount implements Result.Count {
  private long count;

  public PGCount(long count) {
    this.count = count;
  }

  @Override
  public long getCount() {
    return count;
  }
}
