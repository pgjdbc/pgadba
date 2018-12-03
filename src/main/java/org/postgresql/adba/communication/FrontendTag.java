package org.postgresql.adba.communication;

public enum FrontendTag {
  BIND('B'),
  DESCRIBE('D'),
  EXECUTE('E'),
  PARSE('P'),
  PASSWORD_MESSAGE('p'),
  QUERY('Q'),
  SASL_INITIAL_RESPONSE('p'),
  SASL_RESPONSE('p'),
  SYNC('S'),
  TERMINATE('X');

  private char tag;

  FrontendTag(char tag) {
    this.tag = tag;
  }

  /**
   * Search for the correct FrontendTag based on byte value.
   * @param input the byte to search for
   * @return the corresponding FrontendTag
   */
  public static FrontendTag lookup(byte input) {
    for (FrontendTag bt : values()) {
      if (input == bt.tag) {
        return bt;
      }
    }
    throw new IllegalArgumentException("There is no backend server tag that matches byte " + input);
  }

  public byte getByte() {
    return (byte)tag;
  }
}
