package org.postgresql.adba.communication.packets.parsers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

public class TextParserTest {
  @Test
  public void textArrayOut() {
    Object[] result = (Object[])TextParser.textArrayOut("{first,\"\\\"second\",\"th,ird\"}", String[].class);

    assertArrayEquals(new String[] {"first", "\"second", "th,ird"}, result);
  }

  @Test
  public void textArrayOutEmptyArray() {
    Object[] result = (Object[])TextParser.textArrayOut("{}", String[].class);

    assertArrayEquals(new String[] {}, result);
  }

  @Test
  public void textArrayOutArrayOfEmptyStringsOne() {
    Object[] result = (Object[])TextParser.textArrayOut("{\"\"}", String[].class);

    assertArrayEquals(new String[] {""}, result);
  }

  @Test
  public void textArrayOutArrayOfEmptyStringsTwo() {
    Object[] result = (Object[])TextParser.textArrayOut("{,}", String[].class);

    assertArrayEquals(new String[] {"", ""}, result);
  }

  @Test
  public void textArrayOutArrayOfEmptyStringsThree() {
    Object[] result = (Object[])TextParser.textArrayOut("{,,}", String[].class);

    assertArrayEquals(new String[] {"", "", ""}, result);
  }

  @Test
  public void textArrayOutArrayOfEmptyFull() {
    Object[] result = (Object[])TextParser.textArrayOut("{,a}", String[].class);

    assertArrayEquals(new String[] {"", "a"}, result);
  }

  @Test
  public void textArrayOutArrayOfFullEmpty() {
    Object[] result = (Object[])TextParser.textArrayOut("{a,}", String[].class);

    assertArrayEquals(new String[] {"a", ""}, result);
  }

  @Test
  public void textArrayOutWithUnicode() {
    Object[] result = (Object[])TextParser.textArrayOut("{a1,ö2}", String[].class);

    assertArrayEquals(new String[] {"a1", "ö2"}, result);
  }
}
