package com.aptoide.uploader.apps

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.text.TextUtils
import android.util.Log
import com.aptoide.uploader.UploaderApplication
import io.reactivex.disposables.CompositeDisposable

class InstalledIntentService : IntentService("InstalledIntentService") {
  lateinit var myPackageManager: PackageManager
  lateinit var installManager: InstallManager
  var compositeDisposable: CompositeDisposable = CompositeDisposable()
  override fun onCreate() {
    super.onCreate()
    installManager = (applicationContext as UploaderApplication).installManager
    myPackageManager = packageManager
    //installManager.insertAllInstalled()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    super.onStartCommand(intent, flags, startId)
    return Service.START_STICKY
  }

  override fun onHandleIntent(intent: Intent?) {
    if (intent != null) {
      val action = intent.action
      val packageName = intent.data
          .encodedSchemeSpecificPart
      if (!TextUtils.equals(action, Intent.ACTION_PACKAGE_REPLACED) && intent.getBooleanExtra(
              Intent.EXTRA_REPLACING, false)) {
        return
      }
      installManager.insertAllInstalled()
      when (action) {
        Intent.ACTION_PACKAGE_ADDED -> onPackageAdded(packageName)
        Intent.ACTION_PACKAGE_REPLACED -> onPackageReplaced(packageName)
        Intent.ACTION_PACKAGE_REMOVED -> onPackageRemoved(packageName)
      }
    }
  }

  override fun onDestroy() {
    //compositeDisposable.dispose()
    Log.d("APP-85", "onDestroy: InstalledIntentService")
    super.onDestroy()
  }

  protected fun onPackageAdded(packageName: String) {
    Log.d("APP-85", "InstalledIntentService: Package added: $packageName")
    databaseOnPackageAdded(packageName)
  }

  protected fun onPackageReplaced(packageName: String) {
    Log.d("APP-85", "InstalledIntentService: Packaged replaced: $packageName")
    databaseOnPackageReplaced(packageName)
  }

  protected fun onPackageRemoved(packageName: String) {
    Log.d("APP-85", "InstalledIntentService: Packaged removed: $packageName")
    databaseOnPackageRemoved(packageName)
  }

  private fun databaseOnPackageAdded(packageName: String): PackageInfo {
    val packageInfo: PackageInfo = myPackageManager.getPackageInfo(packageName, 0);
    if (checkNullPackageInfo(packageInfo)) {
      return packageInfo
    }
    val installed = InstalledApp(packageInfo, packageManager)
    compositeDisposable.add(installManager.onAppInstalled(installed)
        .subscribe({
        }, { throwable -> throwable.printStackTrace() }))
    return packageInfo
  }

  private fun databaseOnPackageReplaced(packageName: String): PackageInfo {
    val packageInfo: PackageInfo = myPackageManager.getPackageInfo(applicationInfo.packageName, 0);
    if (checkNullPackageInfo(packageInfo)) {
      return packageInfo
    }
    compositeDisposable.add(
        installManager.onUpdateConfirmed(InstalledApp(packageInfo, packageManager))
            .subscribe({
            }, { throwable -> throwable.printStackTrace() }))
    return packageInfo
  }

  private fun databaseOnPackageRemoved(packageName: String) {
    compositeDisposable.add(installManager.onAppRemoved(packageName)
        .subscribe({
        }, { throwable -> throwable.printStackTrace() }))
  }

  private fun checkNullPackageInfo(packageInfo: PackageInfo?): Boolean {
    return packageInfo == null
  }
}