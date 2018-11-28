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
}