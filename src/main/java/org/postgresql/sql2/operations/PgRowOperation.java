package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.RowOperation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PgSubmission;
import org.postgresql.sql2.submissions.BaseSubmission;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collector;

public class PgRowOperation<T> implements RowOperation<T> {
  private static final Collector<Result.RowColumn, List<Map<String, Object>>, List<Map<String, Object>>> defaultCollector =
      Collector.of(
        () -> new ArrayList<>(),
        (a, v) -> {
          Map<String, Object> row = new HashMap<>();
          v.forEach(column -> row.put(column.identifier(), v.get(Object.class)));
          a.add(row);
        },
        (a, b) -> null,
        a -> a);

  private PgRowCountOperation parentOperation;
  private Consumer<Throwable> errorHandler;
  private Collector collector = defaultCollector;

  public PgRowOperation(PgRowCountOperation parentOperation, String... keys) {
    this.parentOperation = parentOperation;
  }

  @Override
  public RowOperation<T> fetchSize(long rows) throws IllegalArgumentException {
    return this;
  }

  @Override
  public <A, S extends T> RowOperation<T> collect(Collector<? super Result.RowColumn, A, S> c) {
    this.collector = c;
    return this;
  }

  @Override
  public RowOperation<T> onError(Consumer<Throwable> errorHandler) {
    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public Operation<T> timeout(Duration minTime) {
    return this;
  }

  @Override
  public Submission<T> submit() {
    PgSubmission<T> submission = new BaseSubmission<>(this::cancel, PgSubmission.Types.ROW, errorHandler, null, null, null);
    submission.setCollector(collector);
    parentOperation.addReturningRowSubmission(submission);
    return submission;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
