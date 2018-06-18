package org.postgresql.sql2.operations;

import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Result;
import jdk.incubator.sql2.RowOperation;
import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.PGSubmission;
import org.postgresql.sql2.submissions.BaseSubmission;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collector;

public class PGRowOperation<T> implements RowOperation<T> {
  private final static Collector<Result.Row, List<Map<String, Object>>, List<Map<String, Object>>> defaultCollector = Collector.of(
      () -> new ArrayList<>(),
      (a, v) -> {
        Map<String, Object> row = new HashMap<>();
        for(String column : v.getIdentifiers()) {
          row.put(column, v.get(column, Object.class));
        }
        a.add(row);
      },
      (a, b) -> null,
      a -> a);

  private PGCountOperation parentOperation;
  private Consumer<Throwable> errorHandler;
  private Collector collector = defaultCollector;

  public PGRowOperation(PGCountOperation parentOperation, String... keys) {
    this.parentOperation = parentOperation;
  }

  @Override
  public RowOperation<T> fetchSize(long rows) throws IllegalArgumentException {
    return this;
  }

  @Override
  public <A, S extends T> RowOperation<T> collect(Collector<? super Result.Row, A, S> c) {
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
    PGSubmission<T> submission = new BaseSubmission<>(this::cancel, PGSubmission.Types.ROW, errorHandler, null, null, null);
    submission.setCollector(collector);
    parentOperation.addReturningRowSubmission(submission);
    return submission;
  }

  private boolean cancel() {
    // todo set life cycle to canceled
    return true;
  }
}
