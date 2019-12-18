package com.aptoide.uploader.apps.network;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import java.util.concurrent.TimeUnit;

public class RetryWithDelay implements Function<Observable<? extends Throwable>, Observable<?>> {
  private final int maxRetries;
  private int retryCount;

  public RetryWithDelay(final int maxRetries) {
    this.maxRetries = maxRetries;
    this.retryCount = 0;
  }

  @Override public Observable<?> apply(final Observable<? extends Throwable> attempts) {
    return attempts.flatMap((Function<Throwable, Observable<?>>) throwable -> {
      if (++retryCount < maxRetries) {
        return Observable.timer((long) Math.pow(4, retryCount), TimeUnit.SECONDS);
      }
      return Observable.error(new GetApksRetryException());
    });
  }
}