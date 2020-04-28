package com.aptoide.uploader.account;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class MaintenanceManager {

  private final MaintenanceService maintenanceService;
  private final AptoideAccountManager accountManager;
  private final MaintenancePersistence maintenancePersistence;

  public MaintenanceManager(MaintenanceService maintenanceService,
      AptoideAccountManager accountManager, MaintenancePersistence maintenancePersistence) {
    this.maintenanceService = maintenanceService;
    this.accountManager = accountManager;
    this.maintenancePersistence = maintenancePersistence;
  }

  public Observable<Boolean> shouldShowSocialLogin() {
    return maintenanceService.getLoginMaintenanceStatus()
        .map(loginMaintenanceResponse -> loginMaintenanceResponse.isShowLogin());
  }

  public Completable logoutUser() {
    if (shouldLogout()) {
      return accountManager.logout()
          .doOnComplete(() -> maintenancePersistence.saveLogout());
    }
    return Completable.complete();
  }

  public boolean shouldLogout() {
    return maintenancePersistence.shouldLogout();
  }
}
