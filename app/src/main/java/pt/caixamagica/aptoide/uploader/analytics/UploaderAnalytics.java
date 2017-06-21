package pt.caixamagica.aptoide.uploader.analytics;

import android.os.Bundle;
import com.facebook.AppEventsLogger;

/**
 * Created by pedroribeiro on 20/06/17.
 */

public class UploaderAnalytics {

  private static final String SUBMIT_APPS = "Submit_Apps";
  private static final String UPLOAD_COMPLETE = "Upload_Complete";
  private final AppEventsLogger facebook;

  public UploaderAnalytics(AppEventsLogger facebook) {
    this.facebook = facebook;
  }

  public void submitApps(int numberOfApps) {
    facebook.logEvent(SUBMIT_APPS, createSubmitAppsBundle(numberOfApps));
  }

  public void uploadComplete(String status, String statusMethod) {
    facebook.logEvent(UPLOAD_COMPLETE, createUploadCompleteBundle(status, statusMethod));
  }

  private Bundle createUploadCompleteBundle(String status, String statusMethod) {
    Bundle bundle = new Bundle();
    bundle.putString("status", status);
    bundle.putString("status_method", statusMethod);
    return bundle;
  }

  private Bundle createSubmitAppsBundle(int numberOfApps) {
    Bundle bundle = new Bundle();
    bundle.putInt("number_of_selected_apps", numberOfApps);
    return bundle;
  }
}
