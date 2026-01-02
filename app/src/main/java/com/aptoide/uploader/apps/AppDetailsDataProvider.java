package com.aptoide.uploader.apps;

import android.os.Build;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppDetailsDataProvider {

  /**
   * Calculate total app size including base APK and all split APKs
   */
  public long calculateAppSize(InstalledApp app) {
    long size = 0;
    
    // Base APK size
    File baseApk = new File(app.getApkPath());
    if (baseApk.exists()) {
      size += baseApk.length();
    }
    
    // Split APKs size (if any)
    if (app.getAppInfo() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      String[] splits = app.getAppInfo().splitSourceDirs;
      if (splits != null) {
        for (String splitPath : splits) {
          File splitFile = new File(splitPath);
          if (splitFile.exists()) {
            size += splitFile.length();
          }
        }
      }
    }
    
    return size;
  }

  /**
   * Format file size to human readable format
   */
  public String formatFileSize(long bytes) {
    if (bytes < 1024) {
      return bytes + " B";
    } else if (bytes < 1024 * 1024) {
      return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
    } else if (bytes < 1024 * 1024 * 1024) {
      return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
    } else {
      return String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
  }

  /**
   * Check if app is installed as App Bundle (has split APKs)
   */
  public boolean isAppBundle(InstalledApp app) {
    if (app.getAppInfo() == null) {
      return false;
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      String[] splits = app.getAppInfo().splitSourceDirs;
      return splits != null && splits.length > 0;
    }
    return false;
  }

  /**
   * Get all file paths (base APK + splits)
   */
  public List<String> getAllFilePaths(InstalledApp app) {
    List<String> paths = new ArrayList<>();
    
    // Add base APK
    paths.add(app.getApkPath());
    
    // Add split APKs
    if (app.getAppInfo() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      String[] splits = app.getAppInfo().splitSourceDirs;
      if (splits != null) {
        for (String splitPath : splits) {
          paths.add(splitPath);
        }
      }
    }
    
    return paths;
  }

  /**
   * Format timestamp to readable date string
   */
  public String formatDate(long timestamp) {
    if (timestamp == 0) {
      return "Unknown";
    }
    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);
    return sdf.format(new Date(timestamp));
  }

  /**
   * Get the number of split APKs
   */
  public int getSplitCount(InstalledApp app) {
    if (app.getAppInfo() == null) {
      return 0;
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      String[] splits = app.getAppInfo().splitSourceDirs;
      return splits != null ? splits.length : 0;
    }
    return 0;
  }
}
