package org.postgresql.sql2.communication.packets;

import org.postgresql.sql2.communication.packets.parts.ErrorResponseField;
import org.postgresql.sql2.util.BinaryHelper;

import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {
  private final List<ErrorResponseField> fields = new ArrayList<>();

  public ErrorResponse(byte[] payload) {
    List<Integer> nullPositions = new ArrayList<>();

    for(int i = 0; i < payload.length; i++) {
      if(payload[i] == 0) {
        nullPositions.add(i);
      }
    }

    for(int i = 0; i < nullPositions.size() - 2; i++) {
      fields.add(new ErrorResponseField(ErrorResponseField.Types.lookup(payload[nullPositions.get(i) + 1]),
          new String(BinaryHelper.subBytes(payload, nullPositions.get(i) + 2, nullPositions.get(i + 1)))));
    }
  }

  public List<ErrorResponseField> getFields() {
    return fields;
  }
}
