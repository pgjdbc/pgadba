package org.postgresql.sql2.communication.packets;

import org.postgresql.sql2.util.BinaryHelper;

public class ParameterStatus {
  private String name;
  private String value;

  public ParameterStatus(byte[] payload) {
    int firstNullPos = 0;
    int secondNullPos = 0;

    for(int i = 0; i < payload.length; i++) {
      if(payload[i] == 0) {
        firstNullPos = i;
        break;
      }
    }

    for(int i = firstNullPos; i < payload.length; i++) {
      if(payload[i] == 0) {
        secondNullPos = i;
        break;
      }
    }

    name = new String(BinaryHelper.subBytes(payload, 0, firstNullPos));
    value = new String(BinaryHelper.subBytes(payload, firstNullPos, secondNullPos));
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }
}
