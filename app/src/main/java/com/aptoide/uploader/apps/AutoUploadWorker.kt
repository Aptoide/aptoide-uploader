package com.aptoide.uploader.apps

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.aptoide.uploader.UploaderApplication
import java.util.concurrent.TimeUnit

class AutoUploadWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

  companion object {
    private const val TAG = "AutoUploadWorker"
    private const val KEY_PACKAGE_NAME = "packageName"
    private const val MAX_RETRIES = 5

    @JvmStatic
    fun enqueue(context: Context, packageName: String) {
      val constraints = Constraints.Builder()
          .setRequiredNetworkType(NetworkType.UNMETERED)
          .build()

      val data = Data.Builder()
          .putString(KEY_PACKAGE_NAME, packageName)
          .build()

      val request = OneTimeWorkRequestBuilder<AutoUploadWorker>()
          .setConstraints(constraints)
          .setInputData(data)
          .addTag("auto_upload_$packageName")
          .setInitialDelay(0, TimeUnit.SECONDS)
          .build()

      WorkManager.getInstance(context)
          .enqueue(request)
    }
  }

  override fun doWork(): Result {
    val packageName = inputData.getString(KEY_PACKAGE_NAME) ?: return Result.failure()
    Log.d(TAG, "Starting auto-upload for: $packageName")

    val app = applicationContext as UploaderApplication
    val storeManager = app.appsManager
    val installedAppsManager = app.installedAppsManager
    val dao = app.pendingAutoUploadDao

    try {
      // Re-check selection status (user may have deselected since enqueue)
      if (!installedAppsManager.isSelectedApp(packageName)) {
        Log.d(TAG, "App no longer selected for auto-upload: $packageName")
        dao.remove(packageName)
        return Result.success()
      }

      // Get the installed app info from the package manager
      val packageInfo = applicationContext.packageManager.getPackageInfo(packageName, 0)
      val installed = InstalledAppBuilder(packageInfo, applicationContext.packageManager).installedApp

      // Check if already uploaded at this version
      if (installedAppsManager.isUploadedVersion(packageName, installed.versionCode)) {
        Log.d(TAG, "Version already uploaded: $packageName v${installed.versionCode}")
        dao.remove(packageName)
        return Result.success()
      }

      // Trigger the upload through the existing pipeline
      storeManager.upload(installed).blockingAwait()

      // Clean up the pending record on success
      dao.remove(packageName)
      Log.d(TAG, "Auto-upload completed for: $packageName")
      return Result.success()
    } catch (e: Exception) {
      Log.e(TAG, "Auto-upload failed for: $packageName", e)
      return if (runAttemptCount < MAX_RETRIES) {
        Result.retry()
      } else {
        dao.remove(packageName)
        Result.failure()
      }
    }
  }
}
