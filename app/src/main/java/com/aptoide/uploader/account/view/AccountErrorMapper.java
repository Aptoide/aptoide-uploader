package com.aptoide.uploader.account.view;

import android.content.Context;
import com.aptoide.uploader.R;
import com.aptoide.uploader.account.AccountValidationException;

/**
 * Created by jose_messejana on 05-01-2018.
 */

public class AccountErrorMapper {

  private final Context context;

  public AccountErrorMapper(Context context) {
    this.context = context;
  }

  public String map(Throwable throwable) {
    String message = "Unknown Error";
    switch (((AccountValidationException) throwable).getCode()) {
      case AccountValidationException.EMPTY_EMAIL_PASSWORD_AND_STORE:
        message = context.getString(R.string.no_email_pass_and_store_error_message);
        break;
      case AccountValidationException.EMPTY_EMAIL_AND_PASSWORD:
        message = context.getString(R.string.no_email_and_pass_error_message);
        break;
      case AccountValidationException.EMPTY_EMAIL_AND_STORE:
        message = context.getString(R.string.no_email_and_store_error_message);
        break;
      case AccountValidationException.EMPTY_PASSWORD_AND_STORE:
        message = context.getString(R.string.no_pass_and_store_error_message);
        break;
      case AccountValidationException.EMPTY_EMAIL:
        message = context.getString(R.string.no_email_error_message);
        break;
      /*case AccountValidationException.EMPTY_PASSWORD:
        message = context.getString(R.string.no_pass_error_message);
        break;
      case AccountValidationException.EMPTY_STORE:
        message = context.getString(R.string.no_store_error_message);
        break;*/
      case AccountValidationException.INVALID_PASSWORD:
        message = context.getString(R.string.invalid_pass_error_message);
        break;
    }
    return message;
  }
}
