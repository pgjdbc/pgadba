/*
MIT License

Copyright (c) [2015-2018] all contributors of https://github.com/marianobarrios/tls-channel, Alexander Kjäll

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package org.postgresql.adba.util.tlschannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channel;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import org.postgresql.adba.util.tlschannel.impl.ByteBufferSet;
import org.postgresql.adba.util.tlschannel.impl.TlsChannelImpl;

/**
 * A client-side {@link TlsChannel}.
 */
public class ClientTlsChannel implements TlsChannel {

  /**
   * Builder of {@link ClientTlsChannel}.
   */
  public static class Builder extends TlsChannelBuilder<Builder> {

    private Supplier<SSLEngine> sslEngineFactory;

    private Builder(ByteChannel underlying, SSLEngine sslEngine) {
      super(underlying);
      this.sslEngineFactory = () -> sslEngine;
    }

    private Builder(ByteChannel underlying, SSLContext sslContext) {
      super(underlying);
      this.sslEngineFactory = () -> defaultSslEngineFactory(sslContext);
    }

    @Override
    Builder getThis() {
      return this;
    }

    public ClientTlsChannel build() {
      return new ClientTlsChannel(underlying, sslEngineFactory.get(), sessionInitCallback, runTasks,
          plainBufferAllocator, encryptedBufferAllocator, releaseBuffers, waitForCloseConfirmation);
    }

  }

  private static SSLEngine defaultSslEngineFactory(SSLContext sslContext) {
    SSLEngine engine = sslContext.createSSLEngine();
    engine.setUseClientMode(true);
    return engine;
  }

  /**
   * Create a new {@link Builder}, configured with a underlying {@link Channel} and a fixed {@link SSLEngine}.
   *
   * @param underlying a reference to the underlying {@link ByteChannel}
   * @param sslEngine the engine to use with this channel
   * @return the builder
   */
  public static Builder newBuilder(ByteChannel underlying, SSLEngine sslEngine) {
    return new Builder(underlying, sslEngine);
  }

  /**
   * Create a new {@link Builder}, configured with a underlying {@link Channel} and a {@link SSLContext}.
   *
   * @param underlying a reference to the underlying {@link ByteChannel}
   * @param sslContext a context to use with this channel, it will be used to create a client {@link SSLEngine}.
   * @return the builder
   */
  public static Builder newBuilder(ByteChannel underlying, SSLContext sslContext) {
    return new Builder(underlying, sslContext);
  }

  private final ByteChannel underlying;
  private final TlsChannelImpl impl;

  private ClientTlsChannel(
      ByteChannel underlying,
      SSLEngine engine,
      Consumer<SSLSession> sessionInitCallback,
      boolean runTasks,
      BufferAllocator plainBufAllocator,
      BufferAllocator encryptedBufAllocator,
      boolean releaseBuffers,
      boolean waitForCloseNotifyOnClose) {
    if (!engine.getUseClientMode()) {
      throw new IllegalArgumentException("SSLEngine must be in client mode");
    }
    this.underlying = underlying;
    TrackingAllocator trackingPlainBufAllocator = new TrackingAllocator(plainBufAllocator);
    TrackingAllocator trackingEncryptedAllocator = new TrackingAllocator(encryptedBufAllocator);
    impl = new TlsChannelImpl(underlying, underlying, engine, Optional.empty(), sessionInitCallback, runTasks,
        trackingPlainBufAllocator, trackingEncryptedAllocator, releaseBuffers, waitForCloseNotifyOnClose);
  }

  @Override
  public ByteChannel getUnderlying() {
    return underlying;
  }

  @Override
  public SSLEngine getSslEngine() {
    return impl.engine();
  }

  @Override
  public Consumer<SSLSession> getSessionInitCallback() {
    return impl.getSessionInitCallback();
  }

  @Override
  public TrackingAllocator getPlainBufferAllocator() {
    return impl.getPlainBufferAllocator();
  }

  @Override
  public TrackingAllocator getEncryptedBufferAllocator() {
    return impl.getEncryptedBufferAllocator();
  }

  @Override
  public boolean getRunTasks() {
    return impl.getRunTasks();
  }

  @Override
  public long read(ByteBuffer[] dstBuffers, int offset, int length) throws IOException {
    ByteBufferSet dest = new ByteBufferSet(dstBuffers, offset, length);
    TlsChannelImpl.checkReadBuffer(dest);
    return impl.read(dest);
  }

  @Override
  public long read(ByteBuffer[] dstBuffers) throws IOException {
    return read(dstBuffers, 0, dstBuffers.length);
  }

  @Override
  public int read(ByteBuffer dstBuffer) throws IOException {
    return (int) read(new ByteBuffer[]{dstBuffer});
  }

  @Override
  public long write(ByteBuffer[] srcBuffers, int offset, int length) throws IOException {
    ByteBufferSet source = new ByteBufferSet(srcBuffers, offset, length);
    return impl.write(source);
  }

  @Override
  public long write(ByteBuffer[] outs) throws IOException {
    return write(outs, 0, outs.length);
  }

  @Override
  public int write(ByteBuffer srcBuffer) throws IOException {
    return (int) write(new ByteBuffer[]{srcBuffer});
  }

  @Override
  public void renegotiate() throws IOException {
    impl.renegotiate();
  }

  @Override
  public void handshake() throws IOException {
    impl.handshake();
  }

  @Override
  public void close() throws IOException {
    impl.close();
  }

  @Override
  public boolean isOpen() {
    return impl.isOpen();
  }

  @Override
  public boolean shutdown() throws IOException {
    return impl.shutdown();
  }

  @Override
  public boolean shutdownReceived() {
    return impl.shutdownReceived();
  }

  @Override
  public boolean shutdownSent() {
    return impl.shutdownSent();
  }

}