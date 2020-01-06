package com.aptoide.uploader.apps.network;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import java.util.concurrent.TimeUnit;

public class RetryWithDelay implements Function<Observable<? extends Throwable>, Observable<?>> {

  public static final int BASE_FOR_RETRIES = 2;
  public static final long TIME_INTERVAL_IN_SEC_FOR_RETRIES = 30L;

  public static final int FIRST_MAX_RETRIES_FOR_GET_STATUS = 6;
  public static final int SECOND_MAX_RETRIES_FOR_GET_STATUS = 10;

  private int retryCount;

  public RetryWithDelay() {
    this.retryCount = 0;
  }

  @Override public Observable<?> apply(final Observable<? extends Throwable> attempts) {
    return attempts.flatMap((Function<Throwable, Observable<?>>) throwable -> {
      if (++retryCount < FIRST_MAX_RETRIES_FOR_GET_STATUS) {
        return Observable.timer((long) Math.pow(BASE_FOR_RETRIES, retryCount), TimeUnit.SECONDS);
      } else if (retryCount < SECOND_MAX_RETRIES_FOR_GET_STATUS) {
        return Observable.timer(TIME_INTERVAL_IN_SEC_FOR_RETRIES, TimeUnit.SECONDS);
      }
      return Observable.error(new GetApksRetryException());
    });
  }
}