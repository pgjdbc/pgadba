package org.postgresql.sql2.communication.packets;

import java.nio.charset.StandardCharsets;

public class CommandComplete {
  private String message;

  public CommandComplete(byte[] payload) {
    message = new String(payload, StandardCharsets.UTF_8);
  }

  public String getMessage() {
    return message;
  }
}
