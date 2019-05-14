package org.postgresql.adba.testutil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import jdk.incubator.sql2.Result;

public class SimpleRowSubscriber implements Flow.Subscriber<Result.RowColumn> {

  private Flow.Subscription subscription;
  private Integer columnTSum = 0;
  private int demand = 0;
  private Consumer<String> fail;
  private CountDownLatch conditionLatch = new CountDownLatch(1);

  public SimpleRowSubscriber(Consumer<String> fail) {
    this.fail = fail;
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
    fail.accept(throwable.getMessage());
  }

  @Override
  public void onComplete() {
    conditionLatch.countDown();
  }

  public Integer getColumnTSum() throws InterruptedException {
    conditionLatch.await();
    return columnTSum;
  }
}

