package com.aptoide.uploader.analytics;

import android.os.Bundle;
import com.facebook.appevents.AppEventsLogger;
import io.rakam.api.Rakam;
import org.json.JSONException;
import org.json.JSONObject;

public class UploaderAnalytics {

  public static final String STATUS = "status";
  public static final String WEB_CODE = "web_code";
  public static final String WEB_DESCRIPTION = "web_description";
  private static final String SUBMIT_APPS = "Submit_Apps";
  private static final String UPLOAD_COMPLETE = "Upload_Complete";
  private static final String UPLOAD_COMPLETE_TO_RAKAM = "uploader_upload_complete";
  private static final String LOGIN = "Login";
  private static final String SIGNUP = "SignUp";
  private final AppEventsLogger facebook;

  public UploaderAnalytics(AppEventsLogger facebook) {
    this.facebook = facebook;
  }

  public void sendLoginEvent(String method, String status) {
    Bundle bundle = new Bundle();
    bundle.putString("method", method);
    bundle.putString("status", status);
    facebook.logEvent(LOGIN, bundle);
  }

  public void sendSignUpEvent(String status) {
    Bundle bundle = new Bundle();
    bundle.putString("status", status);
    facebook.logEvent(SIGNUP, bundle);
  }

  public void sendSubmitAppsEvent(int numberOfApps) {
    Bundle bundle = new Bundle();
    bundle.putInt("number_of_selected_apps", numberOfApps);
    facebook.logEvent(SUBMIT_APPS, bundle);
  }

  public void sendUploadCompleteEvent(String status, String statusMethod, String webCode,
      String webDescription, String packageUploaded, int vcUploaded) {
    Bundle bundle = new Bundle();
    bundle.putString(STATUS, status);
    bundle.putString("status_method", statusMethod);
    bundle.putString(WEB_CODE, webCode);
    bundle.putString(WEB_DESCRIPTION, webDescription);
    facebook.logEvent(UPLOAD_COMPLETE, bundle);

    JSONObject eventProperties = new JSONObject();
    try {
      eventProperties.put(STATUS, status);
      eventProperties.put(WEB_CODE, webCode);
      eventProperties.put(WEB_DESCRIPTION, webDescription);
      eventProperties.put("package_uploaded", packageUploaded);
      eventProperties.put("version_code_uploaded", vcUploaded);
    } catch (JSONException exception) {
    }
    Rakam.getInstance()
        .logEvent(UPLOAD_COMPLETE_TO_RAKAM, eventProperties);
  }
}
