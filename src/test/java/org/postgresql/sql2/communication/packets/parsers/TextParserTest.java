package org.postgresql.sql2.communication.packets.parsers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

public class TextParserTest {
  @Test
  public void textArrayOut() {
    Object[] result = (Object[])TextParser.textArrayOut("{first,\"\\\"second\",\"th,ird\"}", String[].class);

    assertArrayEquals(new String[] {"first", "\"second", "th,ird"}, result);
  }
}
