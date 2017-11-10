package com.aptoide.uploader;

import io.reactivex.Observable;

public interface View {

  Observable<LifecycleEvent> getLifecycleEvent();

  enum LifecycleEvent {
    CREATE, START, RESUME, PAUSE, STOP, DESTROY,
  }
}
