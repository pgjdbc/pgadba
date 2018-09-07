package org.postgresql.sql2.communication;

import jdk.incubator.sql2.ConnectionProperty;

/**
 * Context for writing to the network.
 * 
 * @author Daniel Sagenschneider
 */
public interface NetworkReadContext extends NetworkContext {

  /**
   * Obtains the frame tag.
   * 
   * @return Frame tag.
   */
  char getFrameTag();

  /**
   * Obtains the payload length.
   * 
   * @return Payload length.
   */
  int getPayloadLength();

  /**
   * Obtains the payload.
   * 
   * @return Payload.
   */
  NetworkInputStream getPayload();

  /**
   * Obtains the {@link QueryFactory}.
   * 
   * @return {@link QueryFactory}.
   */
  QueryFactory getQueryFactory();

  /**
   * Allows overriding {@link ConnectionProperty}.
   * 
   * @param property {@link ConnectionProperty}.
   * @param value    Value.
   */
  void setProperty(ConnectionProperty property, Object value);

  /**
   * Triggers for a {@link NetworkRequest} to be undertaken.
   * 
   * @param request {@link NetworkRequest} to be undertaken.
   */
  void write(NetworkRequest request);

  /**
   * Triggers for a write.
   */
  void writeRequired();

}