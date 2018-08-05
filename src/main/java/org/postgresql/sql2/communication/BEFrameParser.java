package org.postgresql.sql2.communication;

import java.nio.ByteBuffer;

import org.postgresql.sql2.util.BinaryHelper;

/**
 * Reads bytes from the stream from the server and produces packages on a stack
 */
public class BEFrameParser {
  private enum States {
    BETWEEN, READ_TAG, READ_LEN1, READ_LEN2, READ_LEN3, READ_LEN4
  }

  private States state = States.BETWEEN;

  private byte tag;
  private byte len1;
  private byte len2;
  private byte len3;
  private byte len4;
  private int payloadLength;
  private int payloadRead;

  // TODO wrap ByteBuffer instances in InputStream for the payload (save copy to
  // unnecessary array)
  private byte[] payload;
  
  private int consumedBytes = 0;
  
  public int getConsumedBytes() {
    return this.consumedBytes;
  }

  public BEFrame parseBEFrame(ByteBuffer readBuffer, int position, int bytesRead) {
    this.consumedBytes = 0;
    for (int i = position; i < bytesRead; i++) {
      this.consumedBytes++;
      switch (state) {
      case BETWEEN:
        tag = readBuffer.get(i);
        state = States.READ_TAG;
        break;
      case READ_TAG:
        len1 = readBuffer.get(i);
        state = States.READ_LEN1;
        break;
      case READ_LEN1:
        len2 = readBuffer.get(i);
        state = States.READ_LEN2;
        break;
      case READ_LEN2:
        len3 = readBuffer.get(i);
        state = States.READ_LEN3;
        break;
      case READ_LEN3:
        len4 = readBuffer.get(i);

        payloadLength = BinaryHelper.readInt(len1, len2, len3, len4);
        payload = new byte[payloadLength - 4];
        payloadRead = 0;
        if (payloadLength - 4 == 0) { // no payload sent, so we short cut this here
          state = States.BETWEEN;
          return new BEFrame(tag, payload);
        } else {
          state = States.READ_LEN4;
        }
        break;
      case READ_LEN4:
        payload[payloadRead] = readBuffer.get(i);
        payloadRead++;
        if (payloadRead == payloadLength - 4) {
          state = States.BETWEEN;

          // Have data to process
          return new BEFrame(tag, payload);
        }
        break;
      }
    }

    // As here, buffer underflow
    return null;
  }

}
