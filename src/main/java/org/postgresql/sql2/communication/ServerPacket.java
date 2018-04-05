package org.postgresql.sql2.communication;

public class ServerPacket {
  private byte tag;
  private byte[] payload;

  public ServerPacket(byte tag, byte[] payload) {
    this.tag = tag;
    this.payload = payload;
  }
}
