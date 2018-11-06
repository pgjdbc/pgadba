package org.postgresql.adba.communication.packets;

import org.postgresql.adba.communication.packets.parts.ErrorResponseField;
import org.postgresql.adba.util.BinaryHelper;

import java.util.ArrayList;
import java.util.List;

public class ErrorPacket extends Exception {
  
  private static List<ErrorResponseField> parseFields(byte[] payload) {
    List<ErrorResponseField> fields = new ArrayList<>();
    List<Integer> nullPositions = new ArrayList<>();

    for (int i = 0; i < payload.length; i++) {
      if (payload[i] == 0) {
        nullPositions.add(i);
      }
    }

    for (int i = 0; i < nullPositions.size() - 2; i++) {
      fields.add(new ErrorResponseField(ErrorResponseField.Types.lookup(payload[nullPositions.get(i) + 1]),
          new String(BinaryHelper.subBytes(payload, nullPositions.get(i) + 2, nullPositions.get(i + 1)))));
    }
    return fields;
  }
  
  private static String getField(ErrorResponseField.Types type, List<ErrorResponseField> fields) {
    for (ErrorResponseField field : fields) {
      if (type == field.getType()) {
        return field.getMessage();
      }
    }

    return null;
  }
  
  private List<ErrorResponseField> fields;

  public ErrorPacket(byte[] payload) {
    this(parseFields(payload));
  }
  
  private ErrorPacket(List<ErrorResponseField> fields) {
    super(getField(ErrorResponseField.Types.MESSAGE, fields));
    this.fields = fields;
  }

  public List<ErrorResponseField> getFields() {
    return fields;
  }

  /**
   * returns the message of the field that matches the type.
   * @param type type to search for
   * @return message of field
   */
  public String getField(ErrorResponseField.Types type) {
    for (ErrorResponseField field : fields) {
      if (type == field.getType()) {
        return field.getMessage();
      }
    }

    return null;
  }
}
