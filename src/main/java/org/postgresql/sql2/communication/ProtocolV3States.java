package org.postgresql.sql2.communication;

import java.util.HashMap;
import java.util.Map;

public class ProtocolV3States {
  public enum States {
    NOT_CONNECTED,
    STARTUP_PACKET_SENT,
    AUTHENTICATION_REQUESTED,
    AUTHENTICATION_SENT,
    IDLE
  }

  public enum Events {
    CONNECTION,
    AUTHENTICATION_REQUEST,
    PASSWORD_SENT,
    AUTHENTICATION_SUCCESS,
    PARAMETER_STATUS,
    BACKEND_KEY_DATA,
    READY_FOR_QUERY
  }

  private final static Map<States, Map<Events, States>> transitions = new HashMap<>();

  static {
    addTransition(States.NOT_CONNECTED, Events.CONNECTION, States.STARTUP_PACKET_SENT);
    addTransition(States.STARTUP_PACKET_SENT, Events.AUTHENTICATION_REQUEST, States.AUTHENTICATION_REQUESTED);
    addTransition(States.AUTHENTICATION_REQUESTED, Events.PASSWORD_SENT, States.AUTHENTICATION_REQUESTED);
    addTransition(States.AUTHENTICATION_REQUESTED, Events.AUTHENTICATION_SUCCESS, States.AUTHENTICATION_REQUESTED);
    addTransition(States.AUTHENTICATION_REQUESTED, Events.PARAMETER_STATUS, States.AUTHENTICATION_REQUESTED);
    addTransition(States.AUTHENTICATION_REQUESTED, Events.BACKEND_KEY_DATA, States.AUTHENTICATION_REQUESTED);
    addTransition(States.AUTHENTICATION_REQUESTED, Events.READY_FOR_QUERY, States.IDLE);
  }

  private static void addTransition(States start, Events event, States end) {
    if(!transitions.containsKey(start))
      transitions.put(start, new HashMap<>());

    transitions.get(start).put(event, end);
  }

  public static States lookup(States state, Events event) {
    States targetState = transitions.get(state).get(event);

    if (targetState == null)
      System.out.println("target state missing for start state: " + state + " and event: " + event);

    return targetState;
  }
}
