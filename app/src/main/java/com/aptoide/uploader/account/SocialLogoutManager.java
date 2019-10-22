package com.aptoide.uploader.account;

import android.content.Context;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class SocialLogoutManager {

  private Context appContext;
  private GoogleSignInOptions gso;

  public SocialLogoutManager(Context appContext, GoogleSignInOptions gso) {
    this.appContext = appContext;
    this.gso = gso;
  }

  public void handleSocialLogout(BaseAccount.LoginType loginType) {
    switch (loginType) {
      case GOOGLE:
        handleGoogleLogout();
        break;
      case FACEBOOK:
        handleFacebookLogout();
        break;
    }
  }

  private void handleGoogleLogout() {
    GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(appContext, gso);
    mGoogleSignInClient.signOut();
  }

  private void handleFacebookLogout() {
    LoginManager.getInstance()
        .logOut();
  }
}
