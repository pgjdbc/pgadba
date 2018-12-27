package org.postgresql.adba.pgdatatypes;

import java.util.Objects;

public class LongRange implements Comparable<LongRange> {
  private boolean empty;
  private Long lower;
  private Long upper;
  private boolean lowerInclusive;
  private boolean upperInclusive;

  /**
   * creates an empty range.
   */
  public LongRange() {
    empty = true;
  }

  /**
   * Creates an range, if the boundaries are next to each other, then the empty flag will be set.
   *
   * @param lower lower bound
   * @param upper upper bound
   * @param lowerInclusive if lower is inclusive, '[' in postgresql syntax
   * @param upperInclusive if upper is inclusive, ']' in postgresql syntax
   */
  public LongRange(Long lower, Long upper, boolean lowerInclusive, boolean upperInclusive) {
    this.lower = lower;
    this.upper = upper;
    this.lowerInclusive = lowerInclusive;
    this.upperInclusive = upperInclusive;

    canonicalize();
  }

  private void canonicalize() {
    if (!lowerInclusive) {
      lowerInclusive = true;
      if (lower != null) {
        lower--;
      }
    }
    if (upperInclusive) {
      upperInclusive = false;
      if (upper != null) {
        upper++;
      }
    }

    if (lower != null && upper != null && lower > upper) {
      throw new IllegalArgumentException("range lower bound must be less than or equal to range upper bound");
    }

    empty = lower != null && upper != null && lower.equals(upper - 1);
  }

  public boolean isEmpty() {
    return empty;
  }

  public Long getLower() {
    return lower;
  }

  /**
   * sets the lower bound, and then runs the canonicalization function.
   *
   * @param lower sets the lower bound, must be lower or equal to the upper bound
   */
  public void setLower(Long lower) {
    this.lower = lower;

    canonicalize();
  }

  public Long getUpper() {
    return upper;
  }

  /**
   * sets the upper bound, and then runs the canonicalization function.
   *
   * @param upper sets the lower bound, must be higher or equal to the lower bound
   */
  public void setUpper(Long upper) {
    this.upper = upper;

    canonicalize();
  }

  public boolean isLowerInclusive() {
    return lowerInclusive;
  }

  /**
   * sets whether the lower bound is inclusive or not, and then runs the canonicalization function.
   *
   * @param lowerInclusive sets the lower inclusive bound flag
   */
  public void setLowerInclusive(boolean lowerInclusive) {
    this.lowerInclusive = lowerInclusive;

    canonicalize();
  }

  public boolean isUpperInclusive() {
    return upperInclusive;
  }

  /**
   * sets whether the upper bound is inclusive or not, and then runs the canonicalization function.
   *
   * @param upperInclusive sets the upper inclusive bound flag
   */
  public void setUpperInclusive(boolean upperInclusive) {
    this.upperInclusive = upperInclusive;

    canonicalize();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LongRange longRange = (LongRange) o;
    if (empty && longRange.empty) {
      return true;
    }
    return lowerInclusive == longRange.lowerInclusive
        && upperInclusive == longRange.upperInclusive
        && Objects.equals(lower, longRange.lower)
        && Objects.equals(upper, longRange.upper);
  }

  @Override
  public int hashCode() {
    return Objects.hash(empty, lower, upper, lowerInclusive, upperInclusive);
  }

  @Override
  public int compareTo(LongRange lr) {
    if (empty && lr.empty) {
      return 0;
    }

    int c = Long.compare(lr.lower, lower);

    if (c != 0) {
      return c;
    }

    c = Long.compare(lr.upper, upper);

    if (c != 0) {
      return c;
    }

    c = Boolean.compare(lr.lowerInclusive, lowerInclusive);

    if (c != 0) {
      return c;
    }

    return Boolean.compare(lr.upperInclusive, upperInclusive);
  }

  @Override
  public String toString() {
    if (empty) {
      return "empty";
    }

    return (lowerInclusive ? "[" : "(")
        + (lower == null ? "" : Long.toString(lower)) + ","
        + (upper == null ? "" : Long.toString(upper))
        + (upperInclusive ? "]" : ")");
  }
}
