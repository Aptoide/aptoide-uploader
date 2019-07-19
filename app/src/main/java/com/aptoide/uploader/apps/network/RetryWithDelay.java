package com.aptoide.uploader.apps.network;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import java.util.concurrent.TimeUnit;

public class RetryWithDelay implements Function<Observable<? extends Throwable>, Observable<?>> {
  private final int maxRetries;
  private final int retryDelayMillis;
  private int retryCount;

  public RetryWithDelay(final int maxRetries, final int retryDelayMillis) {
    this.maxRetries = maxRetries;
    this.retryDelayMillis = retryDelayMillis;
    this.retryCount = 0;
  }

  @Override public Observable<?> apply(final Observable<? extends Throwable> attempts) {
    return attempts.flatMap((Function<Throwable, Observable<?>>) throwable -> {
      if (++retryCount < maxRetries) {
        return Observable.timer(retryDelayMillis, TimeUnit.MILLISECONDS);
      }
      return Observable.error(throwable);
    });
  }
}