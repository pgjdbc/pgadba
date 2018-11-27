package org.postgresql.adba.pgdatatypes;

import static org.junit.Assert.*;

import org.junit.Test;

public class BoxTest {
  @Test
  public void equals() {
    Box b1 = new Box(1, 2, 3, 4);
    Box b2 = new Box(1, 2, 3, 4);

    assertEquals(b1, b2);
  }
}