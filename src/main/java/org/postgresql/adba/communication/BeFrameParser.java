package org.postgresql.adba.communication;

import java.nio.ByteBuffer;
import org.postgresql.adba.util.BinaryHelper;

/**
 * Reads bytes from the stream from the server and produces packages on a stack.
 */
public class BeFrameParser {
  private enum States {
    NEVER_USED, BETWEEN, READ_TAG, READ_LEN1, READ_LEN2, READ_LEN3, READ_LEN4
  }

  private States state = States.NEVER_USED;

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

  /**
   * Reads bytes from the readBuffer, starting at position and stopping when the first
   * packet ends or bytesRead bytes are consumed.
   *
   * @param readBuffer the buffer that contains the packets
   * @param position position to start to read at
   * @param bytesRead number of bytes that's available for reading
   * @return a BeFrame
   */
  public BeFrame parseBeFrame(ByteBuffer readBuffer, int position, int bytesRead) {
    this.consumedBytes = 0;
    for (int i = position; i < bytesRead; i++) {
      this.consumedBytes++;
      switch (state) {
        case NEVER_USED:
          if (readBuffer.get(i) == 'S' || readBuffer.get(i) == 'N') {
            state = States.BETWEEN;
            return new BeFrame((byte)'/', new byte[] {readBuffer.get(i)});
          }
          tag = readBuffer.get(i);
          state = States.READ_TAG;
          break;
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
            return new BeFrame(tag, payload);
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
            return new BeFrame(tag, payload);
          }
          break;
        default:
          throw new IllegalStateException("not all BeFrameParser.States implemented in switch");
      }
    }

    // As here, buffer underflow
    return null;
  }

}
