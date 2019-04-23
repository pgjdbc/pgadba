package org.postgresql.adba.pgdatatypes;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class LineSegmentTest {
  @Test
  public void equals() {
    LineSegment ls1 = new LineSegment(1, 2, 3, 4);
    LineSegment ls2 = new LineSegment(1, 2, 3, 4);

    assertEquals(ls1, ls2);
  }
}