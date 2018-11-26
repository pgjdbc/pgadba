package org.postgresql.adba.pgdatatypes;

import java.util.Objects;

public class Point implements Comparable<Point> {
  private double mx;
  private double my;

  public Point(double x, double y) {
    this.mx = x;
    this.my = y;
  }

  public double getX() {
    return mx;
  }

  public void setX(double x) {
    this.mx = x;
  }

  public double getY() {
    return my;
  }

  public void setY(double y) {
    this.my = y;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Point point = (Point) o;
    return Double.compare(point.mx, mx) == 0
        && Double.compare(point.my, my) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(mx, my);
  }

  @Override
  public int compareTo(Point p2) {
    int c = Double.compare(mx, p2.mx);

    if (c != 0) {
      return c;
    }

    return Double.compare(my, p2.my);
  }
}
