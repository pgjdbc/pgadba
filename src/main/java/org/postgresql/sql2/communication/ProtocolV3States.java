package org.postgresql.sql2.communication;

import java.util.HashMap;
import java.util.Map;

public class ProtocolV3States {
  public enum States {
    NOT_CONNECTED,
    TLS_PACKET_SENT,
    TLS_CONNECTION_REJECTED,
    TLS_CONNECTION_ACCEPTED,
    TLS_HANDSHAKE_FINISHED,
    STARTUP_PACKET_SENT,
    AUTHENTICATION_REQUESTED,
    AUTHENTICATION_SENT,
    IDLE,
    PROCESSING_QUERY
  }

  public enum Events {
    CONNECTION,
    TLS_CONNECTION_START,
    TLS_YES,
    TLS_NO,
    TLS_HANDSHAKE_DONE,
    AUTHENTICATION_REQUEST,
    PASSWORD_SENT,
    AUTHENTICATION_SUCCESS,
    PARAMETER_STATUS,
    BACKEND_KEY_DATA,
    READY_FOR_QUERY,
    PARSE_COMPLETE,
    BIND_COMPLETE,
    COMMAND_COMPLETE;
  }

  private final static Map<States, Map<Events, States>> transitions = new HashMap<>();

  static {
    addTransition(States.NOT_CONNECTED, Events.CONNECTION, States.STARTUP_PACKET_SENT);
    addTransition(States.NOT_CONNECTED, Events.TLS_CONNECTION_START, States.TLS_PACKET_SENT);
    addTransition(States.TLS_PACKET_SENT, Events.TLS_YES, States.TLS_CONNECTION_ACCEPTED);
    addTransition(States.TLS_PACKET_SENT, Events.TLS_NO, States.TLS_CONNECTION_REJECTED);
    addTransition(States.TLS_CONNECTION_ACCEPTED, Events.TLS_HANDSHAKE_DONE, States.TLS_HANDSHAKE_FINISHED);
    addTransition(States.TLS_HANDSHAKE_FINISHED, Events.CONNECTION, States.STARTUP_PACKET_SENT);
    addTransition(States.TLS_CONNECTION_ACCEPTED, Events.CONNECTION, States.STARTUP_PACKET_SENT);
    addTransition(States.STARTUP_PACKET_SENT, Events.AUTHENTICATION_REQUEST, States.AUTHENTICATION_REQUESTED);
    addTransition(States.AUTHENTICATION_REQUESTED, Events.PASSWORD_SENT, States.AUTHENTICATION_REQUESTED);
    addTransition(States.AUTHENTICATION_REQUESTED, Events.AUTHENTICATION_SUCCESS, States.AUTHENTICATION_REQUESTED);
    addTransition(States.AUTHENTICATION_REQUESTED, Events.PARAMETER_STATUS, States.AUTHENTICATION_REQUESTED);
    addTransition(States.AUTHENTICATION_REQUESTED, Events.BACKEND_KEY_DATA, States.AUTHENTICATION_REQUESTED);
    addTransition(States.AUTHENTICATION_REQUESTED, Events.READY_FOR_QUERY, States.IDLE);
    addTransition(States.IDLE, Events.PARSE_COMPLETE, States.PROCESSING_QUERY);
    addTransition(States.IDLE, Events.BIND_COMPLETE, States.PROCESSING_QUERY);
    addTransition(States.PROCESSING_QUERY, Events.BIND_COMPLETE, States.PROCESSING_QUERY);
    addTransition(States.PROCESSING_QUERY, Events.COMMAND_COMPLETE, States.PROCESSING_QUERY);
    addTransition(States.PROCESSING_QUERY, Events.READY_FOR_QUERY, States.IDLE);
  }

  private static void addTransition(States start, Events event, States end) {
    if(!transitions.containsKey(start))
      transitions.put(start, new HashMap<>());

    transitions.get(start).put(event, end);
  }

  public static States lookup(States state, Events event) {
    States targetState = transitions.get(state).get(event);

    if (targetState == null) {
      System.out.println("target state missing for start state: " + state + " and event: " + event);
    }

    return targetState;
  }
}
