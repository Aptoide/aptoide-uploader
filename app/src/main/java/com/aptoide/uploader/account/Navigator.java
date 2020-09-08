package com.aptoide.uploader.account;

public abstract class Navigator {
  public abstract void navigateToAutoLoginFragment(String name, String path);

  public abstract void navigateToAutoLoginFragment(String name);

  public abstract void navigateToLoginFragment();
}
