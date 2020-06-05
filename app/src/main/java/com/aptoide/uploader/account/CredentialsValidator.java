package com.aptoide.uploader.account;

import android.util.Patterns;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by jose_messejana on 05-01-2018.
 */

public class CredentialsValidator {

  /*
  public Completable validate(String email, String password, String storeName) {
    return Completable.defer(() -> {
      if (isEmpty(email) && isEmpty(password) && isEmpty(storeName)) {
        return Completable.error(new AccountValidationException(
            AccountValidationException.EMPTY_EMAIL_PASSWORD_AND_STORE));
      } else if (isEmpty(email) && isEmpty(password)) {
        return Completable.error(
            new AccountValidationException(AccountValidationException.EMPTY_EMAIL_AND_PASSWORD));
      } else if (isEmpty(email) && isEmpty(storeName)) {
        return Completable.error(
            new AccountValidationException(AccountValidationException.EMPTY_EMAIL_AND_STORE));
      } else if (isEmpty(password) && isEmpty(storeName)) {
        return Completable.error(
            new AccountValidationException(AccountValidationException.EMPTY_PASSWORD_AND_STORE));
      } else if (isEmpty(password)) {
        return Completable.error(
            new AccountValidationException(AccountValidationException.EMPTY_PASSWORD));
      } else if (isEmpty(email)) {
        return Completable.error(
            new AccountValidationException(AccountValidationException.EMPTY_EMAIL));
      } else if (isEmpty(storeName)) {
        return Completable.error(
            new AccountValidationException(AccountValidationException.EMPTY_STORE));
      } else if (password.length() < 8 || !validatePassword(password)) {
        return Completable.error(
            new AccountValidationException(AccountValidationException.INVALID_PASSWORD));
      }
      return Completable.complete();
    });
  }
  */

  private boolean isEmpty(CharSequence str) {
    return str == null || str.length() == 0;
  }

  private boolean validatePassword(String password) {
    boolean hasLetter = false;
    boolean hasNumber = false;

    for (char character : password.toCharArray()) {
      if (!hasLetter && Character.isLetter(character)) {
        if (hasNumber) return true;
        hasLetter = true;
      } else if (!hasNumber && Character.isDigit(character)) {
        if (hasLetter) return true;
        hasNumber = true;
      }
    }
    if (password.contains("!")
        || password.contains("@")
        || password.contains("#")
        || password.contains("$")
        || password.contains("#")
        || password.contains("*")) {
      hasNumber = true;
    }

    return hasNumber && hasLetter;
  }

  public Single<Boolean> isEmailValid(String email) {
    return Single.just(checkIsEmailValid(email));
  }

  private boolean checkIsEmailValid(String email) {
    if (!isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email)
        .matches()) {
      return true;
    }
    return false;
  }

  /**
   * Returns true if email and password are not empty. If validate password content is enable
   *
   * @param credentials
   */
  public Completable validate(AptoideCredentials credentials) {
    return Completable.defer(() -> {
      int result = validateFields(credentials);
      if (result != -1) return Completable.error(new AccountValidationException(result));
      return Completable.complete();
    });
  }

  @Nullable @VisibleForTesting protected int validateFields(AptoideCredentials credentials) {
    if (isEmpty(credentials.getEmail()) && isEmpty(credentials.getCode())) {
      return AccountValidationException.EMPTY_EMAIL_AND_CODE;
    } else if (isEmpty(credentials.getCode())) {
      return AccountValidationException.EMPTY_CODE;
    } else if (isEmpty(credentials.getEmail())) {
      return AccountValidationException.EMPTY_EMAIL;
    }
    return -1;
  }
}
