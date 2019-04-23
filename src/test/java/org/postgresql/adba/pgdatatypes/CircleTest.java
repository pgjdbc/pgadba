package org.postgresql.adba.pgdatatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CircleTest {

  @Test
  public void equals() {
    Circle c1 = new Circle(1, 2, 3);
    Circle c2 = new Circle(1, 2, 3);

    assertEquals(c1, c2);
  }
}