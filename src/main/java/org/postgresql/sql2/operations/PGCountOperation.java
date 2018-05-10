package org.postgresql.sql2.operations;

import jdk.incubator.sql2.ParameterizedCountOperation;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.RowOperation;
import jdk.incubator.sql2.SqlType;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGConnection;
import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.operations.helpers.FutureQueryParameter;
import org.postgresql.sql2.operations.helpers.ParameterHolder;
import org.postgresql.sql2.operations.helpers.ValueQueryParameter;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

public class PGCountOperation<R> implements ParameterizedCountOperation<R> {
  private PGConnection connection;
  private String sql;
  private ParameterHolder holder;

  public PGCountOperation(PGConnection connection, String sql) {
    this.connection = connection;
    this.sql = sql;
    this.holder = new ParameterHolder();
  }

  @Override
  public RowOperation<R> returning(String... keys) {
    return null;
  }

  @Override
  public ParameterizedCountOperation<R> onError(Consumer<Throwable> handler) {
    return this;
  }

  @Override
  public ParameterizedCountOperation<R> apply(Function<Result.Count, ? extends R> processor) {
    return this;
  }

  @Override
  public ParameterizedCountOperation<R> set(String id, Object value) {
    holder.add(id, new ValueQueryParameter(value));
    return this;
  }

  @Override
  public ParameterizedCountOperation<R> set(String id, Object value, SqlType type) {
    holder.add(id, new ValueQueryParameter(value, type));
    return this;
  }

  @Override
  public ParameterizedCountOperation<R> set(String id, CompletionStage<?> source) {
    holder.add(id, new FutureQueryParameter(source));
    return this;
  }

  @Override
  public ParameterizedCountOperation<R> set(String id, CompletionStage<?> source, SqlType type) {
    holder.add(id, new FutureQueryParameter(source, type));
    return this;
  }

  @Override
  public ParameterizedCountOperation<R> timeout(Duration minTime) {
    return this;
  }

  @Override
  public Submission<R> submit() {
    PGSubmission<R> submission = new PGSubmission<>(this::cancel);
    submission.setConnectionSubmission(false);
    submission.setSql(sql);
    submission.setHolder(holder);
    submission.setCompletionType(PGSubmission.Types.COUNT);
    connection.addSubmissionOnQue(submission);
    return submission;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
