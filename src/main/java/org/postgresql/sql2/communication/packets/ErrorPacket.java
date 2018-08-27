package org.postgresql.sql2.communication.packets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.sql2.communication.NetworkInputStream;
import org.postgresql.sql2.communication.NetworkReadContext;
import org.postgresql.sql2.communication.packets.parts.ErrorResponseField;

public class ErrorPacket extends Exception {

  private static List<ErrorResponseField> parseFields(NetworkReadContext context) throws IOException {

    List<ErrorResponseField> fields = new ArrayList<>();

    // Parse out the fields
    NetworkInputStream input = context.getPayload();
    int errorType;
    while ((errorType = input.read()) != -1) {
      String message = input.readString();
      fields.add(new ErrorResponseField(ErrorResponseField.Types.lookup(errorType), message));
    }
    
    // Return the fields
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

  public ErrorPacket(NetworkReadContext context) throws IOException {
    this(parseFields(context));
  }

  private ErrorPacket(List<ErrorResponseField> fields) {
    super(getField(ErrorResponseField.Types.MESSAGE, fields));
    this.fields = fields;
  }

  public List<ErrorResponseField> getFields() {
    return fields;
  }

  public String getField(ErrorResponseField.Types type) {
    for (ErrorResponseField field : fields) {
      if (type == field.getType()) {
        return field.getMessage();
      }
    }

    return null;
  }
}
