package org.postgresql.sql2.communication.packets;

import java.io.IOException;

import org.postgresql.sql2.communication.NetworkInputStream;
import org.postgresql.sql2.communication.NetworkReadContext;

public class AuthenticationRequest {
  public enum Types {
    SUCCESS(0), KERBEROS_V5(2), CLEAR_TEXT(3), MD5(5), SCM_CREDENTIAL(6), GSS(7), GSS_CONTINUE(8), SSPI(9), SASL(10),
    SASL_CONTINUE(11), SASL_FINAL(12);

    private int value;

    Types(int value) {
      this.value = value;
    }

    /**
     * find the corresponding type for the incoming integer value.
     * 
     * @param input integer value to search for
     * @return the corresponding type
     */
    public static Types lookup(int input) {
      for (Types t : values()) {
        if (t.value == input) {
          return t;
        }
      }

      throw new IllegalArgumentException("unknown authentication packet tag: " + input);
    }
  }

  private Types type;
  private byte[] salt = new byte[4];

  public AuthenticationRequest(NetworkReadContext context) throws IOException {
    NetworkInputStream input = context.getPayload();
    type = Types.lookup(input.readInteger());
    if (type == Types.MD5) {
      salt[0] = (byte) input.read();
      salt[1] = (byte) input.read();
      salt[2] = (byte) input.read();
      salt[3] = (byte) input.read();
    }
  }

  public byte[] getSalt() {
    return salt;
  }

  public Types getType() {
    return type;
  }
}
