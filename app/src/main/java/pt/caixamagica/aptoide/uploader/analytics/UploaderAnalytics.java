package pt.caixamagica.aptoide.uploader.analytics;

import android.os.Bundle;
import com.facebook.AppEventsLogger;

/**
 * Created by pedroribeiro on 20/06/17.
 */

public class UploaderAnalytics {

  private static final String SUBMIT_APPS_EVENT_NAME = "Submit_Apps";
  private static final String UPLOAD_COMPLETE_EVENT_NAME = "Upload_Complete";
  private static final String UPLOAD_COMPLETE_SUCCESS = "success";
  private static final String UPLOAD_COMPLETE_FAIL = "fail";
  private final AppEventsLogger facebook;

  public UploaderAnalytics(AppEventsLogger facebook) {
    this.facebook = facebook;
  }

  public void submitApps(int numberOfApps) {
    facebook.logEvent(SUBMIT_APPS_EVENT_NAME, createSubmitAppsBundle(numberOfApps));
  }

  public void uploadCompleteSuccess(Method statusMethod) {
    facebook.logEvent(UPLOAD_COMPLETE_EVENT_NAME,
        createUploadCompleteBundle(UPLOAD_COMPLETE_SUCCESS, statusMethod.getMethodName(), "", ""));
  }

  public void uploadCompleteFail(Method statusMethod, String webCode, String webDescription) {
    facebook.logEvent(UPLOAD_COMPLETE_EVENT_NAME,
        createUploadCompleteBundle(UPLOAD_COMPLETE_FAIL, statusMethod.getMethodName(), "", ""));
  }

  private Bundle createUploadCompleteBundle(String status, String statusMethod, String webCode,
      String webDescription) {
    Bundle bundle = new Bundle();
    bundle.putString("status", status);
    bundle.putString("status_method", statusMethod);
    bundle.putString("web_code", webCode);
    bundle.putString("web_description", webDescription);
    return bundle;
  }

  private Bundle createSubmitAppsBundle(int numberOfApps) {
    Bundle bundle = new Bundle();
    bundle.putInt("number_of_selected_apps", numberOfApps);
    return bundle;
  }

  public enum Method {
    UPLOAD("Upload App to Repo"), CHECK("Check if in Store");

    private String methodName;

    Method(String methodName) {
      this.methodName = methodName;
    }

    public String getMethodName() {
      return methodName;
    }
  }
}
