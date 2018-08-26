package org.postgresql.sql2.communication;

import org.postgresql.sql2.util.BinaryHelper;

import java.nio.ByteBuffer;

@Deprecated // write directly to ByteBuffers
public class FeFrame {
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
    public static FeFrame.FrontendTag lookup(byte input) {
      for (FeFrame.FrontendTag bt : values()) {
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

  private ByteBuffer payload;

  /**
   * creates a frontend tag packed from a series of bytes.
   * @param payload the payload of the packet
   * @param startupPacket if the packet is the startup packet
   */
  public FeFrame(byte[] payload, boolean startupPacket) {
    if (startupPacket) {
      byte[] size = BinaryHelper.writeInt(payload.length);
      payload[0] = size[0];
      payload[1] = size[1];
      payload[2] = size[2];
      payload[3] = size[3];
    } else {
      byte[] size = BinaryHelper.writeInt(payload.length - 1);
      payload[1] = size[0];
      payload[2] = size[1];
      payload[3] = size[2];
      payload[4] = size[3];
    }

    this.payload = ByteBuffer.wrap(payload);
  }

  public ByteBuffer getPayload() {
    return payload;
  }

  public boolean hasRemaining() {
    return payload.hasRemaining();
  }
}
