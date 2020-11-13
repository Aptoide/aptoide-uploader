package com.aptoide.uploader.apps

import android.app.IntentService
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.text.TextUtils
import android.util.Log
import com.aptoide.uploader.UploaderApplication
import io.reactivex.Observable.just
import java.util.*

class InstalledIntentService(name: String) : IntentService(name) {
  lateinit var myPackageManager: PackageManager
  lateinit var installManager: InstallManager
  override fun onCreate() {
    super.onCreate()
    Log.d("APP-85", "InstalledIntentService onCreate")
    installManager = (applicationContext as UploaderApplication).installManager
    myPackageManager = (applicationContext as UploaderApplication).packageManager
  }

  override fun onHandleIntent(intent: Intent?) {
    Log.d("APP-85", "InstalledIntentService onHandleIntent")
    if (intent != null) {
      val action = intent.action
      val packageName = intent.data
          .encodedSchemeSpecificPart
      if (!TextUtils.equals(action, Intent.ACTION_PACKAGE_REPLACED) && intent.getBooleanExtra(
              Intent.EXTRA_REPLACING, false)) {
        // do nothing if its a replacement ongoing. we are only interested in
        // already replaced apps
        return
      }
      when (action) {
        Intent.ACTION_PACKAGE_ADDED -> onPackageAdded(packageName)
        Intent.ACTION_PACKAGE_REPLACED -> onPackageReplaced(packageName)
        Intent.ACTION_PACKAGE_REMOVED -> onPackageRemoved(packageName)
      }
    }
  }

  protected fun onPackageAdded(packageName: String) {
    Log.d("APP-85", "Package added: $packageName")
    databaseOnPackageAdded(packageName)
  }

  protected fun onPackageReplaced(packageName: String) {
    Log.d("APP-85", "Packaged replaced: $packageName")
    databaseOnPackageReplaced(packageName)
  }

  protected fun onPackageRemoved(packageName: String) {
    Log.d("APP-85", "Packaged removed: $packageName")
    databaseOnPackageRemoved(packageName)
  }
  private fun databaseOnPackageAdded(packageName: String): PackageInfo {
    val packageInfo: PackageInfo = myPackageManager.getPackageInfo(applicationInfo.packageName, 0);
    if (checkNullPackageInfo(packageInfo)) {1
      return packageInfo
    }
    val installed = RoomInstalled(packageInfo, packageManager)
    installManager.onAppInstalled(installed)!!
        .subscribe({}, { throwable -> throwable.printStackTrace() })
    return packageInfo
  }
  private fun databaseOnPackageReplaced(packageName: String): PackageInfo {
    val packageInfo: PackageInfo = myPackageManager.getPackageInfo(applicationInfo.packageName, 0);
    if (checkNullPackageInfo(packageInfo)) {
      return packageInfo
    }
    installManager.onUpdateConfirmed(RoomInstalled(packageInfo, packageManager))!!
        .subscribe({
          Log.d("APP-85", "databaseOnPackageReplaced: $packageName")
        }, { throwable -> throwable.printStackTrace() })
    return packageInfo
  }

  private fun databaseOnPackageRemoved(packageName: String) {
    installManager.onAppRemoved(packageName)!!
        .subscribe({
          Log.d("APP-85", "databaseOnPackageRemoved: $packageName")
        }, { throwable -> throwable.printStackTrace() })
  }

  private fun checkNullPackageInfo(packageInfo: PackageInfo?): Boolean {
    return packageInfo == null
  }
}