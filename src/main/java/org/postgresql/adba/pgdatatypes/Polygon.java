package org.postgresql.adba.pgdatatypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Polygon implements Comparable<Polygon> {
  private List<Point> points;

  /**
   * Initializes the Polygon from a list of points.
   *
   * @param points not allowed to be null
   */
  public Polygon(List<Point> points) {
    if (points == null) {
      throw new RuntimeException("point list not allowed to be null");
    }
    this.points = points;
  }

  /**
   * Initializes the Polygon from a list of points.
   *
   * @param points not allowed to be null
   */
  public Polygon(Point... points) {
    if (points == null) {
      throw new RuntimeException("point list not allowed to be null");
    }
    this.points = new ArrayList<>();
    this.points.addAll(Arrays.asList(points));
  }

  public List<Point> getPoints() {
    return points;
  }

  /**
   * Sets the points that this Path consists of.
   *
   * @param points not allowed to be null
   */
  public void setPoints(List<Point> points) {
    if (points == null) {
      throw new RuntimeException("point list not allowed to be null");
    }
    this.points = points;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Polygon polygon = (Polygon) o;
    return Objects.equals(points, polygon.points);
  }

  @Override
  public int hashCode() {
    return Objects.hash(points);
  }

  @Override
  public int compareTo(Polygon p) {
    int c = Integer.compare(p.points.size(), points.size());

    if (c != 0) {
      return c;
    }

    for (int i = 0; i < points.size(); i++) {
      c = p.points.get(i).compareTo(points.get(i));

      if (c != 0) {
        return c;
      }
    }
    return 0;
  }
}
