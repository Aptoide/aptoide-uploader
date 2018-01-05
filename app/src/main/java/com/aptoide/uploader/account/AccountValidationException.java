package com.aptoide.uploader.account;

/**
 * Created by jose_messejana on 05-01-2018.
 */

public class AccountValidationException extends Exception {
  public static final int EMPTY_EMAIL = 1;
  public static final int EMPTY_PASSWORD = 2;
  public static final int EMPTY_STORE = 3;
  public static final int INVALID_PASSWORD = 4;
  public static final int EMPTY_EMAIL_AND_PASSWORD = 5;
  public static final int EMPTY_EMAIL_AND_STORE = 6;
  public static final int EMPTY_PASSWORD_AND_STORE = 7;
  public static final int EMPTY_EMAIL_PASSWORD_AND_STORE = 8;

  private final int code;

  public AccountValidationException(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
