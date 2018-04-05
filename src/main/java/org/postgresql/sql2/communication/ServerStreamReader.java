package org.postgresql.sql2.communication;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Reads bytes from the stream from the server and produces packages on a stack
 */
public class ServerStreamReader {
  private enum States {
    BETWEEN,
    READ_TAG,
    READ_LEN1,
    READ_LEN2,
    READ_LEN3,
    READ_LEN4
  }

  private States state = States.BETWEEN;

  private byte tag;
  private byte len1;
  private byte len2;
  private byte len3;
  private byte len4;
  private int payloadLength;
  private int payloadRead;
  private byte[] payload;

  private Queue<ServerPacket> packets = new ConcurrentLinkedQueue<>();

  public void updateState(ByteBuffer readBuffer, int bytesRead) {
    readBuffer.flip();

    for(int i = 0; i < bytesRead; i++) {
      switch (state){
        case BETWEEN:
          tag = readBuffer.get();
          state = States.READ_TAG;
          break;
        case READ_TAG:
          len1 = readBuffer.get();
          state = States.READ_LEN1;
          break;
        case READ_LEN1:
          len2 = readBuffer.get();
          state = States.READ_LEN2;
          break;
        case READ_LEN2:
          len3 = readBuffer.get();
          state = States.READ_LEN3;
          break;
        case READ_LEN3:
          len4 = readBuffer.get();

          payloadLength = (len1 & 0xFF) << 24 | (len2 & 0xFF) << 16 | (len3 & 0xFF) << 8 | (len4 & 0xFF);
          payload = new byte[payloadLength];
          payloadRead = 0;

          state = States.READ_LEN4;
          break;
        case READ_LEN4:
          payload[payloadRead] = readBuffer.get();
          payloadRead++;
          if(payloadRead == payloadLength - 4) {
            packets.add(new ServerPacket(tag, payload));
            state = States.BETWEEN;
          }
          break;
      }
    }
  }

  public ServerPacket popPacket() {
    return packets.poll();
  }
}
