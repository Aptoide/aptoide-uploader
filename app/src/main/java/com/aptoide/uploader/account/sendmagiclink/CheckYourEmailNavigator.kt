package com.aptoide.uploader.account.sendmagiclink

import android.app.Activity
import android.content.Intent

class CheckYourEmailNavigator(private val activity: Activity?) {

  fun navigateToEmailApp() {
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_APP_EMAIL)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    activity?.startActivity(intent)
  }
}