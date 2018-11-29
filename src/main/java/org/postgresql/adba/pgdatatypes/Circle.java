package org.postgresql.adba.pgdatatypes;

import java.util.Objects;

public class Circle implements Comparable<Circle> {
  private double mx;
  private double my;
  private double radius;

  /**
   * Describes a circle in a 2-d plane.
   *
   * @param x x coordinate of middle
   * @param y y coordinate of middle
   * @param radius radius of circle
   */
  public Circle(double x, double y, double radius) {
    this.mx = x;
    this.my = y;
    this.radius = radius;
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

  public double getRadius() {
    return radius;
  }

  public void setRadius(double radius) {
    this.radius = radius;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Circle circle = (Circle) o;
    return Double.compare(circle.mx, mx) == 0
        && Double.compare(circle.my, my) == 0
        && Double.compare(circle.radius, radius) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(mx, my, radius);
  }

  @Override
  public int compareTo(Circle circle) {
    int c = Double.compare(circle.mx, mx);

    if (c != 0) {
      return c;
    }

    c = Double.compare(circle.my, my);

    if (c != 0) {
      return c;
    }

    return Double.compare(circle.radius, radius);
  }
}
