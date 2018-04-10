package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.function.Consumer;

public class ConnectOperation implements Operation<Void> {
  private SocketChannel socketChannel;
  private String host;
  private int port;
  private Consumer<Throwable> errorHandler;
  private Duration minTime;

  public ConnectOperation(SocketChannel socketChannel, String host, int port) {
    this.socketChannel = socketChannel;
    this.host = host;
    this.port = port;
  }

  @Override
  public Operation<Void> onError(Consumer<Throwable> errorHandler) {
    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public Operation<Void> timeout(Duration minTime) {
    this.minTime = minTime;
    return this;
  }

  @Override
  public Submission<Void> submit() {
    try {
      socketChannel.configureBlocking(false);
      socketChannel.connect(new InetSocketAddress(host, port));
    } catch (IOException e) {
      errorHandler.accept(e);
    }
    return null;
  }
}
