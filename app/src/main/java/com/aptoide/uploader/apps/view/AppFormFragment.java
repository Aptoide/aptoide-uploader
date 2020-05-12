package com.aptoide.uploader.apps.view;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;
import com.aptoide.uploader.R;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.apps.Category;
import com.aptoide.uploader.apps.Metadata;
import com.aptoide.uploader.view.android.FragmentView;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;
import java.util.LinkedHashMap;
import java.util.List;
import static android.util.TypedValue.TYPE_NULL;

public class AppFormFragment extends FragmentView implements AppFormView, OnBackPressedInterface {

  protected View rootView;
  LinkedHashMap<String, String> map;
  private EditText applicationNameEditText;
  private Spinner ageRatingSpinner;
  private Spinner appCategorySpinner;
  private Spinner appLanguageSpinner;
  private EditText appDescriptionEditText;
  private EditText phoneNumberEditText;
  private EditText emailEditText;
  private EditText websiteEditText;
  private Button submitFormButton;
  private String md5;
  private String appName;
  private PublishSubject<Boolean> backPressedEvent;
  private ScrollView parentScrollView;
  private View descriptionScrollView;

  public static AppFormFragment newInstance(String md5, String appName) {
    AppFormFragment appFormFragment = new AppFormFragment();
    Bundle args = new Bundle();
    args.putString("md5", md5);
    args.putString("appName", appName);
    appFormFragment.setArguments(args);
    return appFormFragment;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    md5 = getArguments().getString("md5", "");
    appName = getArguments().getString("appName", "");
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
    parentScrollView = view.findViewById(R.id.submit_app_content);
    descriptionScrollView = view.findViewById(R.id.scroll_description);
    backPressedEvent = PublishSubject.create();

    new AppFormPresenter(this,
        ((UploaderApplication) getContext().getApplicationContext()).getCategoriesManager(),
        AndroidSchedulers.mainThread(),
        ((UploaderApplication) getContext().getApplicationContext()).getUploadManager(), md5,
        new AppFormNavigator(getFragmentManager(), getContext())).present();
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

  @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.submit_app_fragment, container, false);
    rootView.setFocusableInTouchMode(true);
    rootView.requestFocus();
    return rootView;
  }

  public void showAgeRatingSpinner() {
    setSpinnerData(R.id.age_rating_spinner, R.array.age_rating_array);
  }

  @Override public Observable<Boolean> backPressedEvent() {
    return backPressedEvent;
  }

  public void setAppName() {
    applicationNameEditText.setText(appName);
    applicationNameEditText.setFocusable(true);
    applicationNameEditText.setFocusableInTouchMode(true);
    applicationNameEditText.setInputType(TYPE_NULL);
  }

  @Override public void showMandatoryFieldError() {
    Toast.makeText(getActivity(), R.string.missing_fields, Toast.LENGTH_LONG)
        .show();
  }

  @Override public void showGeneralError() {

  }

  @Override public void showForm() {
    showAgeRatingSpinner();
    showLanguageSpinner();
    setAppName();
    allowTextViewScroll(parentScrollView,descriptionScrollView);
  }

  @Override public void showCategories(List<Category> categoriesList) {
    Spinner spinner = rootView.findViewById(R.id.app_category_spinner);
    ArrayAdapter<Category> spinnerArrayAdapter =
        new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, categoriesList);
    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(spinnerArrayAdapter);
    spinner.setEnabled(true);
  }

  @Override public Metadata getMetadata() {
    Metadata metadata = new Metadata();
    metadata.setName(applicationNameEditText.getText()
        .toString());
    metadata.setAgeRating(String.valueOf(ageRatingSpinner.getSelectedItemPosition()));
    Category category = (Category) appCategorySpinner.getSelectedItem();
    String categoryId = String.valueOf(category.getId());
    metadata.setCategory(categoryId);
    metadata.setDescription(appDescriptionEditText.getText()
        .toString());
    metadata.setEmail(emailEditText.getText()
        .toString());
    String languageCode = map.get(appLanguageSpinner.getSelectedItem()
        .toString());
    metadata.setLang(languageCode);
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

  @Override public void hideKeyboard() {
    InputMethodManager manager =
        (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
    View view = getActivity().getCurrentFocus();
    if (view == null) {
      view = new View(getActivity());
    }
    manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }

  @Override public boolean isValidForm() {
    boolean validation = true;
    validation &= ageRatingSpinner.getSelectedItemPosition() != 0;
    validation &= appLanguageSpinner.getSelectedItemPosition() != 0;
    validation &= !applicationNameEditText.getText()
        .toString()
        .equals("");
    validation &= !appDescriptionEditText.getText()
        .toString()
        .equals("");
    return validation;
  }

  public void showLanguageSpinner() {
    String[] languages = this.getResources()
        .getStringArray(R.array.language_array);
    String[] languageCodes = this.getResources()
        .getStringArray(R.array.language_codes);
    map = new LinkedHashMap<>();
    for (int i = 0; i < Math.min(languages.length, languageCodes.length); ++i) {
      map.put(languages[i], languageCodes[i]);
    }
    setSpinnerData(R.id.app_language, R.array.language_array);
  }

  private void setSpinnerData(int viewId, int arrayId) {
    Spinner spinner = rootView.findViewById(viewId);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), arrayId,
        android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
  }

  public void allowTextViewScroll(final ScrollView parentScrollView, View description) {
    for (int i = ((ViewGroup) description).getChildCount() - 1; i >= 0; i--) {
        final View child = ((ViewGroup) description).getChildAt(i);
        if (child instanceof ViewGroup) {
          allowTextViewScroll(parentScrollView, (ViewGroup) child);
        } else {
            child.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event)
                {
                    parentScrollView.requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
        }
    }
  }

  @Override public boolean onBackPressed() {
    backPressedEvent.onNext(true);
    return true;
  }
}