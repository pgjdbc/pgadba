package org.postgresql.sql2.communication;

import java2.sql2.ConnectionProperty;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.postgresql.sql2.PGConnectionProperties;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ProtocolV3Test {

  @Test
  public void sendStartupPacket() {
  }

  @Test
  public void doAuthentication() throws IOException {
    Map<ConnectionProperty, Object> properties = new HashMap<>();
    for(PGConnectionProperties prop : PGConnectionProperties.values())
      properties.put(prop, prop.defaultValue());

    ProtocolV3 instance = new ProtocolV3(properties);

    BEFrame authRequest = new BEFrame((byte) 0x52, new byte[] {0x00, 0x00, 0x00, 0x05, (byte)0xd9, 0x11, 0x4d, 0x0b});

    instance.doAuthentication(authRequest);

    SocketChannel sc = mock(SocketChannel.class);

    instance.sendData(sc);

    ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);
    verify(sc).write(bufferCaptor.capture());

    ByteBuffer buf = bufferCaptor.getValue();
    buf.flip();

    byte[] expexted = new byte[] {
        0x70, 0x00, 0x00, 0x00, 0x28, 0x6d, 0x64, 0x35, 0x33, 0x32, 0x34, 0x37, 0x32, 0x31, 0x63, 0x37,
        0x32, 0x61, 0x64, 0x62, 0x39, 0x30, 0x33, 0x63, 0x63, 0x39, 0x61, 0x66, 0x30, 0x34, 0x62, 0x61,
        0x31, 0x31, 0x64, 0x65, 0x39, 0x36, 0x35, 0x39, 0x00 };
    assertArrayEquals(expexted, buf.array());
  }
}