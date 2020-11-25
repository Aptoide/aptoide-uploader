package com.aptoide.uploader.apps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class InstalledBroadcastReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    val callService = Intent(intent)
    callService.setClassName(context, InstalledIntentService::class.java.getName())
    context.startService(callService)
    // from the documentation @ https://developer.android.com/reference/android/content/BroadcastReceiver.html#ProcessLifecycle
    /*
    This means that for longer-running operations you will often use a Service in conjunction with a
    BroadcastReceiver to keep the containing process active for the entire time of your operation.
    */
  }
}