package com.aptoide.uploader.apps.view;

import androidx.fragment.app.FragmentManager;

public class AppDetailsNavigator {

  private final FragmentManager fragmentManager;

  public AppDetailsNavigator(FragmentManager fragmentManager) {
    this.fragmentManager = fragmentManager;
  }

  public void navigateBack() {
    fragmentManager.popBackStack();
  }
}
