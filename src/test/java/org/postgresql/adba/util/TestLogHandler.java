package org.postgresql.adba.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class TestLogHandler extends Handler {
  private Level lastLevel = Level.FINEST;
  private String lastMessage = null;

  public Level checkLevel() {
    return lastLevel;
  }

  public String checkMessage() {
    return lastMessage;
  }

  public void publish(LogRecord record) {
    lastLevel = record.getLevel();
    lastMessage = record.getMessage();
  }

  public void close() {
  }

  public void flush() {
  }
}