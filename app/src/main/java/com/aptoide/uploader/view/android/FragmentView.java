package com.aptoide.uploader.view.android;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class FragmentView extends Fragment implements View {

  private Subject<View.LifecycleEvent> events;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    events = BehaviorSubject.create();
  }

  @Override public void onViewCreated(android.view.View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    events.onNext(View.LifecycleEvent.CREATE);
  }

  @Override public void onStart() {
    super.onStart();
    events.onNext(View.LifecycleEvent.START);
  }

  @Override public void onResume() {
    super.onResume();
    events.onNext(View.LifecycleEvent.RESUME);
  }

  @Override public void onPause() {
    events.onNext(View.LifecycleEvent.PAUSE);
    super.onPause();
  }

  @Override public void onStop() {
    events.onNext(View.LifecycleEvent.STOP);
    super.onStop();
  }

  @Override public void onDestroyView() {
    events.onNext(View.LifecycleEvent.DESTROY);
    super.onDestroyView();
  }

  @Override public Observable<View.LifecycleEvent> getLifecycleEvent() {
    return events;
  }
}
