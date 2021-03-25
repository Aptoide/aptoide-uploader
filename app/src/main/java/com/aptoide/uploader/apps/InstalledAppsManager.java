package com.aptoide.uploader.apps;

import com.aptoide.uploader.apps.persistence.AppUploadStatusPersistence;
import com.aptoide.uploader.apps.persistence.AutoUploadSelectsPersistence;
import com.aptoide.uploader.apps.persistence.InstalledPersistence;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import java.util.List;

public class InstalledAppsManager {

  private final InstalledPersistence installedPersistence;
  private final AppUploadStatusPersistence uploadPersistence;
  private final AutoUploadSelectsPersistence selectedPersistence;
  private Scheduler scheduler;

  public InstalledAppsManager(InstalledPersistence installedPersistence,
      AppUploadStatusPersistence uploadPersistence,
      AutoUploadSelectsPersistence selectedPersistence, Scheduler scheduler) {
    this.installedPersistence = installedPersistence;
    this.uploadPersistence = uploadPersistence;
    this.selectedPersistence = selectedPersistence;
    this.scheduler = scheduler;
  }

  public Observable<InstalledAppsStatus> getInstalledAppsStatus() {
    return Observable.zip(getInstalledAppsPersistence(), getAppUploadStatusPersistence(),
        getAutoUploadSelectsPersistence(),
        (apps, uploadStatus, autoUploadSelects) -> new InstalledAppsStatus(apps, uploadStatus,
            autoUploadSelects))
        .subscribeOn(scheduler);
  }

  private Observable<List<InstalledApp>> getInstalledAppsPersistence() {
    return installedPersistence.allInstalledSorted();
  }

  private Observable<List<AppUploadStatus>> getAppUploadStatusPersistence() {
    return uploadPersistence.getAppsUploadStatus();
  }

  private Observable<List<AutoUploadSelects>> getAutoUploadSelectsPersistence() {
    return selectedPersistence.getAllAutoUploadSelectStatus();
  }

  public Completable replaceSelectsListOnPersistence(List<AutoUploadSelects> selectsList) {
    return selectedPersistence.replaceAllBy(selectsList);
  }

  public Observable<List<String>> getUploadedFromAppUploadStatusPersistence() {
    return uploadPersistence.getUploadedApps()
        .distinctUntilChanged(
            (previous, current) -> !uploadedPersistenceHasChanged(previous, current))
        .flatMapSingle(apps -> Observable.fromIterable(apps)
            .map(appUploaded -> appUploaded.getPackageName())
            .toList());
  }

  public Observable<List<String>> getSelectedFromAutoUploadSelectsPersistence() {
    return selectedPersistence.getSelectedApps()
        .distinctUntilChanged(
            (previous, current) -> !selectedPersistenceHasChanged(previous, current))
        .flatMapSingle(apps -> Observable.fromIterable(apps)
            .map(appSelected -> appSelected.getPackageName())
            .toList());
  }

  public Observable<List<InstalledApp>> getSelectedInstalledApps() {
    return installedPersistence.allInstalledSorted()
        .firstOrError()
        .flatMapObservable(installedApps -> Observable.fromIterable(installedApps))
        .filter(installedApp -> isSelectedApp(installedApp.getPackageName()))
        .toList()
        .toObservable();
  }

  private boolean isSelectedApp(String installedPackageName) {
    return selectedPersistence.isSelectedApp(installedPackageName);
  }

  private boolean uploadedPersistenceHasChanged(List<AppUploadStatus> previousList,
      List<AppUploadStatus> currentList) {
    if (previousList.size() != currentList.size()) {
      return true;
    }
    for (AppUploadStatus previous : previousList) {
      AppUploadStatus current = currentList.get(previousList.indexOf(previous));
      if (!previous.getPackageName()
          .equals(current.getPackageName()) && !(previous.isUploaded() == current.isUploaded())) {
        return true;
      }
    }
    return !previousList.equals(currentList);
  }

  private boolean selectedPersistenceHasChanged(List<AutoUploadSelects> previousList,
      List<AutoUploadSelects> currentList) {
    if (previousList.size() != currentList.size()) {
      return true;
    }
    for (AutoUploadSelects previous : previousList) {
      AutoUploadSelects current = currentList.get(previousList.indexOf(previous));
      if (!previous.getPackageName()
          .equals(current.getPackageName()) && !(previous.isSelectedAutoUpload()
          == current.isSelectedAutoUpload())) {
        return true;
      }
    }
    return !previousList.equals(currentList);
  }
}
