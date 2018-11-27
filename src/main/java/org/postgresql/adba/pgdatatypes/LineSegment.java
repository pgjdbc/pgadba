package org.postgresql.adba.pgdatatypes;

import java.util.Objects;

public class LineSegment implements Comparable<LineSegment> {
  private double x1;
  private double y1;
  private double x2;
  private double y2;

  /**
   * Represent a line segment between two points.
   *
   * @param x1 first point x
   * @param y1 first point y
   * @param x2 second point x
   * @param y2 second point y
   */
  public LineSegment(double x1, double y1, double x2, double y2) {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
  }

  public double getX1() {
    return x1;
  }

  public void setX1(double x1) {
    this.x1 = x1;
  }

  public double getY1() {
    return y1;
  }

  public void setY1(double y1) {
    this.y1 = y1;
  }

  public double getX2() {
    return x2;
  }

  public void setX2(double x2) {
    this.x2 = x2;
  }

  public double getY2() {
    return y2;
  }

  public void setY2(double y2) {
    this.y2 = y2;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LineSegment that = (LineSegment) o;
    return Double.compare(that.x1, x1) == 0
        && Double.compare(that.y1, y1) == 0
        && Double.compare(that.x2, x2) == 0
        && Double.compare(that.y2, y2) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x1, y1, x2, y2);
  }

  @Override
  public int compareTo(LineSegment ls) {
    int c = Double.compare(ls.x1, x1);

    if (c != 0) {
      return c;
    }

    c = Double.compare(ls.y1, y1);

    if (c != 0) {
      return c;
    }

    c = Double.compare(ls.x2, x2);

    if (c != 0) {
      return c;
    }

    return Double.compare(ls.y2, y2);
  }
}
