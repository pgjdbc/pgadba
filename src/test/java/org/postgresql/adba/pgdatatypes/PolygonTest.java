package org.postgresql.adba.pgdatatypes;

import static org.junit.Assert.*;

import org.junit.Test;

public class PolygonTest {

  @Test
  public void equals() {
    Polygon p1 = new Polygon(new Point(1, 2));
    Polygon p2 = new Polygon(new Point(1, 2));

    assertEquals(p1, p2);
  }
}