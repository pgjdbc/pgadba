package org.postgresql.adba.pgdatatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DiscreteRangeTest {

  private static Object[][] equalsData() {
    return new Object[][]{
        {new LongRange(1L, 2L, true, false),
            new LongRange(1L, 2L, true, false)},
        {new IntegerRange(1, 2, true, false),
            new IntegerRange(1, 2, true, false)}};
  }

  @ParameterizedTest
  @MethodSource("equalsData")
  public void equals(Object l1, Object l2) {
    assertEquals(l1, l2);
  }

  private static Object[][] equalsCanonicalData() {
    return new Object[][]{
        {new LongRange(1L, 1L, true, true),
            new LongRange(1L, 2L, true, false)},
        {new IntegerRange(1, 1, true, true),
            new IntegerRange(1, 2, true, false)}};
  }

  @ParameterizedTest
  @MethodSource("equalsCanonicalData")
  public void equalsCanonical(Object l1, Object l2) {
    assertEquals(l1, l2);
  }

  private static Object[][] equalsCanonicalWithInfiniteLowerData() {
    return new Object[][]{
        {new LongRange(null, 1L, true, true),
            new LongRange(null, 2L, false, false)},
        {new IntegerRange(null, 1, true, true),
            new IntegerRange(null, 2, false, false)}};
  }

  @ParameterizedTest
  @MethodSource("equalsCanonicalWithInfiniteLowerData")
  public void equalsCanonicalWithInfiniteLower(Object l1, Object l2) {
    assertEquals(l1, l2);
  }

  private static Object[][] equalsCanonicalWithInfiniteLower2Data() {
    return new Object[][]{
        {new LongRange(null, 1L, true, true),
            new LongRange(null, 1L, false, true)},
        {new IntegerRange(null, 1, true, true),
            new IntegerRange(null, 1, false, true)}};
  }

  @ParameterizedTest
  @MethodSource("equalsCanonicalWithInfiniteLower2Data")
  public void equalsCanonicalWithInfiniteLower2(Object l1, Object l2) {
    assertEquals(l1, l2);
  }

  private static Object[][] equalsCanonicalWithInfiniteUpperData() {
    return new Object[][]{
        {new LongRange(1L, null, true, true),
            new LongRange(2L, null, false, false)},
        {new IntegerRange(1, null, true, true),
            new IntegerRange(2, null, false, false)}};
  }

  @ParameterizedTest
  @MethodSource("equalsCanonicalWithInfiniteUpperData")
  public void equalsCanonicalWithInfiniteUpper(Object l1, Object l2) {
    assertEquals(l1, l2);
  }

  private static Object[][] equalsCanonicalWithInfiniteUpper2Data() {
    return new Object[][]{
        {new LongRange(1L, null, true, true),
            new LongRange(1L, null, true, false)},
        {new IntegerRange(1, null, true, true),
            new IntegerRange(1, null, true, false)}};
  }

  @ParameterizedTest
  @MethodSource("equalsCanonicalWithInfiniteUpper2Data")
  public void equalsCanonicalWithInfiniteUpper2(Object l1, Object l2) {
    assertEquals(l1, l2);
  }

  private static Object[][] equalsCanonicalWithInfiniteLowerUpperData() {
    return new Object[][]{
        {new LongRange(null, null, true, true),
            new LongRange(null, null, false, false)},
        {new IntegerRange(null, null, true, true),
            new IntegerRange(null, null, false, false)}};
  }

  @ParameterizedTest
  @MethodSource("equalsCanonicalWithInfiniteLowerUpperData")
  public void equalsCanonicalWithInfiniteLowerUpper(Object l1, Object l2) {
    assertEquals(l1, l2);
  }

  private static Object[][] equalsCanonicalEmptyData() {
    return new Object[][]{
        {new LongRange(1L, 2L, true, false),
            new LongRange(2L, 3L, true, false)},
        {new IntegerRange(1, 2, true, false),
            new IntegerRange(2, 3, true, false)}};
  }

  @ParameterizedTest
  @MethodSource("equalsCanonicalEmptyData")
  public void equalsCanonicalEmpty(Object l1, Object l2) {
    assertEquals(l1, l2);
  }

  private static Object[][] equalsCanonicalEmpty2Data() {
    return new Object[][]{
        {new LongRange(1L, 2L, true, false),
            new LongRange()},
        {new IntegerRange(1, 2, true, false),
            new IntegerRange()}};
  }

  @ParameterizedTest
  @MethodSource("equalsCanonicalEmpty2Data")
  public void equalsCanonicalEmpty2(Object l1, Object l2) {
    assertEquals(l1, l2);
  }

  @Test
  public void throwsWhenRangeOutOfOrder() {
    assertThrows(RuntimeException.class, () -> new LongRange(2L, 1L, true, false));
  }

  @Test
  public void emptyLongRange() {
    LongRange l1 = new LongRange(1L, 2L, true, false);

    assertTrue(l1.isEmpty());
  }

  @Test
  public void emptyIntegerRange() {
    IntegerRange l1 = new IntegerRange(1, 2, true, false);

    assertTrue(l1.isEmpty());
  }
}