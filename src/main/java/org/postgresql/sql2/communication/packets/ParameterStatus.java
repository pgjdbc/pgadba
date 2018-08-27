package org.postgresql.sql2.communication.packets;

import java.io.IOException;

import org.postgresql.sql2.communication.NetworkInputStream;
import org.postgresql.sql2.communication.NetworkReadContext;

public class ParameterStatus {
  private String name;
  private String value;

  public ParameterStatus(NetworkReadContext context) throws IOException {
    NetworkInputStream input = context.getPayload();
    name = input.readString();
    value = input.readString();
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }
}
