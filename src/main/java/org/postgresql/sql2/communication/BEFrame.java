package org.postgresql.sql2.communication;

public class BEFrame {
  public enum BackendTag {
    AUTHENTICATION('R'),
    CANCELLATION_KEY_DATA('K'),
    BIND_COMPLETE('2'),
    CLOSE_COMPLETE('3'),
    COMMAND_COMPLETE('C'),
    COPY_DATA('d'),
    COPY_DONE('c'),
    COPY_IN_RESPONSE('G'),
    COPY_OUT_RESPONSE('H'),
    COPY_BOTH_RESPONSE('W'),
    DATA_ROW('D'),
    EMPTY_QUERY_RESPONSE('I'),
    ERROR_RESPONSE('E'),
    FUNCTION_CALL_RESPONSE('V'),
    NEGOTIATE_PROTOCOL_VERSION('v'),
    NO_DATA('n'),
    NOTICE_RESPONSE('N'),
    NOTIFICATION_RESPONSE('A'),
    PARAM_DESCRIPTION('t'),
    PARAM_STATUS('S'),
    PARSE_COMPLETE('1'),
    PORTAL_SUSPENDED('s'),
    READY_FOR_QUERY('Z'),
    ROW_DESCRIPTION('T');

    private char tag;

    BackendTag(char tag) {
      this.tag = tag;
    }

    public static BackendTag lookup(byte input) {
      for (BackendTag bt : values()) {
        if (input == bt.tag) {
          return bt;
        }
      }
      throw new IllegalArgumentException("There is no backend server tag that matches byte " + input);
    }
  }

  private BackendTag tag;
  private byte[] payload;

  public BEFrame(byte tag, byte[] payload) {
    this.tag = BackendTag.lookup(tag);
    this.payload = payload;
  }

  public BackendTag getTag() {
    return tag;
  }

  // TODO make this InputStream from PooledByteBuffer instances (avoids unnecessary copies)
  public byte[] getPayload() {
    return payload;
  }
}
