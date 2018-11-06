package org.postgresql.adba.submissions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collector;
import jdk.incubator.sql2.SessionProperty;
import org.postgresql.adba.PgSessionProperty;
import org.postgresql.adba.PgSubmission;
import org.postgresql.adba.communication.NetworkConnect;
import org.postgresql.adba.communication.network.NetworkConnectRequest;
import org.postgresql.adba.communication.network.TlsConnectRequest;
import org.postgresql.adba.communication.packets.DataRow;
import org.postgresql.adba.operations.helpers.ParameterHolder;

public class ConnectSubmission implements PgSubmission<Void> {

  private final Supplier<Boolean> cancel;
  private CompletableFuture<Void> publicStage;
  private final AtomicBoolean sendConsumed = new AtomicBoolean(false);

  private Collector collector;
  private Object collectorHolder;
  private Consumer<Throwable> errorHandler;

  private GroupSubmission groupSubmission;
  
  private NetworkConnect request;

  /**
   * Creates the connect submission.
   * @param cancel cancel method
   * @param errorHandler error handler method
   * @param groupSubmission group submission this submission is a part of
   * @param properties properties for the connection, to determine if tls should be used or not
   */
  public ConnectSubmission(Supplier<Boolean> cancel, Consumer<Throwable> errorHandler, GroupSubmission groupSubmission,
      Map<SessionProperty, Object> properties) {
    this.cancel = cancel;
    this.errorHandler = errorHandler;
    this.groupSubmission = groupSubmission;
    if (properties.containsKey(PgSessionProperty.SSL) && (Boolean)properties.get(PgSessionProperty.SSL)) {
      this.request = new TlsConnectRequest(this);
    } else {
      this.request = new NetworkConnectRequest(this);
    }

    if (groupSubmission != null) {
      groupSubmission.stackFuture((CompletableFuture<Void>) getCompletionStage());
    }
  }
  
  public NetworkConnect getNetworkConnect() {
    return request;
  }

  @Override
  public CompletionStage<Boolean> cancel() {
    return new CompletableFuture<Boolean>().completeAsync(cancel);
  }

  @Override
  public CompletionStage<Void> getCompletionStage() {
    if (publicStage == null) {
      publicStage = new CompletableFuture<>();
    }

    return publicStage;
  }

  @Override
  public String getSql() {
    return null;
  }

  @Override
  public AtomicBoolean getSendConsumed() {
    return sendConsumed;
  }

  @Override
  public ParameterHolder getHolder() {
    return null;
  }

  @Override
  public Types getCompletionType() {
    return BaseSubmission.Types.CONNECT;
  }

  @Override
  public void setCollector(Collector collector) {
    this.collector = collector;

    collectorHolder = collector.supplier().get();
  }

  @Override
  public Object finish(Object finishObject) {
    ((CompletableFuture<Void>) getCompletionStage()).complete(null);
    return null;
  }

  @Override
  public void addRow(DataRow row) {
    try {
      collector.accumulator().accept(collectorHolder, row);
    } catch (Throwable e) {
      publicStage.completeExceptionally(e);
    }
  }

  @Override
  public List<Integer> getParamTypes() throws ExecutionException, InterruptedException {
    return null;
  }

  @Override
  public int numberOfQueryRepetitions() throws ExecutionException, InterruptedException {
    return 1;
  }

  @Override
  public Consumer<Throwable> getErrorHandler() {
    return errorHandler;
  }

}
