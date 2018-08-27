package org.postgresql.sql2.communication.packets.parts;

public enum FormatCodeTypes {
  TEXT((short)0),
  BINARY((short)1);

  private short code;

  FormatCodeTypes(short code) {
    this.code = code;
  }

  /**
   * Finds the format code for the short value.
   * @param code either 0 or 1 is allowed
   * @return returns TEXT or BINARY
   */
  public static FormatCodeTypes lookup(short code) {
    for (FormatCodeTypes type : values()) {
      if (code == type.code) {
        return type;
      }
    }

    throw new IllegalArgumentException("no FormatType with code: " + code);
  }

  public short getCode() {
    return code;
  }
}
