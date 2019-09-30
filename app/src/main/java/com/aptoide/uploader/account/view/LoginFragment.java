package com.aptoide.uploader.account.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.aptoide.uploader.R;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.view.android.FragmentView;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class LoginFragment extends FragmentView implements LoginView {

  private EditText passwordEditText;
  private EditText usernameEditText;
  private View progressContainer;
  private View fragmentContainer;
  private TextView loadingTextView;
  private AptoideAccountManager accountManager;
  private View loginButton;
  private View signUpButton;

  public static LoginFragment newInstance() {
    return new LoginFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    accountManager =
        ((UploaderApplication) getContext().getApplicationContext()).getAccountManager();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    passwordEditText = view.findViewById(R.id.fragment_login_password_edit_text);
    usernameEditText = view.findViewById(R.id.fragment_login_username_edit_text);
    loginButton = view.findViewById(R.id.fragment_login_button);
    signUpButton = view.findViewById(R.id.fragment_login_sign_up);
    progressContainer = view.findViewById(R.id.fragment_login_progress_container);
    loadingTextView = view.findViewById(R.id.fragment_login_loading_text_view);
    fragmentContainer = view.findViewById(R.id.fragment_login_content);

    new LoginPresenter(this, accountManager, new LoginNavigator(getContext(), getFragmentManager()),
        new CompositeDisposable(), AndroidSchedulers.mainThread(),
        ((UploaderApplication) getContext().getApplicationContext()).getUploaderAnalytics()).present();
  }

  @Override public void onDestroyView() {
    passwordEditText = null;
    usernameEditText = null;
    loginButton = null;
    signUpButton = null;
    fragmentContainer = null;
    progressContainer = null;
    loadingTextView = null;
    super.onDestroyView();
  }

  @Nullable @Override
  public android.view.View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_login, container, false);
  }

  @Override public Observable<CredentialsViewModel> getLoginEvent() {
    return RxView.clicks(loginButton)
        .map(__ -> getViewModel());
  }

  @Override public Observable<CredentialsViewModel> getOpenCreateAccountView() {
    return RxView.clicks(signUpButton)
        .map(__ -> getViewModel());
  }

  @Override public void showLoading(String username) {
    loadingTextView.setText(getString(R.string.logging_as).concat(" " + username));
    fragmentContainer.setVisibility(View.GONE);
    progressContainer.setVisibility(View.VISIBLE);
  }

  @Override public void hideLoading() {
    progressContainer.setVisibility(View.GONE);
    loadingTextView.setVisibility(View.GONE);
    fragmentContainer.setVisibility(View.VISIBLE);
  }

  @Override public void showCrendentialsError() {
    Toast.makeText(getContext(), R.string.loginFail, Toast.LENGTH_SHORT)
        .show();
  }

  @Override public void showNetworkError() {
    Toast.makeText(getContext(), R.string.error_occurred, Toast.LENGTH_SHORT)
        .show();
  }

  @Override public void showNoConnectivityError() {
    Toast.makeText(getContext(), R.string.no_connectivity_error, Toast.LENGTH_LONG)
        .show();
  }

  @NonNull private CredentialsViewModel getViewModel() {
    return new CredentialsViewModel(usernameEditText.getText()
        .toString(), passwordEditText.getText()
        .toString());
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
