package com.aptoide.uploader.account;

import io.reactivex.Observable;

public class MaintenanceManager {

  private final MaintenanceService maintenanceService;

  public MaintenanceManager(MaintenanceService maintenanceService) {
    this.maintenanceService = maintenanceService;
  }

  public Observable<Boolean> shouldShowSocialLogin() {
    return maintenanceService.getLoginMaintenanceStatus()
        .map(loginMaintenanceResponse -> loginMaintenanceResponse.isShowLogin());
  }
}
