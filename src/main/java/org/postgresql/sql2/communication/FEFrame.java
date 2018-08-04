package org.postgresql.sql2.communication;

import org.postgresql.sql2.util.BinaryHelper;

import java.nio.ByteBuffer;

@Deprecated // write directly to ByteBuffers
public class FEFrame {
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

    public static FEFrame.FrontendTag lookup(byte input) {
      for (FEFrame.FrontendTag bt : values()) {
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

  public FEFrame(byte[] payload, boolean startupPacket) {
    if(startupPacket) {
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
