package org.postgresql.sql2;

import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.operations.helpers.ParameterHolder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class PGSubmission<T> implements Submission<T> {
  final private Supplier<Boolean> cancel;
  private CompletableFuture<T> publicStage;
  private boolean connectionSubmission;
  private String sql;
  private final AtomicBoolean sendConsumed = new AtomicBoolean(false);
  private ParameterHolder holder;

  public PGSubmission(Supplier<Boolean> cancel) {
    this.cancel = cancel;
  }

  @Override
  public CompletionStage<Boolean> cancel() {
    return new CompletableFuture<Boolean>().completeAsync(cancel);
  }

  @Override
  public CompletionStage<T> getCompletionStage() {
    if (publicStage == null)
      publicStage = new CompletableFuture<>();
    return publicStage;
  }

  public boolean isConnectionSubmission() {
    return connectionSubmission;
  }

  public void setConnectionSubmission(boolean connectionSubmission) {
    this.connectionSubmission = connectionSubmission;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

  public String getSql() {
    return sql;
  }

  public AtomicBoolean getSendConsumed() {
    return sendConsumed;
  }

  public void setHolder(ParameterHolder holder) {
    this.holder = holder;
  }

  public ParameterHolder getHolder() {
    return holder;
  }
}