/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.liquid;

/**
 * Classe com os eventos do Liquid.
 */
public class Event {

  // Fragments
  public static final String LOGIN_PAGE = "Login Fragment";
  public static final String APPS_LIST_VIEW = "Apps List View Fragment";
  public static final String SUBMIT_APP_PAGE = "Submit App Fragment";

  // Actions
  public static final String LOGGED_IN = "User Logged In";
  public static final String SUBMIT_APP = "Submitting App";

  // Errors
  public static final String DUPLICATED_APP = "User Tried to submit duplicated app";

  // App Info Changes
  public static class AppInfoChanges {

    public static final String NAME = "Edit App name";
    public static final String AGE_RATING = "Edit Age Rating";
    public static final String CATEGORY = "Edit Category";
    public static final String APP_DESCRIPTION = "Edit App Description";
  }
}
