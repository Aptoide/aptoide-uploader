/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.vending.licensing.ValidationException;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import pt.caixamagica.aptoide.uploader.liquid.Event;
import pt.caixamagica.aptoide.uploader.retrofit.RetrofitSpiceServiceUploader;
import pt.caixamagica.aptoide.uploader.retrofit.request.GetApkInfoRequest;
import pt.caixamagica.aptoide.uploader.retrofit.request.ListCategoriesRequest;
import pt.caixamagica.aptoide.uploader.uploadService.MyBinder;
import pt.caixamagica.aptoide.uploader.uploadService.UploadService;
import pt.caixamagica.aptoide.uploader.util.LanguageCodesHelper;
import pt.caixamagica.aptoide.uploader.webservices.json.CategoriesJson;
import pt.caixamagica.aptoide.uploader.webservices.json.GetApkInfoJson;
import pt.caixamagica.aptoide.uploader.webservices.json.UserCredentialsJson;

/**
 * Created by neuro on 10-02-2015.
 */
public class SubmitAppFragment extends Fragment {

  private static List<String> spinnerArray;

  private static List<CategoriesJson.Category> categories = new LinkedList<>();

  protected SpiceManager spiceManager = new SpiceManager(RetrofitSpiceServiceUploader.class);

  protected View rootView;

  UploadService mService;

  boolean mBound = false;

