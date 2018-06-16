package org.postgresql.sql2;

import jdk.incubator.sql2.Submission;
import org.postgresql.sql2.communication.packets.DataRow;
import org.postgresql.sql2.operations.helpers.ParameterHolder;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collector;

public interface PGSubmission<T> extends Submission<T> {
  enum Types {
    COUNT,
    ROW,
    CLOSE,
    CONNECT,
    TRANSACTION,
    ARRAY_COUNT,
    VOID,
    PROCESSOR,
    OUT_PARAMETER,
    LOCAL,
    GROUP;
  }

  void setSql(String sql);

  String getSql();

  AtomicBoolean getSendConsumed();

  ParameterHolder getHolder();

  Types getCompletionType();

  void setCollector(Collector collector);

  Object finish(Object finishObject);

  void addRow(DataRow row);

  List<Integer> getParamTypes() throws ExecutionException, InterruptedException;

  int numberOfQueryRepetitions() throws ExecutionException, InterruptedException;

  Consumer<Throwable> getErrorHandler();
}
