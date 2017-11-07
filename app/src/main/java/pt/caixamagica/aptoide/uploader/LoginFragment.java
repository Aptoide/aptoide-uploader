/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader;

/**
 * Created by neuro on 02-02-2015.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import java.util.Arrays;
import pt.caixamagica.aptoide.uploader.activities.LoginActivity;
import pt.caixamagica.aptoide.uploader.components.callbacks.login.LoginActivityCallback;
import pt.caixamagica.aptoide.uploader.model.UserInfo;
import pt.caixamagica.aptoide.uploader.retrofit.request.OAuth2AuthenticationRequest;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginFragment extends Fragment {

  // Nome do ficheiro Shared Preferences, para controlo de versão
  public static final String SHARED_PREFERENCES_FILE = "UploaderPrefs2";

  protected View rootView;

  protected Button button;

  LoginActivityCallback mCallback;

  RelativeLayout progressLayout;

  boolean showPassword = false;

  private RelativeLayout contentLayout;

  //    private UiLifecycleHelper uiLifecycleHelper = ((MainActivity)getActivity()).uiLifecycleHelper;
  private Session.StatusCallback statusCallback = new Session.StatusCallback() {
    @Override public void call(final Session session, SessionState state, Exception exception) {
      if (state.isOpened()) {
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {

          @Override public void onCompleted(final GraphUser user, Response response) {

            String username = user.getProperty("email") == null ? "" : user.getProperty("email")
                .toString();

            if (TextUtils.isEmpty(username)) {
              session.close();

              if (getActivity() != null && !getActivity().isFinishing()) {
                getActivity().runOnUiThread(new Runnable() {
                  public void run() {
                    Toast.makeText(getActivity(), R.string.facebook_error, Toast.LENGTH_LONG)
                        .show();
                  }
                });
              }
            }

            if (session == Session.getActiveSession() && user != null) {
              String authToken = session.getAccessToken();
              OAuth2AuthenticationRequest.Mode mode = OAuth2AuthenticationRequest.Mode.facebook;

              //                            mCallback.submitAuthentication(username, authToken, mode, null, null, null, null);
              mCallback.submitAuthentication(
                  new UserInfo(username, null, authToken, mode, null, null, null, null, 0));
            }
          }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
      }
    }
  };

  /**
   * Prepara e envia o pedido de autenticação.
   *
   * @param username username
   * @param password password
   */
  private OAuth2AuthenticationRequest oAuth2AuthenticationRequest;
  private View btnGoogle;

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    ((LoginActivity) getActivity()).uiLifecycleHelper.onActivityResult(requestCode, resultCode,
        data);
  }

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mCallback = (LoginActivityCallback) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement MainActivityCallback");
    }
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setHasOptionsMenu(true);

    ((LoginActivity) getActivity()).uiLifecycleHelper =
        new UiLifecycleHelper(getActivity(), statusCallback);
    ((LoginActivity) getActivity()).uiLifecycleHelper.onCreate(savedInstanceState);
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.login_fragment, container, false);

    inputStuff();

    contentLayout = (RelativeLayout) rootView.findViewById(R.id.content);
    progressLayout = (RelativeLayout) rootView.findViewById(R.id.progressBar);
    btnGoogle = rootView.findViewById(R.id.sign_in_button);

    setUpFacebookButton();
    setUpGooglePlusButton();
    setUpSignUpButton();

    return rootView;
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    if (btnGoogle.getId() == R.id.sign_in_button
        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        && getActivity().checkSelfPermission(Manifest.permission.GET_ACCOUNTS)
        != PackageManager.PERMISSION_GRANTED) {

      btnGoogle.setEnabled(false);
    }
  }

  @Override public void onResume() {
    super.onResume();
    ((LoginActivity) getActivity()).uiLifecycleHelper.onResume();
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    ((LoginActivity) getActivity()).uiLifecycleHelper.onSaveInstanceState(outState);
  }

  @Override public void onPause() {
    super.onPause();
    ((LoginActivity) getActivity()).uiLifecycleHelper.onPause();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    ((LoginActivity) getActivity()).uiLifecycleHelper.onDestroy();
  }

  /**
   * Define a acção do botão, e adiciona um listener ao passwordText para fazer autoClick quando o
   * utilizador termina o input da password.
   */
  private void inputStuff() {
    buttonAction();

    EditText editText = (EditText) rootView.findViewById(R.id.passwordText);
    editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          button.performClick();
        }
        return true;
      }
    });

    setShowPasswordEye();
  }

  private void setUpFacebookButton() {
    LoginButton fbButton = (LoginButton) rootView.findViewById(R.id.fb_login_button);
    fbButton.setFragment(this);

    fbButton.setReadPermissions(Arrays.asList("email", "user_friends"));

    fbButton.setOnErrorListener(new LoginButton.OnErrorListener() {
      @Override public void onError(FacebookException error) {

        if (error.getMessage()
            .equals("Log in attempt aborted.")) {
          return;
        }

        error.printStackTrace();
      }
    });
  }

  private void setUpGooglePlusButton() {
    rootView.findViewById(R.id.sign_in_button)
        .setOnClickListener((LoginActivity) getActivity());
  }

  private void setUpSignUpButton() {
    View view = rootView.findViewById(R.id.sign_up_link);
    view.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        switchtoSignUpFragment();
      }
    });
  }

  /**
   * Define a acção do botão (efectuar login) <br> Lança a cosmética do loading.
   */
  private void buttonAction() {

    button = (Button) rootView.findViewById(R.id.loginButton);
    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {

        String username, password;
        TextView textView;

        textView = (TextView) rootView.findViewById(R.id.usernameText);
        username = textView.getText()
            .toString();
        textView = (TextView) rootView.findViewById(R.id.passwordText);
        password = textView.getText()
            .toString();

        if (!username.equals("") && !password.equals("")) {
          mCallback.submitAuthentication(
              new UserInfo(username, password, null, null, null, null, null, null, 0));

          ((TextView) rootView.findViewById(R.id.signing_in_text)).setText(
              "Signing in as\n" + username);

          UploaderUtils.hideKeyboard(getActivity(), getView());
        } else {
          Toast.makeText(getActivity(), R.string.missing_username_password, Toast.LENGTH_SHORT)
              .show();
        }
      }
    });
  }

  private void setShowPasswordEye() {
    final EditText password_box = (EditText) rootView.findViewById(R.id.passwordText);
    password_box.setTransformationMethod(new PasswordTransformationMethod());

    final Drawable hidePasswordRes = getResources().getDrawable(R.drawable.ic_show_password);
    final Drawable showPasswordRes = getResources().getDrawable(R.drawable.ic_hide_password);

    password_box.setCompoundDrawablesWithIntrinsicBounds(null, null, hidePasswordRes, null);
    password_box.setOnTouchListener(new View.OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        if (password_box.getCompoundDrawables()[2] == null) {
          return false;
        }
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
          return false;
        }
        if (event.getX()
            > password_box.getWidth()
            - password_box.getPaddingRight()
            - hidePasswordRes.getIntrinsicWidth()) {
          if (showPassword) {
            showPassword = false;
            password_box.setTransformationMethod(null);
            password_box.setCompoundDrawablesWithIntrinsicBounds(null, null, showPasswordRes, null);
          } else {
            showPassword = true;
            password_box.setTransformationMethod(new PasswordTransformationMethod());
            password_box.setCompoundDrawablesWithIntrinsicBounds(null, null, hidePasswordRes, null);
          }
        }

        return false;
      }
    });
  }

  private void switchtoSignUpFragment() {
    Fragment signUpFragment = new SignUpFragment();
    getFragmentManager().beginTransaction()
        .replace(R.id.container, signUpFragment)
        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        .addToBackStack("signUp")
        .commit();
  }
}