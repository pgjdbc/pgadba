package org.postgresql.sql2.communication.packets.parts;

public enum FormatCodeTypes {
  TEXT((short)0),
  BINARY((short)1);

  private short code;

  FormatCodeTypes(short code) {
    this.code = code;
  }

  public static FormatCodeTypes lookup(short code) {
    for(FormatCodeTypes type : values()) {
      if(code == type.code) {
        return type;
      }
    }

    throw new IllegalArgumentException("no FormatType with code: " + code);
  }

  public short getCode() {
    return code;
  }
}
