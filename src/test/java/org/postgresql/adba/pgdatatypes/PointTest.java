package org.postgresql.adba.pgdatatypes;

import static org.junit.Assert.*;

import org.junit.Test;

public class PointTest {

  @Test
  public void equals() {
    Point p1 = new Point(1, 2);
    Point p2 = new Point(1, 2);

    assertEquals(p1, p2);
  }
}