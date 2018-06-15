package org.postgresql.sql2.operations;

import jdk.incubator.sql2.OutOperation;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.SqlType;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.operations.helpers.FutureQueryParameter;
import org.postgresql.sql2.operations.helpers.ParameterHolder;
import org.postgresql.sql2.operations.helpers.ValueQueryParameter;
import org.postgresql.sql2.submissions.GroupSubmission;
import org.postgresql.sql2.submissions.OutSubmission;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

public class PGOutOperation<R> implements OutOperation<R> {
  private final PGConnection connection;
  private final String sql;
  private ParameterHolder holder;
  private Consumer<Throwable> errorHandler;
  private Function<Result.OutParameterMap, ? extends R> processor;
  private Map<String, SqlType> outParameterTypes;
  private GroupSubmission groupSubmission;

  public PGOutOperation(PGConnection connection, String sql, GroupSubmission groupSubmission) {
    this.connection = connection;
    this.sql = sql;
    this.holder = new ParameterHolder();
    this.outParameterTypes = new HashMap<>();
    this.groupSubmission = groupSubmission;
  }

  @Override
  public OutOperation<R> outParameter(String id, SqlType type) {
    outParameterTypes.put(id, type);
    holder.add(id, new ValueQueryParameter(null, type));
    return this;
  }

  @Override
  public OutOperation<R> apply(Function<Result.OutParameterMap, ? extends R> processor) {
    this.processor = processor;
    return this;
  }

  @Override
  public OutOperation<R> onError(Consumer<Throwable> errorHandler) {
    if (this.errorHandler != null) {
      throw new IllegalStateException("you are not allowed to call onError multiple times");
    }

    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public OutOperation<R> set(String id, Object value) {
    holder.add(id, new ValueQueryParameter(value));
    return this;
  }

  @Override
  public OutOperation<R> set(String id, Object value, SqlType type) {
    holder.add(id, new ValueQueryParameter(value, type));
    return this;
  }

  @Override
  public OutOperation<R> set(String id, CompletionStage<?> source) {
    holder.add(id, new FutureQueryParameter(source));
    return this;
  }

  @Override
  public OutOperation<R> set(String id, CompletionStage<?> source, SqlType type) {
    holder.add(id, new FutureQueryParameter(source, type));
    return this;
  }

  @Override
  public OutOperation<R> timeout(Duration minTime) {
    return this;
  }

  @Override
  public Submission<R> submit() {
    PGSubmission<R> submission = new OutSubmission<>(this::cancel, errorHandler, sql, outParameterTypes, processor, groupSubmission, holder);
    connection.addSubmissionOnQue(submission);
    return submission;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
