package org.postgresql.sql2.communication.packets;

import org.postgresql.sql2.util.BinaryHelper;

public class AuthenticationRequest {
  public enum Types {
    SUCCESS(0),
    KERBEROS_V5(2),
    CLEAR_TEXT(3),
    MD5(5),
    SCM_CREDENTIAL(6),
    GSS(7),
    GSS_CONTINUE(8),
    SSPI(9),
    SASL(10),
    SASL_CONTINUE(11),
    SASL_FINAL(12);

    private int value;

    Types(int value) {
      this.value = value;
    }

    public static Types lookup(int input) {
      for(Types t : values()) {
        if(t.value == input)
          return t;
      }

      throw new IllegalArgumentException("unknown authentication packet tag: " + input);
    }
  }

  private Types type;
  private byte[] salt = new byte[4];

  public AuthenticationRequest(byte[] bytes) {
    type = Types.lookup(BinaryHelper.readInt(bytes[0], bytes[1], bytes[2], bytes[3]));
    salt[0] = bytes[4];
    salt[1] = bytes[5];
    salt[2] = bytes[6];
    salt[3] = bytes[7];
  }

  public byte[] getSalt() {
    return salt;
  }

  public Types getType() {
    return type;
  }
}
