package com.aptoide.uploader;

import io.reactivex.Observable;

public interface View {

  Observable<LifecycleEvent> getLifecycle();

  enum LifecycleEvent {
    CREATE, START, RESUME, PAUSE, STOP, DESTROY,
  }
}
