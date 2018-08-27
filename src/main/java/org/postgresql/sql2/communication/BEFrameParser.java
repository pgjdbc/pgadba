package org.postgresql.sql2.communication;

import java.io.IOException;

import org.postgresql.sql2.util.BinaryHelper;

/**
 * Reads bytes from the stream from the server and produces packages on a stack
 */
public class BEFrameParser {

  public static final char AUTHENTICATION = 'R';
  public static final char CANCELLATION_KEY_DATA = 'K';
  public static final char BIND_COMPLETE = '2';
  public static final char CLOSE_COMPLETE = '3';
  public static final char COMMAND_COMPLETE = 'C';
  public static final char COPY_DATA = 'd';
  public static final char COPY_DONE = 'c';
  public static final char COPY_IN_RESPONSE = 'G';
  public static final char COPY_OUT_RESPONSE = 'H';
  public static final char COPY_BOTH_RESPONSE = 'W';
  public static final char DATA_ROW = 'D';
  public static final char EMPTY_QUERY_RESPONSE = 'I';
  public static final char ERROR_RESPONSE = 'E';
  public static final char FUNCTION_CALL_RESPONSE = 'V';
  public static final char NEGOTIATE_PROTOCOL_VERSION = 'v';
  public static final char NO_DATA = 'n';
  public static final char NOTICE_RESPONSE = 'N';
  public static final char NOTIFICATION_RESPONSE = 'A';
  public static final char PARAM_DESCRIPTION = 't';
  public static final char PARAM_STATUS = 'S';
  public static final char PARSE_COMPLETE = '1';
  public static final char PORTAL_SUSPENDED = 's';
  public static final char READY_FOR_QUERY = 'Z';
  public static final char ROW_DESCRIPTION = 'T';

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

  public boolean parseBEFrame(NetworkInputStream inputStream) throws IOException {

    // Read frame header (tag and length)
    if (this.state != States.READ_LEN4) {
      READ_HEADER: while (inputStream.available() > 0) {
        switch (state) {
        case BETWEEN:
          tag = (byte) inputStream.read();
          state = States.READ_TAG;
          break;
        case READ_TAG:
          len1 = (byte) inputStream.read();
          state = States.READ_LEN1;
          break;
        case READ_LEN1:
          len2 = (byte) inputStream.read();
          state = States.READ_LEN2;
          break;
        case READ_LEN2:
          len3 = (byte) inputStream.read();
          state = States.READ_LEN3;
          break;
        case READ_LEN3:
          len4 = (byte) inputStream.read();
          // -4 to ignore payload length
          payloadLength = BinaryHelper.readInt(len1, len2, len3, len4) - 4;
          state = States.READ_LEN4;
          break READ_HEADER;
        case READ_LEN4:
          break READ_HEADER;
        }
      }
    }

    // Wait until all frame bytes available
    if (this.state == States.READ_LEN4) {
      if (this.payloadLength <= inputStream.available()) {

        // Reset for next frame
        this.state = States.BETWEEN;
        return true;
      }
    }

    // As here, buffer underflow
    return false;
  }

  public char getTag() {
    return (char) this.tag;
  }

  public int getPayloadLength() {
    return this.payloadLength;
  }

}