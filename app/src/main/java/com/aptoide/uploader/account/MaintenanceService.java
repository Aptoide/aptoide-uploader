package com.aptoide.uploader.account;

import io.reactivex.Observable;
import retrofit2.http.GET;

public class MaintenanceService {

  private LoginMaintenanceService loginMaintenanceService;

  public MaintenanceService(LoginMaintenanceService loginMaintenanceService) {
    this.loginMaintenanceService = loginMaintenanceService;
  }

  public Observable<LoginMaintenanceResponse> getLoginMaintenanceStatus() {
    return loginMaintenanceService.getLoginMaintenanceStatus();
  }

  public interface LoginMaintenanceService {
    @GET("uploader_login_v2_106.json")
    Observable<LoginMaintenanceResponse> getLoginMaintenanceStatus();
  }
}
