package org.postgresql.sql2.communication.packets;

public class ReadyForQuery {
  public enum TransactionStatus {
    IDLE('I'),
    OPEN('T'),
    FAILED('E');

    private byte code;

    TransactionStatus(char code) {
      this.code = (byte)code;
    }

    /**
     * find the transaction status that matches the supplied byte.
     * @param b byte to search for
     * @return the matching TransactionStatus
     */
    public static TransactionStatus lookup(byte b) {
      for (TransactionStatus ts : values()) {
        if (ts.code == b) {
          return ts;
        }
      }

      throw new IllegalArgumentException("unknown ready for query packet tag: " + b);
    }
  }

  private TransactionStatus transactionStatus;

  public ReadyForQuery(byte[] payload) {
    this.transactionStatus = TransactionStatus.lookup(payload[0]);
  }

  public TransactionStatus getTransactionStatus() {
    return transactionStatus;
  }
}
