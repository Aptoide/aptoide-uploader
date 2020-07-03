package com.aptoide.uploader

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aptoide.uploader.view.IntentView
import com.aptoide.uploader.view.View.LifecycleEvent
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

abstract class ActivityView : AppCompatActivity(), IntentView {
  private val lifecycleEvents: Subject<LifecycleEvent> = BehaviorSubject.create()
  private val intentEvents: Subject<IntentData> = BehaviorSubject.create()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycleEvents.onNext(LifecycleEvent.CREATE)
    intentEvents.onNext(parseIntent(intent))
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    intent?.let { i -> intentEvents.onNext(parseIntent(i)) }
  }

  override fun onResume() {
    super.onResume()
    lifecycleEvents.onNext(LifecycleEvent.RESUME)
  }

  override fun onPause() {
    lifecycleEvents.onNext(LifecycleEvent.PAUSE)
    super.onPause()
  }

  override fun onStart() {
    super.onStart()
    lifecycleEvents.onNext(LifecycleEvent.START)
  }

  override fun onStop() {
    lifecycleEvents.onNext(LifecycleEvent.STOP)
    super.onStop()
  }

  override fun onDestroy() {
    lifecycleEvents.onNext(LifecycleEvent.DESTROY)
    super.onDestroy()
  }

  override fun getLifecycleEvent(): Observable<LifecycleEvent> {
    return lifecycleEvents
  }

  override fun getIntentEvents(): Observable<IntentData> {
    return intentEvents
  }

  private fun parseIntent(intent: Intent): IntentData {
    val extrasMap = HashMap<String, Any?>()
    intent.extras?.let { extras ->
      for (key in extras.keySet()) {
        extrasMap[key] = extras.get(key)
      }
    }
    return IntentData(intent.action ?: "", intent.dataString ?: "", extrasMap)
  }
}