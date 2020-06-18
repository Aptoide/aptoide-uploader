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

  private boolean isEmpty(CharSequence str) {
    return str == null || str.length() == 0;
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
