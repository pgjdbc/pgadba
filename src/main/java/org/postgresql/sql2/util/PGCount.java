package org.postgresql.sql2.util;

import jdk.incubator.sql2.Result;

import java.util.Objects;

public class PGCount implements Result.RowCount {
  private long count;

  public PGCount(long count) {
    this.count = count;
  }

  @Override
  public long getCount() {
    return count;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;

    if (o == null || getClass() != o.getClass())
      return false;

    PGCount pgCount = (PGCount) o;
    return count == pgCount.count;
  }

  @Override
  public int hashCode() {
    return Objects.hash(count);
  }
}
