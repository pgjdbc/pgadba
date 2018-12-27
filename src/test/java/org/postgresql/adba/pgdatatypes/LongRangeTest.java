package org.postgresql.adba.pgdatatypes;

import static org.junit.Assert.*;

import org.junit.Test;

public class LongRangeTest {

  @Test
  public void equals() {
    LongRange l1 = new LongRange(1L, 2L, true, false);
    LongRange l2 = new LongRange(1L, 2L, true, false);

    assertEquals(l1, l2);
  }

  @Test
  public void equalsCanonical() {
    LongRange l1 = new LongRange(1L, 1L, true, true);
    LongRange l2 = new LongRange(1L, 2L, true, false);

    assertEquals(l1, l2);
  }

  @Test
  public void equalsCanonicalWithInfiniteLower() {
    LongRange l1 = new LongRange(null, 1L, true, true);
    LongRange l2 = new LongRange(null, 2L, false, false);

    assertEquals(l1, l2);
  }

  @Test
  public void equalsCanonicalWithInfiniteLower2() {
    LongRange l1 = new LongRange(null, 1L, true, true);
    LongRange l2 = new LongRange(null, 1L, false, true);

    assertEquals(l1, l2);
  }

  @Test
  public void equalsCanonicalWithInfiniteUpper() {
    LongRange l1 = new LongRange(1L, null, true, true);
    LongRange l2 = new LongRange(2L, null, false, false);

    assertEquals(l1, l2);
  }

  @Test
  public void equalsCanonicalWithInfiniteUpper2() {
    LongRange l1 = new LongRange(1L, null, true, true);
    LongRange l2 = new LongRange(1L, null, true, false);

    assertEquals(l1, l2);
  }

  @Test
  public void equalsCanonicalWithInfiniteLowerUpper() {
    LongRange l1 = new LongRange(null, null, true, true);
    LongRange l2 = new LongRange(null, null, false, false);

    assertEquals(l1, l2);
  }

  @Test
  public void equalsCanonicalEmpty() {
    LongRange l1 = new LongRange(1L, 2L, true, false);
    LongRange l2 = new LongRange(2L, 3L, true, false);

    assertEquals(l1, l2);
  }

  @Test
  public void equalsCanonicalEmpty2() {
    LongRange l1 = new LongRange(1L, 2L, true, false);
    LongRange l2 = new LongRange();

    assertEquals(l1, l2);
  }

  @Test
  public void empty() {
    LongRange l1 = new LongRange(1L, 2L, true, false);

    assertTrue(l1.isEmpty());
  }
}