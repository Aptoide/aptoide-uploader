package com.aptoide.uploader.apps.network;

import java.io.IOException;

public class NoConnectivityException extends IOException {

  @Override public String getMessage() {
    return "No connectivity exception";
  }
}