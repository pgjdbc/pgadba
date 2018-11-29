package org.postgresql.adba.pgdatatypes;

import static org.junit.Assert.*;

import org.junit.Test;

public class PathTest {

  @Test
  public void equals() {
    Path p1 = new Path(true, new Point(1, 2));
    Path p2 = new Path(true, new Point(1, 2));

    assertEquals(p1, p2);
  }

  @Test
  public void equalsFalse1() {
    Path p1 = new Path(true, new Point(1, 2));
    Path p2 = new Path(false, new Point(1, 2));

    assertNotEquals(p1, p2);
  }

  @Test
  public void equalsFalse2() {
    Path p1 = new Path(true, new Point(1, 2));
    Path p2 = new Path(true, new Point(2, 1));

    assertNotEquals(p1, p2);
  }

  @Test
  public void equalsFalse3() {
    Path p1 = new Path(true, new Point(1, 2));
    Path p2 = new Path(true, new Point(1, 2), new Point(1, 2));

    assertNotEquals(p1, p2);
  }
}