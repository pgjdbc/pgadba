package org.postgresql.sql2.util;

import jdk.incubator.sql2.Result;

import java.util.Objects;

public class PgCount implements Result.RowCount {
  private long count;

  public PgCount(long count) {
    this.count = count;
  }

  @Override
  public long getCount() {
    return count;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PgCount pgCount = (PgCount) o;
    return count == pgCount.count;
  }

  @Override
  public int hashCode() {
    return Objects.hash(count);
  }
}
