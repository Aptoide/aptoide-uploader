package com.aptoide.uploader.view

import com.aptoide.uploader.IntentData
import io.reactivex.Observable

interface IntentView : View {
  fun getIntentEvents(): Observable<IntentData>
}