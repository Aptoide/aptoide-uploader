package com.aptoide.uploader.apps.view;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import com.aptoide.uploader.R;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.apps.Metadata;
import com.aptoide.uploader.view.android.FragmentView;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.util.List;

public class AppFormFragment extends FragmentView implements AppFormView {

  protected View rootView;
  boolean mBound = false;
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
  private String languageCode;
  private String language;
  private Button submitFormButton;
  private String md5;

  public static AppFormFragment newInstance(String md5) {
    AppFormFragment appFormFragment = new AppFormFragment();
    Bundle args = new Bundle();
    args.putString("md5", md5);
    appFormFragment.setArguments(args);
    return appFormFragment;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    md5 = getArguments().getString("md5");
    rootView = inflater.inflate(R.layout.submit_app_fragment, container, false);

    rootView.setFocusableInTouchMode(true);
    rootView.requestFocus();

    new AppFormPresenter(this,
        ((UploaderApplication) getContext().getApplicationContext()).getCategoriesManager(),
        AndroidSchedulers.mainThread(),
        ((UploaderApplication) getContext().getApplicationContext()).getUploadManager(),
        ((UploaderApplication) getContext().getApplicationContext()).getUploadPersistence(),
        md5).present();

    return rootView;
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    applicationNameEditText = view.findViewById(R.id.appName);
    ageRatingSpinner = view.findViewById(R.id.age_rating_spinner);
    appCategorySpinner = view.findViewById(R.id.app_category_spinner);
    appCategorySpinner.setEnabled(false);
    appLanguageSpinner = view.findViewById(R.id.app_language);
    appDescriptionEditText = view.findViewById(R.id.app_description);
    phoneNumberEditText = view.findViewById(R.id.phone_number);
    emailEditText = view.findViewById(R.id.email);
    websiteEditText = view.findViewById(R.id.website);
    submitFormButton = view.findViewById(R.id.submit_app_button);
  }

  public void showAgeRatingSpinner() {
    setSpinnerData(R.id.age_rating_spinner, R.array.age_rating_array);
  }

  public void showLanguageSpinner() {
    setSpinnerData(R.id.app_language, R.array.language_array);
  }

  private void setSpinnerData(int viewId, int arrayId) {
    Spinner spinner = rootView.findViewById(viewId);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), arrayId,
        android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
  }

  @Override public void setAppName() {

  }

  @Override public void showMandatoryFieldError() {

  }

  @Override public void showGeneralError() {

  }

  @Override public void showForm() {
    showAgeRatingSpinner();
    showLanguageSpinner();
  }

  @Override public void showCategories(List<String> categoriesList) {
    Spinner spinner = rootView.findViewById(R.id.app_category_spinner);
    ArrayAdapter<String> spinnerArrayAdapter =
        new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, categoriesList);
    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(spinnerArrayAdapter);
    spinner.setEnabled(true);
  }

  @Override public Metadata getMetadata() {
    Metadata metadata = new Metadata();
    metadata.setName(applicationNameEditText.getText()
        .toString());
    metadata.setAgeRating((int) ageRatingSpinner.getSelectedItem());
    metadata.setCategory(appCategorySpinner.getSelectedItem()
        .toString());
    metadata.setDescription(appDescriptionEditText.getText()
        .toString());
    metadata.setEmail(emailEditText.getText()
        .toString());
    metadata.setLang(appLanguageSpinner.getSelectedItem()
        .toString());
    metadata.setPhoneNumber(phoneNumberEditText.getText()
        .toString());
    metadata.setWebsite(websiteEditText.getText()
        .toString());
    return metadata;
  }

  @Override public Observable<Metadata> submitAppEvent() {
    return RxView.clicks(submitFormButton)
        .map(__ -> getMetadata());
  }

  @Override public void onStart() {
    super.onStart();
  }

  @Override public void onStop() {
    super.onStop();
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
  }
}