  /**
   * Defines callbacks for service binding, passed to bindService()
   */
  private ServiceConnection mConnection = new ServiceConnection() {

    @Override public void onServiceConnected(ComponentName className, IBinder service) {
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      MyBinder binder = (MyBinder) service;
      mService = binder.getService();
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

  private UserCredentialsJson userCredentialsJson;

  private ArrayList<SelectablePackageInfo> selectablePackageInfos;
  private boolean dataLoaded;

  private String proposedTitle;
  private String proposedDescription;
  private boolean fromAppView = false;
  private String languageCode;
  private String language;

  public static SubmitAppFragment newInstance() {
    SubmitAppFragment submitAppFragment = new SubmitAppFragment();
    return submitAppFragment;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (!getArguments().isEmpty()) {
      proposedTitle = getArguments().getString("title");
      proposedDescription = getArguments().getString("description");
    }
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

    prepareButtons();
    loadBundleData();
    prepareSpinners();

    if (savedInstanceState == null || !dataLoaded) prepareInfo();

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

    if (applicationNameEditText.getText().toString().equals("")
        && selectablePackageInfos != null && proposedTitle == null) {
      applicationNameEditText.setText(selectablePackageInfos.get(0).getLabel());
    }

    if (proposedTitle != null && !proposedTitle.isEmpty()) {
      applicationNameEditText.setText(proposedTitle);
    }
    if (proposedDescription != null && !proposedDescription.isEmpty()) {
      appDescriptionEditText.setText(proposedDescription);
    }
    if (languageCode != null && !languageCode.isEmpty()) {
      language = LanguageCodesHelper.translateToLanguageName(languageCode);
      int languagePosition = findLanguageInArray(language);
      if (languagePosition != -1) {
        appLanguageSpinner.setSelection(languagePosition);
      }
    }
  }

  @Override public void onStart() {
    super.onStart();
    spiceManager.start(getActivity());

    Intent intent = new Intent(getActivity(), UploadService.class);

    getActivity().startService(intent);
    intent = new Intent(getActivity(), UploadService.class);
    getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    outState.putString("loading",
        ((TextView) rootView.findViewById(R.id.progressBarText)).getText().toString());
    outState.putBoolean("dataLoaded", dataLoaded);
    super.onSaveInstanceState(outState);
  }

  @Override public void onStop() {
    super.onStop();
    spiceManager.shouldStop();
    if (mBound) {
      getActivity().unbindService(mConnection);
      mBound = false;
    }
  }

  private int findLanguageInArray(String language) {
    int i = -1;
    int index = 0;
    String[] languages = getResources().getStringArray(R.array.language_array);
    for (String s : languages) {
      if (s.equals(language)) {
        i = index;
        break;
      }
      index++;
    }
    return i;
  }

  private void loadingCosmetics(boolean state) {
    loadingCosmetics(state, "");
  }

  private void loadingCosmetics(boolean state, String text) {
    View content = rootView.findViewById(R.id.submit_app_content);
    View submitButton = rootView.findViewById(R.id.submit_app_button);
    View loading = rootView.findViewById(R.id.progressBar);
    TextView textView = (TextView) rootView.findViewById(R.id.progressBarText);

    textView.setText(text);

    content.setVisibility(state ? View.GONE : View.VISIBLE);
    submitButton.setVisibility(state ? View.GONE : View.VISIBLE);
    loading.setVisibility(state ? View.VISIBLE : View.GONE);
  }

  private void showErrorMessage(boolean state) {
    View errorMessageLayout = rootView.findViewById(R.id.errorLayout);
    View loading = rootView.findViewById(R.id.progressBar);

    loading.setVisibility(state ? View.GONE : View.VISIBLE);
    errorMessageLayout.setVisibility(state ? View.VISIBLE : View.GONE);
  }

  private void loadBundleData() {
    selectablePackageInfos = getArguments().getParcelableArrayList("selectableAppNames");
    userCredentialsJson =
        (UserCredentialsJson) getArguments().getSerializable("userCredentialsJson");
    proposedTitle = getArguments().getString("title");
    proposedDescription = getArguments().getString("description");
    languageCode = getArguments().getString("languageCode");
    fromAppView = getArguments().getBoolean("fromAppview");
  }

  private void submitApp() throws ValidationException {

    if (validadeFields()) {

      setEditedFields();
      uploadApp();

      Toast.makeText(getActivity(), R.string.sending_background, Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(getActivity(), R.string.missing_fields, Toast.LENGTH_LONG).show();
    }
  }

  private void setEditedFields() {
    SelectablePackageInfo selectablePackageInfo = selectablePackageInfos.get(0);

    selectablePackageInfo.setName(getAppName());
    selectablePackageInfo.setCategory(getCategory());
    selectablePackageInfo.setAgeRating(getAgeRating());
    selectablePackageInfo.setDescription(getDescription());
    String languageCode = LanguageCodesHelper.translateToLanguageCode(getLanguage());
    selectablePackageInfo.setLang(languageCode);
  }

  private void nextApp() {
    if (selectablePackageInfos.size() > 1) {
      selectablePackageInfos.remove(0);
      prepareInfo();
    } else {
      getActivity().finish();
    }
  }

  private void uploadApp() throws ValidationException {

    if (proposedTitle != null) { //In case this fragment is filled with content from getProposed
      mService.inputTitle = applicationNameEditText.getText().toString();
    } else {
      mService.inputTitle = null;
    }
    mService.prepareUploadAndSend(userCredentialsJson, selectablePackageInfos.get(0));

    nextApp();
  }

  /**
   * Valida campos obrigatórios.
   */
  private boolean validadeFields() {

    boolean validation = true;

    validation &= !applicationNameEditText.getText().toString().equals("");
    validation &= appCategorySpinner.getSelectedItemPosition() != 0;
    validation &= !appDescriptionEditText.getText().toString().equals("");
    validation &= appLanguageSpinner.getSelectedItemPosition() != 0;

    return validation;
  }

  private void prepareInfo() {
    dataLoaded = false;

    if (selectablePackageInfos != null && selectablePackageInfos.size() > 0) {

      if (!fromAppView) {
        loadingCosmetics(true, "Checking repository");
        getAppInfo(selectablePackageInfos.get(0));
      }
      getActivity().setTitle(getString(R.string.submit_app));
    }
  }

  private void prepareButtons() {
    Button button = (Button) rootView.findViewById(R.id.submit_app_button);
    button.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        try {
          submitApp();
        } catch (ValidationException e) {
          e.printStackTrace();
        }
      }
    });

    button = (Button) rootView.findViewById(R.id.okErrorButton);
    button.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        nextApp();
      }
    });
  }

  private void prepareSpinners() {
    prepareSpinner(R.id.age_rating_spinner, R.array.age_rating_array);
    prepareSpinner(R.id.app_language, R.array.language_array);
    retrieveCategorySpinnerArray();
  }

  private void prepareSpinner(int viewId, int arrayId) {
    Spinner spinner = (Spinner) rootView.findViewById(viewId);
    // Create an ArrayAdapter using the string array and a default spinner layout
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), arrayId,
        android.R.layout.simple_spinner_item);
    // Specify the layout to use when the list of choices appears
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
    spinner.setAdapter(adapter);
  }

  private void prepareCategorySpinner() {
    Spinner spinner = (Spinner) rootView.findViewById(R.id.app_category_spinner);
    ArrayAdapter<String> spinnerArrayAdapter =
        new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, spinnerArray);
    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(spinnerArrayAdapter);
  }

  private void retrieveCategorySpinnerArray() {

    if (spinnerArray == null) {
      ListCategoriesRequest listCategoriesRequest = new ListCategoriesRequest();
      listCategoriesRequest.setMode("json");
      listCategoriesRequest.setLanguage(Locale.getDefault()
          .getLanguage());

      spiceManager.execute(listCategoriesRequest, new RequestListener<CategoriesJson>() {
        @Override public void onRequestFailure(SpiceException spiceException) {
        }

        @Override public void onRequestSuccess(CategoriesJson categoriesJson) {

          categoriesJson.categories.standard.remove(new CategoriesJson.Category(1));
          categoriesJson.categories.standard.remove(new CategoriesJson.Category(2));

          categories.addAll(categoriesJson.categories.standard);
          categories.addAll(categoriesJson.categories.custom);
          Collections.sort(categories, new Comparator<CategoriesJson.Category>() {
            @Override public int compare(CategoriesJson.Category lhs, CategoriesJson.Category rhs) {
              return lhs.getName().compareTo(rhs.getName());
            }
          });

          spinnerArray = new LinkedList<String>();

          for (CategoriesJson.Category category : categories) {
            spinnerArray.add(category.getName());
          }
          spinnerArray.add(0, "App Category");

          prepareCategorySpinner();
        }
      });
    } else {
      prepareCategorySpinner();
    }
  }

  /**
   * Retorna o id da categoria cujo nome é <code>name</code>, -1 para default (não encontrado).
   *
   * @param name nome da categoria
   *
   * @return o id da categoria cujo nome é <code>name</code>.
   */
  public int getCategoryId(String name) {

    // Default Value
    if (name.equals("App Category (Optional)")) return -1;

    for (CategoriesJson.Category category : categories) {
      if (category.getName().equals(name)) return category.getId().intValue();
    }

    return -1;
  }

  /**
   * Retorna o indice no spinner da categoria com o <code>id</code> fornecido. 0 caso seja
   * desconhecido.
   *
   * @param id id da categoria
   *
   * @return o indice no spinner da categoria com o <code>id</code> fornecido.
   */
  public int getCategorySpinnerIndex(Number id) {

    int i = 0;
    for (CategoriesJson.Category category : categories) {
      i++;
      if (category.getId().equals(id)) {
        return i;
      }
    }
    return 0;
  }

  /**
   * Retorna o índice no spinner do age rating fornecido.
   *
   * @param number age rating fornecido.
   *
   * @return o índice no spinner do age rating fornecido.
   */
  public int getAgeRatingSpinnerIndex(Number number) {
    switch (number.intValue()) {
      case 0:
        return 0;
      case 10:
        return 1;
      case 16:
        return 2;
      case 18:
        return 3;
      default:
        return 0;
    }
  }

  private void getAppInfo(final SelectablePackageInfo selectablePackageInfo) {
    GetApkInfoRequest getApkInfoRequest = new GetApkInfoRequest();

    getApkInfoRequest.setApkid(selectablePackageInfo.packageName);

    spiceManager.execute(getApkInfoRequest, new RequestListener<GetApkInfoJson>() {

      @Override public void onRequestFailure(SpiceException spiceException) {
        loadingCosmetics(false);
      }

      @Override public void onRequestSuccess(GetApkInfoJson getApkInfoJson) {

        setAppInfo(getApkInfoJson.getMeta());

        loadingCosmetics(false);
        dataLoaded = true;
      }
    });
  }

  private void setAppInfo(GetApkInfoJson.Meta meta) {
    if (meta != null) {
      setAppName(selectablePackageInfos.get(0).getLabel());
      setAppDescription(meta.getDescription());
      setCategorySpinner(meta.categories.standard.get(1).id);
      setAgeRatingSpinner(meta.min_age);
    } else {
      setAppDescription(null);
      setCategorySpinner(0);
      setCategorySpinner(0);
    }
  }

  private String getAppName() {
    return ((EditText) rootView.findViewById(R.id.appName)).getText().toString();
  }

  private void setAppName(String appName) {
    EditText editText = (EditText) rootView.findViewById(R.id.appName);
    editText.setText(appName);

    addLiquidListener(editText, Event.AppInfoChanges.NAME);
  }

  private int getAgeRating() {
    return ((Spinner) rootView.findViewById(R.id.age_rating_spinner)).getSelectedItemPosition() + 1;
  }

  private int getCategory() {
    String selectedItem =
        (String) ((Spinner) rootView.findViewById(R.id.app_category_spinner)).getSelectedItem();
    return idFromCategoryName(selectedItem);
  }

  private String getDescription() {
    return ((EditText) rootView.findViewById(R.id.app_description)).getText().toString();
  }

  private String getLanguage() {
    String selectedItem =
        (String) ((Spinner) rootView.findViewById(R.id.app_language)).getSelectedItem();
    return selectedItem;
  }

  private int idFromCategoryName(String name) {
    for (CategoriesJson.Category category : categories) {
      if (category.getName().equals(name)) return (int) category.getId();
    }
    return 0;
  }

  private void addLiquidListener(View view, final String liquidEvent) {
    view.setOnTouchListener(new View.OnTouchListener() {

      boolean triggered = false;

      @Override public boolean onTouch(View v, MotionEvent event) {
        if (!triggered) {
          triggered = true;
        }
        return false;
      }
    });
  }

  private void setAgeRatingSpinner(Number min_age) {
    Spinner spinner = (Spinner) rootView.findViewById(R.id.age_rating_spinner);
    spinner.setSelection(getAgeRatingSpinnerIndex(min_age));

    addLiquidListener(spinner, Event.AppInfoChanges.AGE_RATING);
  }

  private void setCategorySpinner(Number id) {
    Spinner spinner = (Spinner) rootView.findViewById(R.id.app_category_spinner);
    spinner.setSelection(getCategorySpinnerIndex(id));

    addLiquidListener(spinner, Event.AppInfoChanges.CATEGORY);
  }

  private void setAppDescription(String description) {

    EditText editText = (EditText) rootView.findViewById(R.id.app_description);
    editText.setText(description);

    addLiquidListener(editText, Event.AppInfoChanges.APP_DESCRIPTION);

    descriptionLogicMartelado();
  }

  private void descriptionLogicMartelado() {
    EditText dwEdit = (EditText) rootView.findViewById(R.id.app_description);
    dwEdit.setOnTouchListener(new View.OnTouchListener() {

      public boolean onTouch(View view, MotionEvent event) {
        if (view.getId() == R.id.app_description) {
          view.getParent().getParent().requestDisallowInterceptTouchEvent(true);
          switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
              view.getParent().requestDisallowInterceptTouchEvent(false);
              break;
          }
        }
        return false;
      }
    });
  }
}
