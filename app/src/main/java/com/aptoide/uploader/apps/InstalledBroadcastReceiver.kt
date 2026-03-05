package com.aptoide.uploader.apps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils

class InstalledBroadcastReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    if (intent == null) return

    val action = intent.action ?: return
    val packageName = intent.data?.encodedSchemeSpecificPart ?: return

    // Filter out PACKAGE_ADDED events that are part of a PACKAGE_REPLACED sequence
    if (!TextUtils.equals(action, Intent.ACTION_PACKAGE_REPLACED)
        && intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
      return
    }

    PackageEventWorker.enqueue(context, action, packageName)
  }
}
