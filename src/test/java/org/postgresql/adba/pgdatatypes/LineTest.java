package org.postgresql.adba.pgdatatypes;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class LineTest {

  @Test
  public void equals() {
    Line l1 = new Line(1, 2, 3);
    Line l2 = new Line(1, 2, 3);

    assertEquals(l1, l2);
  }
}