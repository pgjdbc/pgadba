package org.postgresql.adba.pgdatatypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Path implements Comparable<Path> {
  private List<Point> points;
  private boolean closed;

  /**
   * Initializes the Path from a list of points.
   *
   * @param points not allowed to be null
   */
  public Path(boolean closed, List<Point> points) {
    if (points == null) {
      throw new RuntimeException("point list not allowed to be null");
    }
    this.points = points;
  }

  /**
   * Initializes the Path from a list of points.
   *
   * @param points not allowed to be null
   */
  public Path(boolean closed, Point... points) {
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

  public boolean isClosed() {
    return closed;
  }

  public void setClosed(boolean closed) {
    this.closed = closed;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Path path = (Path) o;
    return closed == path.closed
        && Objects.equals(points, path.points);
  }

  @Override
  public int hashCode() {
    return Objects.hash(points, closed);
  }

  @Override
  public int compareTo(Path p) {
    int c = Integer.compare(p.points.size(), points.size());

    if (c != 0) {
      return c;
    }

    c = Boolean.compare(p.closed, closed);

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
