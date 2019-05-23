package com.aptoide.uploader.apps;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.aptoide.uploader.R;
import com.aptoide.uploader.view.android.FragmentView;
import java.util.List;

public class SubmitAppFragment extends FragmentView {

  protected View rootView;
  boolean mBound = false;
  private List<String> spinnerArray;
  /**
   * Defines callbacks for service binding, passed to bindService()
   */
  private ServiceConnection mConnection = new ServiceConnection() {

    @Override public void onServiceConnected(ComponentName className, IBinder service) {
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      mBound = true;
    }

    @Override public void onServiceDisconnected(ComponentName arg0) {
      mBound = false;
    }
  };

  // Campos editáveis:
  private EditText applicationNameEditText;

  private Spinner ageRatingSpinner;

  private Spinner appCategorySpinner;
  private Spinner appLanguageSpinner;
  private EditText appDescriptionEditText;

  private EditText phoneNumberEditText;

  private EditText emailEditText;

  private EditText websiteEditText;

  private boolean dataLoaded;

  private String proposedTitle;
  private String proposedDescription;
  private String proposedCategory;
  private boolean fromAppView = false;
  private String languageCode;
  private String language;

  public static SubmitAppFragment newInstance() {
    return new SubmitAppFragment();
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    rootView = inflater.inflate(R.layout.submit_app_fragment, container, false);

    rootView.setFocusableInTouchMode(true);
    rootView.requestFocus();

    if (savedInstanceState != null) {
      ((TextView) rootView.findViewById(R.id.progressBarText)).setText(
          savedInstanceState.getString("loading"));
      dataLoaded = savedInstanceState.getBoolean("dataLoaded");
    }

    return rootView;
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    applicationNameEditText = (EditText) view.findViewById(R.id.appName);
    ageRatingSpinner = (Spinner) view.findViewById(R.id.age_rating_spinner);
    appCategorySpinner = (Spinner) view.findViewById(R.id.app_category_spinner);
    appLanguageSpinner = (Spinner) view.findViewById(R.id.app_language);
    appDescriptionEditText = (EditText) view.findViewById(R.id.app_description);
    phoneNumberEditText = (EditText) view.findViewById(R.id.phone_number);
    emailEditText = (EditText) view.findViewById(R.id.email);
    websiteEditText = (EditText) view.findViewById(R.id.website);

    if (proposedTitle != null && !proposedTitle.isEmpty()) {
      applicationNameEditText.setText(proposedTitle);
    }
    if (proposedDescription != null && !proposedDescription.isEmpty()) {
      appDescriptionEditText.setText(proposedDescription);
    }
  }

  @Override public void onStart() {
    super.onStart();
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    outState.putString("loading", ((TextView) rootView.findViewById(R.id.progressBarText)).getText()
        .toString());
    outState.putBoolean("dataLoaded", dataLoaded);
    super.onSaveInstanceState(outState);
  }

  @Override public void onStop() {
    super.onStop();
    //secondarySpiceManager.shouldStop();
    if (mBound) {
      getActivity().unbindService(mConnection);
      mBound = false;
    }
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
  }

  @Override public void onDestroy() {
    super.onDestroy();
  }
}