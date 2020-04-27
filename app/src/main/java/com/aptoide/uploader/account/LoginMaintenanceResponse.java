package com.aptoide.uploader.account;

import com.squareup.moshi.Json;

class LoginMaintenanceResponse {
  @Json(name = "show_login") private boolean showLogin;

  public LoginMaintenanceResponse(boolean showLogin) {
    this.showLogin = showLogin;
  }

  public boolean isShowLogin() {
    return showLogin;
  }

  public void setShowLogin(boolean showLogin) {
    this.showLogin = showLogin;
  }

  @Override public String toString() {
    return "LoginMaintenanceResponse{" + "showLogin=" + showLogin + '}';
  }
}
