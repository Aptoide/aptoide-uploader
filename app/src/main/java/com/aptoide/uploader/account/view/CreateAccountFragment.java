package com.aptoide.uploader.account.view;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class CreateAccountFragment extends FragmentView implements CreateAccountView {

  private EditText userNameEditText;
  private EditText userPasswordEditText;
  private EditText storeNameEditText;
  private EditText storeUserEditText;
  private EditText storePasswordEditText;
  private AccountErrorMapper accountErrorMapper;
  private RadioButton privateStoreRadioButton;
  private View submitButton;
  private View goToLoginViewButton;
  private View forgotPasswordButton;
  private View progressBarContent;
  private View viewContent;
  private AptoideAccountManager accountManager;
  private CompositeDisposable compositeDisposable;

  public static CreateAccountFragment newInstance() {
    return new CreateAccountFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    accountManager =
        ((UploaderApplication) getContext().getApplicationContext()).getAccountManager();
    accountErrorMapper = new AccountErrorMapper(getContext());
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewContent = view.findViewById(R.id.fragment_create_account_content);
    progressBarContent = view.findViewById(R.id.fragment_create_account_progress_bar_content);
    userNameEditText = view.findViewById(R.id.fragment_create_account_user_name);
    userPasswordEditText = view.findViewById(R.id.fragment_create_account_user_password);
    storeNameEditText = view.findViewById(R.id.fragment_create_account_store_name);
    storeUserEditText = view.findViewById(R.id.fragment_create_account_store_username);
    storePasswordEditText = view.findViewById(R.id.fragment_create_account_store_password);
    privateStoreRadioButton = view.findViewById(R.id.fragment_create_account_private_store);
    submitButton = view.findViewById(R.id.fragment_create_account_submit);
    goToLoginViewButton = view.findViewById(R.id.fragment_create_account_go_to_login);
    forgotPasswordButton = view.findViewById(R.id.fragment_create_account_forgot_password);

    RadioGroup storePrivacyRadioGroup =
        view.findViewById(R.id.fragment_create_account_store_privacy);
    compositeDisposable.add(RxRadioGroup.checkedChanges(storePrivacyRadioGroup)
        .skip(1)
        .map(id -> id == R.id.fragment_create_account_private_store)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(isPrivateStore -> {
          if (isPrivateStore) {
            showPrivateStoreFields();
          } else {
            hidePrivateStoreFields();
          }
        }));

    new CreateAccountPresenter(this, accountManager,
        new CreateAccountNavigator(getFragmentManager(), getContext()), new CompositeDisposable(),
        accountErrorMapper, AndroidSchedulers.mainThread(),
        ((UploaderApplication) getContext().getApplicationContext()).getUploaderAnalytics()).present();
  }

  @Override public void onDestroyView() {
    userNameEditText = null;
    userPasswordEditText = null;
    storeNameEditText = null;
    storeUserEditText = null;
    storePasswordEditText = null;
    privateStoreRadioButton = null;
    submitButton = null;
    goToLoginViewButton = null;
    forgotPasswordButton = null;
    progressBarContent = null;
    viewContent = null;
    accountManager = null;

    if (!compositeDisposable.isDisposed()) {
      compositeDisposable.dispose();
    }
    compositeDisposable = null;
    super.onDestroyView();
  }

  private void hidePrivateStoreFields() {
    storeUserEditText.setVisibility(View.GONE);
    storePasswordEditText.setVisibility(View.GONE);
  }

  private void showPrivateStoreFields() {
    storeUserEditText.setVisibility(View.VISIBLE);
    storePasswordEditText.setVisibility(View.VISIBLE);
  }

  @Nullable @Override
  public android.view.View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    compositeDisposable = new CompositeDisposable();
    return inflater.inflate(R.layout.fragment_create_account, container, false);
  }

  @Override public Observable<ViewModel> getCreateAccountEvent() {
    return RxView.clicks(submitButton)
        .map(__ -> getViewModel());
  }

  @Override public Observable<ViewModel> getOpenLoginView() {
    return RxView.clicks(goToLoginViewButton)
        .map(__ -> getViewModel());
  }

  @Override public Observable<ViewModel> getOpenRecoverPasswordView() {
    return RxView.clicks(forgotPasswordButton)
        .map(__ -> getViewModel());
  }

  @Override public void showLoading() {
    progressBarContent.setVisibility(View.VISIBLE);
    viewContent.setVisibility(View.GONE);
  }

  @Override public void hideLoading() {
    progressBarContent.setVisibility(View.GONE);
    viewContent.setVisibility(View.VISIBLE);
  }

  @Override public void showInvalidFieldError(String messageError) {
    Toast.makeText(getContext(), messageError, Toast.LENGTH_SHORT)
        .show();
  }

  @Override public void showNetworkError() {
    Toast.makeText(getContext(), R.string.error_occurred, Toast.LENGTH_SHORT)
        .show();
  }

  @Override public void showErrorUserAlreadyExists() {
    Toast.makeText(getContext(), R.string.error_user_already_exists, Toast.LENGTH_SHORT)
        .show();
  }

  @Override public void showErrorStoreAlreadyExists() {
    Toast.makeText(getContext(), R.string.error_store_already_exists, Toast.LENGTH_SHORT)
        .show();
  }

  @NonNull private ViewModel getViewModel() {
    if (isPrivateStore()) {
      return new ViewModel(userNameEditText.getText()
          .toString(), userPasswordEditText.getText()
          .toString(), storeNameEditText.getText()
          .toString(), storeUserEditText.getText()
          .toString(), storePasswordEditText.getText()
          .toString());
    }
    return new ViewModel(userNameEditText.getText()
        .toString(), userPasswordEditText.getText()
        .toString(), storeNameEditText.getText()
        .toString());
  }

  private boolean isPrivateStore() {
    return privateStoreRadioButton.isSelected();
  }
}
