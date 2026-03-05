package com.aptoide.uploader.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aptoide.uploader.UploaderApplication

class PackageEventWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

  companion object {
    private const val TAG = "PackageEventWorker"
    private const val KEY_ACTION = "action"
    private const val KEY_PACKAGE_NAME = "packageName"

    @JvmStatic
    fun enqueue(context: Context, action: String, packageName: String) {
      val data = Data.Builder()
          .putString(KEY_ACTION, action)
          .putString(KEY_PACKAGE_NAME, packageName)
          .build()

      val request = OneTimeWorkRequestBuilder<PackageEventWorker>()
          .setInputData(data)
          .build()

      WorkManager.getInstance(context).enqueue(request)
    }
  }

  override fun doWork(): Result {
    val action = inputData.getString(KEY_ACTION) ?: return Result.failure()
    val packageName = inputData.getString(KEY_PACKAGE_NAME) ?: return Result.failure()

    val app = applicationContext as UploaderApplication
    val installManager = app.installManager
    val packageManager = applicationContext.packageManager

    try {
      when (action) {
        Intent.ACTION_PACKAGE_ADDED -> {
          Log.d(TAG, "Package added: $packageName")
          val packageInfo = packageManager.getPackageInfo(packageName, 0)
          val installed = InstalledAppBuilder(packageInfo, packageManager).installedApp
          installManager.onAppInstalled(installed).blockingAwait()
        }
        Intent.ACTION_PACKAGE_REPLACED -> {
          Log.d(TAG, "Package replaced: $packageName")
          val packageInfo = packageManager.getPackageInfo(packageName, 0)
          val installed = InstalledAppBuilder(packageInfo, packageManager).installedApp

          // Update DB only (no upload trigger — that's handled by AutoUploadWorker)
          installManager.onUpdateConfirmedWithoutUpload(installed).blockingAwait()

          // Check if this app should be auto-uploaded
          val installedAppsManager = app.installedAppsManager
          if (!installedAppsManager.isUploadedVersion(packageName, installed.versionCode)
              && installedAppsManager.isSelectedApp(packageName)) {
            Log.d(TAG, "App selected for auto-upload, enqueueing: $packageName")
            val dao = app.pendingAutoUploadDao
            dao.insert(PendingAutoUpload(packageName, System.currentTimeMillis()))
            AutoUploadWorker.enqueue(applicationContext, packageName)
          }
        }
        Intent.ACTION_PACKAGE_REMOVED -> {
          Log.d(TAG, "Package removed: $packageName")
          installManager.onAppRemoved(packageName).blockingAwait()
        }
      }
    } catch (e: PackageManager.NameNotFoundException) {
      Log.e(TAG, "Package not found: $packageName", e)
      return Result.failure()
    } catch (e: Exception) {
      Log.e(TAG, "Error processing package event for: $packageName", e)
      return Result.failure()
    }

    return Result.success()
  }
}
