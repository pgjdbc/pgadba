package org.postgresql.adba.testutil;

import jdk.incubator.sql2.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

public class SimpleRowSubscriber implements Flow.Subscriber<Result.RowColumn> {

  private Flow.Subscription subscription;
  private Integer columnTSum = 0;
  private int demand = 0;
  private CompletableFuture<Integer> result;

  public SimpleRowSubscriber(CompletableFuture<Integer> result) {
    this.result = result;
  }

  @Override
  public void onSubscribe(Flow.Subscription subscription) {
    this.subscription = subscription;
    this.subscription.request(10);
    demand += 10;
  }

  @Override
  public void onNext(Result.RowColumn column) {
    columnTSum += column.at("t").get(Integer.class);
    if (--demand < 1) {
      subscription.request(10);
      demand += 10;
    }
  }

  @Override
  public void onError(Throwable throwable) {
    result.completeExceptionally(throwable);
  }

  @Override
  public void onComplete() {
    result.complete(columnTSum);
  }

}

