package com.aptoide.uploader.apps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class InstalledBroadcastReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    val callService = Intent(intent)
    callService.setClassName(context, InstalledIntentService::class.java.name)
    context.startService(callService)
  }
}