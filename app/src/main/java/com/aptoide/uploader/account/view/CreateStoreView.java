package com.aptoide.uploader.account.view;

import android.content.DialogInterface;
import android.util.Log;
import androidx.annotation.Nullable;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public interface CreateStoreView extends View {

  Observable<CreateStoreViewModel> getStoreInfo();

  Observable<CreateStoreViewModel> getOpenLoginView();

  Observable<CreateStoreViewModel> getOpenRecoverPasswordView();

  void showLoading();

  void hideLoading();

  void showInvalidFieldError(String messageError);

  void showNetworkError();

  void showErrorUserAlreadyExists();

  void showErrorStoreAlreadyExists();

  void hideKeyboard();

  Observable<Boolean> pressBack();

  void showDialog();

  void dismissDialog();

  Observable<DialogInterface> positiveClick();

  void showError();

  class CreateStoreViewModel {
    private final String storeName;
    private final String storeUser;
    private final String storePassword;
    private boolean privacyFlag = false;

    public CreateStoreViewModel(String storeName) {
      this(storeName, null, null);
    }

    public CreateStoreViewModel(String storeName, @Nullable String storeUser,
        @Nullable String storePassword) {
      this.storeName = storeName;
      this.storeUser = storeUser;
      this.storePassword = storePassword;
    }

    public String getStoreName() {
      return storeName;
    }

    public String getStoreUser() {
      return storeUser;
    }

    public String getStorePassword() {
      return storePassword;
    }

    public boolean isPrivateStore() {
      return storePassword != null && !storePassword.isEmpty();
    }

    public boolean getPrivacyFlag() {
      return privacyFlag;
    }

    public void setPrivacyFlag(boolean flag) {
      privacyFlag = flag;
    }
  }

  class AlgorithmU {

    private static final String TAG = AlgorithmU.class.getName();

    public static String computeSha1(String text) {
      try {
        return convToHex(computeSha1(text.getBytes("iso-8859-1")));
      } catch (UnsupportedEncodingException e) {
        Log.e(TAG, "computeSha1(String): " + e);
      }
      return "";
    }

    private static String convToHex(byte[] data) {
      final StringBuilder buffer = new StringBuilder();
      for (byte b : data) {
        buffer.append(Integer.toString((b & 0xff) + 0x100, 16)
            .substring(1));
      }
      return buffer.toString();
    }

    private static byte[] computeSha1(byte[] bytes) {
      MessageDigest md;
      try {
        md = MessageDigest.getInstance("SHA-1");
        md.update(bytes, 0, bytes.length);
        return md.digest();
      } catch (NoSuchAlgorithmException e) {
        Log.e(TAG, "computeSha1: " + e);
      }

      return new byte[0];
    }
  }
}