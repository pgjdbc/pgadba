package org.postgresql.adba.communication.packets;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.postgresql.adba.util.BinaryHelper;

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

    /**
     * find the corresponding type for the incoming integer value.
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

  public enum ScramMechanism {
    SCRAM_SHA_256("SCRAM-SHA-256");

    private String value;

    ScramMechanism(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    /**
     * find the corresponding type for the incoming string value.
     * @param input string value to search for
     * @return the corresponding type
     */
    public static ScramMechanism lookup(String input) {
      for (ScramMechanism t : values()) {
        if (Objects.equals(t.value, input)) {
          return t;
        }
      }

      throw new IllegalArgumentException("unknown authentication packet tag: " + input);
    }
  }

  private Types type;
  private byte[] salt = new byte[4];
  private List<ScramMechanism> scramMechanisms = new ArrayList<>();
  private String saslContinueMessage;
  private String saslFinalMessage;

  /**
   * describes the authentication type and salt.
   * @param bytes incoming bytes
   */
  public AuthenticationRequest(byte[] bytes) {
    type = Types.lookup(BinaryHelper.readInt(bytes[0], bytes[1], bytes[2], bytes[3]));
    if (type == Types.MD5) {
      salt[0] = bytes[4];
      salt[1] = bytes[5];
      salt[2] = bytes[6];
      salt[3] = bytes[7];
    } else if (type == Types.SASL) {
      String mechanisms = new String(BinaryHelper.subBytes(bytes, 4, bytes.length - 2),
          StandardCharsets.UTF_8);

      for (String mechanism : mechanisms.split(",")) {
        scramMechanisms.add(ScramMechanism.lookup(mechanism));
      }
    } else if (type == Types.SASL_CONTINUE) {
      saslContinueMessage = new String(BinaryHelper.subBytes(bytes, 4, bytes.length), StandardCharsets.UTF_8);
    } else if (type == Types.SASL_FINAL) {
      saslFinalMessage = new String(BinaryHelper.subBytes(bytes, 4, bytes.length), StandardCharsets.UTF_8);
    }
  }

  public byte[] getSalt() {
    return salt;
  }

  public Types getType() {
    return type;
  }

  public List<ScramMechanism> getScramMechanisms() {
    return scramMechanisms;
  }

  public String getSaslContinueMessage() {
    return saslContinueMessage;
  }

  public String getSaslFinalMessage() {
    return saslFinalMessage;
  }
}
