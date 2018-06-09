package org.postgresql.sql2.testUtil;

import jdk.incubator.sql2.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

public class SimpleRowProcessor implements Flow.Processor<Result.Row, Integer> {
  private Integer sum = 0;
  private Flow.Subscription subscription;
  private List<Flow.Subscriber<? super Integer>> subscribers = new ArrayList<>();

  @Override
  public void subscribe(Flow.Subscriber<? super Integer> subscriber) {
    subscribers.add(subscriber);
    subscriber.onSubscribe(new Flow.Subscription() {
      @Override
      public void request(long n) {

      }

      @Override
      public void cancel() {

      }
    });
  }

  @Override
  public void onSubscribe(Flow.Subscription subscription) {
    this.subscription = subscription;
    subscription.request(1);
  }

  @Override
  public void onNext(Result.Row item) {
    sum += item.get("t", Integer.class);
    subscription.request(1);

    for(Flow.Subscriber<? super Integer> s : subscribers) {
      s.onNext(sum);
    }
  }

  @Override
  public void onComplete() {
    for(Flow.Subscriber<? super Integer> s : subscribers) {
      s.onComplete();
    }
  }

  @Override
  public void onError(Throwable t) {
    for(Flow.Subscriber<? super Integer> s : subscribers) {
      s.onError(t);
    }
  }
}
