package com.aptoide.uploader.account;

public interface AutoLoginPersistence {

  boolean isForcedLogout();

  void setForcedLogout(boolean forcedLogout);
}
