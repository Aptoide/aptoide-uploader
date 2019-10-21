package com.aptoide.uploader.account.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.aptoide.uploader.R;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.view.android.FragmentView;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxRadioGroup;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class CreateStoreFragment extends FragmentView implements CreateStoreView {

  private EditText storeName;
  private RadioButton publicStore;
  private RadioButton privateStore;
  private EditText storeUsername;
  private EditText storePassword;
  private Button createStoreButton;
  private CompositeDisposable compositeDisposable;
  private AptoideAccountManager accountManager;
  private AccountErrorMapper accountErrorMapper;

  public CreateStoreFragment() {
  }

  public static CreateStoreFragment newInstance() {
    return new CreateStoreFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    accountManager =
        ((UploaderApplication) getContext().getApplicationContext()).getAccountManager();
    accountErrorMapper = new AccountErrorMapper(getContext());
    super.onCreate(savedInstanceState);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    storeName = view.findViewById(R.id.create_store_name);
    publicStore = view.findViewById(R.id.public_store);
    privateStore = view.findViewById(R.id.private_store);
    storeUsername = view.findViewById(R.id.store_username);
    storePassword = view.findViewById(R.id.store_password);
    createStoreButton = view.findViewById(R.id.fragment_create_store_button);

    RadioGroup storePrivacyRadioGroup = view.findViewById(R.id.create_store_privacy_radiogroup);
    compositeDisposable.add(RxRadioGroup.checkedChanges(storePrivacyRadioGroup)
        .skip(1)
        .map(id -> id == R.id.private_store)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(isPrivateStore -> {
          if (isPrivateStore) {
            showPrivateStoreFields();
          } else {
            hidePrivateStoreFields();
          }
        }));

    new CreateStorePresenter(this, accountManager, new LoginNavigator(getFragmentManager()),
        compositeDisposable,
        accountErrorMapper, AndroidSchedulers.mainThread()).present();
  }

  @Nullable @Override
  public android.view.View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    compositeDisposable = new CompositeDisposable();
    return inflater.inflate(R.layout.fragment_create_store, container, false);
  }

  @Override public void onDestroyView() {
    storeName = null;
    publicStore = null;
    privateStore = null;
    storeUsername = null;
    storePassword = null;
    createStoreButton = null;
    super.onDestroyView();
  }

  private void hidePrivateStoreFields() {
    storeUsername.setVisibility(View.GONE);
    storePassword.setVisibility(View.GONE);
  }

  private void showPrivateStoreFields() {
    storeUsername.setVisibility(View.VISIBLE);
    storePassword.setVisibility(View.VISIBLE);
  }

  private boolean isPrivateStore() {
    return privateStore.isSelected();
  }

  @Override public Observable<CreateStoreViewModel> getStoreInfo() {
    return RxView.clicks(createStoreButton)
        .map(__ -> getViewModel());
  }

  @NonNull private CreateStoreViewModel getViewModel() {
    if (isPrivateStore()) {
      return new CreateStoreViewModel(storeName.getText()
          .toString(), storeUsername.getText()
          .toString(), storePassword.getText()
          .toString());
    }
    return new CreateStoreViewModel(storeName.getText()
        .toString());
  }

  @Override public Observable<CreateStoreViewModel> getOpenLoginView() {
    return null;
  }

  @Override public Observable<CreateStoreViewModel> getOpenRecoverPasswordView() {
    return null;
  }

  @Override public void showLoading() {

  }

  @Override public void hideLoading() {

  }

  @Override public void showInvalidFieldError(String messageError) {

  }

  @Override public void showNetworkError() {
    Toast.makeText(getContext(), getString(R.string.connection_error_body), Toast.LENGTH_LONG)
        .show();
  }

  @Override public void showErrorUserAlreadyExists() {

  }

  @Override public void showErrorStoreAlreadyExists() {
    Toast.makeText(getContext(), getText(R.string.duplicate_store_error),
        Toast.LENGTH_LONG)
        .show();
  }

  @Override public void hideKeyboard() {
    InputMethodManager imm =
        (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
    View view = getActivity().getCurrentFocus();
    if (view == null) {
      view = new View(getActivity());
    }
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }
}
