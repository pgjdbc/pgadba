package org.postgresql.sql2.testutil;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureUtil {
  public static <T> T get10(CompletionStage<T> cs) throws InterruptedException, ExecutionException, TimeoutException {
    return cs.toCompletableFuture().get(10, TimeUnit.SECONDS);
  }
}
