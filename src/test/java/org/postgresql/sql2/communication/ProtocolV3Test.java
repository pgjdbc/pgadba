package org.postgresql.sql2.communication;

import jdk.incubator.sql2.ConnectionProperty;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.postgresql.sql2.PGConnectionProperties;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ProtocolV3Test {

  @Test
  public void sendStartupPacket() {
  }

  @Test
  public void doAuthentication() throws IOException {
    Map<ConnectionProperty, Object> properties = new HashMap<>();
    for (PGConnectionProperties prop : PGConnectionProperties.values())
      properties.put(prop, prop.defaultValue());

    ProtocolV3 instance = new ProtocolV3(properties);

    instance.sendStartupPacket();

    BEFrame authRequest = new BEFrame((byte) 0x52, new byte[]{0x00, 0x00, 0x00, 0x05, (byte) 0xd9, 0x11, 0x4d, 0x0b});

    instance.doAuthentication(authRequest);

    SocketChannel sc = mock(SocketChannel.class);
    instance.sendData(sc);

    ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);
    verify(sc).write(bufferCaptor.capture());

    ByteBuffer buf = bufferCaptor.getValue();
    buf.flip();

    assertEquals("540003000075736572007465737400646174616261736500006170706c69636174696f6e5f6e616d65006a6176615f73716c325f636c69656e7400636c69656e745f656e636f64696e6700555446380000",
        new BigInteger(buf.array()).toString(16));
  }
}