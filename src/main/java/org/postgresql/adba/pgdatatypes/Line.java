package org.postgresql.adba.pgdatatypes;

import java.util.Objects;

public class Line implements Comparable<Line> {
  private double ma;
  private double mb;
  private double mc;

  /**
   * An infinite line on a plane, represented by Ax + By + C.
   *
   * @param a x axis
   * @param b y axis
   * @param c constant
   */
  public Line(double a, double b, double c) {
    this.ma = a;
    this.mb = b;
    this.mc = c;
  }

  public double getA() {
    return ma;
  }

  public void setA(double a) {
    this.ma = a;
  }

  public double getB() {
    return mb;
  }

  public void setB(double b) {
    this.mb = b;
  }

  public double getC() {
    return mc;
  }

  public void setC(double c) {
    this.mc = c;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Line line = (Line) o;
    return Double.compare(line.ma, ma) == 0
        && Double.compare(line.mb, mb) == 0
        && Double.compare(line.mc, mc) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ma, mb, mc);
  }

  @Override
  public int compareTo(Line line) {
    int c = Double.compare(line.ma, ma);

    if (c != 0) {
      return c;
    }

    c = Double.compare(line.mb, mb);

    if (c != 0) {
      return c;
    }

    return Double.compare(line.mc, mc);
  }
}
