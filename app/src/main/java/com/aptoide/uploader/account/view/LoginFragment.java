package com.aptoide.uploader.account.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.aptoide.uploader.R;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.view.android.FragmentView;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;
import java.util.Arrays;

public class LoginFragment extends FragmentView implements LoginView {

  private static final int RC_SIGN_IN = 9001;
  PublishSubject<GoogleSignInAccount> googleLoginSubject = PublishSubject.create();
  PublishSubject<LoginResult> facebookLoginSubject = PublishSubject.create();
  private View progressContainer;
  private View fragmentContainer;
  private TextView loadingTextView;
  private AptoideAccountManager accountManager;
  private LoginButton facebookLoginButton;
  private Button googleLoginButton;
  private GoogleSignInClient mGoogleSignInClient;
  private GoogleSignInOptions gso;
  private CallbackManager callbackManager;
  private TextView title;
  private TextView message_first;
  private TextView message_second;
  private TextView blog;
  private ImageView blogNextButton;

  public static LoginFragment newInstance() {
    return new LoginFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    gso = ((UploaderApplication) getContext().getApplicationContext()).getGSO();
    accountManager =
        ((UploaderApplication) getContext().getApplicationContext()).getAccountManager();
    callbackManager =
        ((UploaderApplication) getContext().getApplicationContext()).getCallbackManager();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    progressContainer = view.findViewById(R.id.fragment_login_progress_container);
    loadingTextView = view.findViewById(R.id.fragment_login_loading_text_view);
    fragmentContainer = view.findViewById(R.id.fragment_login_content);

    title = view.findViewById(R.id.login_title);
    message_first = view.findViewById(R.id.login_message1);
    message_second = view.findViewById(R.id.login_message2);
    blog = view.findViewById(R.id.login_blog);
    blogNextButton = view.findViewById(R.id.login_blognext);

    googleLoginButton = view.findViewById(R.id.google_sign_in_button);
    //googleLoginButton.setSize(SignInButton.SIZE_WIDE);
    facebookLoginButton = view.findViewById(R.id.facebook_login_button);
    facebookLoginButton.setPermissions(Arrays.asList("email", "public_profile"));
    facebookLoginButton.setFragment(this);
    setFacebookCustomListener();

    fragmentContainer.setVisibility(View.VISIBLE);
    title.setText(getString(R.string.login_disclaimer_title));
    message_first.setText(getString(R.string.login_disclaimer_body_1));
    message_second.setText(getString(R.string.login_disclaimer_body_2));
    setupBlogTextView();

    new LoginPresenter(this, accountManager,
        new LoginNavigator(getFragmentManager(), getContext().getApplicationContext()),
        new CompositeDisposable(), AndroidSchedulers.mainThread(),
        ((UploaderApplication) getContext().getApplicationContext()).getUploaderAnalytics(),
        ((UploaderApplication) getContext().getApplicationContext()).getAutoLoginManager()).present();
  }

  @Override public void onResume() {
    super.onResume();
  }

  @Override public void onPause() {
    super.onPause();
  }

  @Override public void onDestroyView() {
    googleLoginButton = null;
    fragmentContainer = null;
    progressContainer = null;
    loadingTextView = null;
    facebookLoginButton = null;
    title = null;
    message_first = null;
    message_second = null;
    blog = null;
    blogNextButton = null;
    super.onDestroyView();
  }

  private void setupBlogTextView() {
    SpannableString content = new SpannableString(getString(R.string.login_disclaimer_blog_button));
    content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
    blog.setText(content);
  }

  @Override public Observable<Object> getGoogleLoginEvent() {
    mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
    return RxView.clicks(googleLoginButton);
  }

  @Override public Observable<GoogleSignInAccount> googleLoginSuccessEvent() {
    return googleLoginSubject;
  }

  @Override public Observable<LoginResult> facebookLoginSucessEvent() {
    return facebookLoginSubject;
  }

  @Override public void showLoading(String username) {
    loadingTextView.setText(getString(R.string.logging_as).concat(" " + username));
    fragmentContainer.setVisibility(View.GONE);
    progressContainer.setVisibility(View.VISIBLE);
  }

  @Override public void showLoadingWithoutUserName() {
    loadingTextView.setText(getString(R.string.logging_in));
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

  @Override public void hideKeyboard() {
    InputMethodManager imm =
        (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
    View view = getActivity().getCurrentFocus();
    if (view == null) {
      view = new View(getActivity());
    }
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }

  @Override public void startGoogleActivity() {
    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
    startActivityForResult(signInIntent, RC_SIGN_IN);
  }

  @Override public Observable<Integer> clickOnBlog() {
    return Observable.merge(RxView.clicks(blog), RxView.clicks(blogNextButton))
        .map(__ -> 1);
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    callbackManager.onActivityResult(requestCode, resultCode, data);
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RC_SIGN_IN) {
      Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
      try {
        GoogleSignInAccount account = task.getResult(ApiException.class);
        googleLoginSubject.onNext(account);
      } catch (ApiException e) {
        Log.w(getClass().getSimpleName(), "signInResult:failed code=" + e.getStatusCode());
      }
    }
  }

  @Nullable @Override
  public android.view.View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_login, container, false);
  }

  private void setFacebookCustomListener() {
    facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
      @Override public void onSuccess(LoginResult loginResult) {
        facebookLoginSubject.onNext(loginResult);
      }

      @Override public void onCancel() {
      }

      @Override public void onError(FacebookException exception) {
      }
    });
  }
}
