package org.postgresql.sql2.communication;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ProtocolV3StateMachine {
  public void updateState(ByteBuffer readBuffer, int bytesRead) {

  }

  public boolean hasMoreToWrite() {
    return false;
  }

  public void write(SocketChannel writeBuffer) {
  }
}
