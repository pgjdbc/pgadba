package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;

import java.time.Duration;
import java.util.function.Consumer;

public class PGCloseOperation implements Operation<Void> {
  @Override
  public Operation<Void> onError(Consumer<Throwable> handler) {
    return null;
  }

  @Override
  public Operation<Void> timeout(Duration minTime) {
    return null;
  }

  @Override
  public Submission<Void> submit() {
    return null;
  }
}
