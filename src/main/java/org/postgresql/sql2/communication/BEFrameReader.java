package org.postgresql.sql2.communication;

import org.postgresql.sql2.util.BinaryHelper;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static javax.net.ssl.SSLEngineResult.HandshakeStatus.FINISHED;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.NEED_TASK;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
import static org.postgresql.sql2.util.BinaryHelper.combineBuffers;

/**
 * Reads bytes from the stream from the server and produces packages on a stack
 */
public class BEFrameReader {
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
  private ByteBuffer tlsHandshakePayload;

  private Queue<BEFrame> frames = new ConcurrentLinkedQueue<>();

  public BEFrameReader() {
    tlsHandshakePayload = ByteBuffer.allocate(1000);
    tlsHandshakePayload.flip();
  }

  public void updateState(ByteBuffer readBuffer, int bytesRead, ProtocolV3States.States currentState, SSLEngine tlsEngine) {
    readBuffer.flip();

    if (tlsEngine != null) {
      try {
        SSLEngineResult.HandshakeStatus hss = tlsEngine.getHandshakeStatus();
        if (hss != FINISHED && hss != NOT_HANDSHAKING) {
          if (hss == NEED_UNWRAP) {
            tlsHandshakePayload = combineBuffers(tlsHandshakePayload, readBuffer);
            tlsHandshakePayload.flip();
            ByteBuffer out = ByteBuffer.allocate(tlsEngine.getSession().getApplicationBufferSize());
            SSLEngineResult tlsEngineResult = tlsEngine.unwrap(tlsHandshakePayload, out);
            if (out.position() != 0) {
              frames.add(new BEFrame((byte) '/', out.array()));
            }
            return;
          } else if (hss == NEED_TASK) {
            final Runnable tlsTask = tlsEngine.getDelegatedTask();
            new Thread(tlsTask).run();
            return;
          }
        }
        tlsHandshakePayload = combineBuffers(tlsHandshakePayload, readBuffer);
        tlsHandshakePayload.flip();
        ByteBuffer dst = ByteBuffer.allocate(tlsEngine.getSession().getApplicationBufferSize());
        SSLEngineResult r = tlsEngine.unwrap(tlsHandshakePayload, dst);
        switch (r.getStatus()) {
          case BUFFER_OVERFLOW:
            // Could attempt to drain the dst buffer of any already obtained
            // data, but we'll just increase it to the size needed.
            int appSize = tlsEngine.getSession().getApplicationBufferSize();
            ByteBuffer b = ByteBuffer.allocate(appSize + dst.position());
            dst.flip();
            b.put(dst);
            dst = b;
            // retry the operation.
            break;
          case BUFFER_UNDERFLOW:
            return;
          // other cases: CLOSED, OK.
        }
        dst.flip();
        readBuffer = dst;
        bytesRead = r.bytesProduced();
      } catch (SSLException e) {
        e.printStackTrace();
      }
    }

    if (bytesRead == 1 && currentState == ProtocolV3States.States.TLS_PACKET_SENT) {
      payload = new byte[1];
      payload[0] = readBuffer.get();
      frames.add(new BEFrame((byte) '-', payload));
      return;
    }

    for (int i = 0; i < bytesRead; i++) {
      switch (state) {
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

          payloadLength = BinaryHelper.readInt(len1, len2, len3, len4);
          payload = new byte[payloadLength - 4];
          payloadRead = 0;

          if (payloadLength - 4 == 0) { // no payload sent, so we short cut this here
            frames.add(new BEFrame(tag, payload));
            state = States.BETWEEN;
          } else {
            state = States.READ_LEN4;
          }
          break;
        case READ_LEN4:
          payload[payloadRead] = readBuffer.get();
          payloadRead++;
          if (payloadRead == payloadLength - 4) {
            frames.add(new BEFrame(tag, payload));
            state = States.BETWEEN;
          }
          break;
      }
    }
  }

  public BEFrame popFrame() {
    return frames.poll();
  }
}
