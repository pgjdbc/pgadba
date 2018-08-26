package org.postgresql.sql2.communication.packets;

import java.nio.charset.StandardCharsets;

public class CommandComplete {
  public enum Types {
    INSERT,
    DELETE,
    CREATE_TABLE,
    CREATE_TYPE,
    START_TRANSACTION,
    ROLLBACK,
    COMMIT,
    UPDATE,
    SELECT,
    MOVE,
    FETCH,
    COPY
  }

  private int numberOfRowsAffected;
  private Types type;

  /**
   * parses a command complete package from the server.
   * @param payload the bytes to parse
   */
  public CommandComplete(byte[] payload) {
    String message = new String(payload, StandardCharsets.UTF_8);

    if (message.startsWith("INSERT")) {
      type = Types.INSERT;
      numberOfRowsAffected = Integer.parseInt(message.substring(message.lastIndexOf(" ") + 1, message.length() - 1));
    } else if (message.startsWith("DELETE")) {
      type = Types.DELETE;
      numberOfRowsAffected = Integer.parseInt(message.substring(message.lastIndexOf(" ") + 1, message.length() - 1));
    } else if (message.startsWith("CREATE TABLE")) {
      type = Types.CREATE_TABLE;
      numberOfRowsAffected = 0;
    } else if (message.startsWith("CREATE TYPE")) {
      type = Types.CREATE_TYPE;
      numberOfRowsAffected = 0;
    } else if (message.startsWith("START TRANSACTION")) {
      type = Types.START_TRANSACTION;
      numberOfRowsAffected = 0;
    } else if (message.startsWith("ROLLBACK")) {
      type = Types.ROLLBACK;
      numberOfRowsAffected = 0;
    } else if (message.startsWith("COMMIT")) {
      type = Types.COMMIT;
      numberOfRowsAffected = 0;
    } else if (message.startsWith("UPDATE")) {
      type = Types.UPDATE;
      numberOfRowsAffected = Integer.parseInt(message.substring(message.lastIndexOf(" ") + 1, message.length() - 1));
    } else if (message.startsWith("SELECT")) {
      type = Types.SELECT;
      numberOfRowsAffected = Integer.parseInt(message.substring(message.lastIndexOf(" ") + 1, message.length() - 1));
    } else if (message.startsWith("MOVE")) {
      type = Types.MOVE;
      numberOfRowsAffected = Integer.parseInt(message.substring(message.lastIndexOf(" ") + 1, message.length() - 1));
    } else if (message.startsWith("FETCH")) {
      type = Types.FETCH;
      numberOfRowsAffected = Integer.parseInt(message.substring(message.lastIndexOf(" ") + 1, message.length() - 1));
    } else if (message.startsWith("COPY")) {
      type = Types.COPY;
      numberOfRowsAffected = Integer.parseInt(message.substring(message.lastIndexOf(" ") + 1, message.length() - 1));
    }
  }

  public int getNumberOfRowsAffected() {
    return numberOfRowsAffected;
  }

  public Types getType() {
    return type;
  }
}
