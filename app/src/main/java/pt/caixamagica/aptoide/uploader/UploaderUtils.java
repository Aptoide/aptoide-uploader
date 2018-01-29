/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import pt.caixamagica.aptoide.uploader.util.LoadingFragment;

/**
 * Created by neuro on 02-03-2015.
 */
public class UploaderUtils {

  public static String computeSHA1sum(String text) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      byte[] sha1hash = new byte[40];
      md.update(text.getBytes("iso-8859-1"), 0, text.length());
      sha1hash = md.digest();
      return convToHex(sha1hash);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    return null;
  }

  private static String convToHex(byte[] data) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < data.length; i++) {
      int halfbyte = (data[i] >>> 4) & 0x0F;
      int two_halfs = 0;
      do {
        if ((0 <= halfbyte) && (halfbyte <= 9)) {
          buf.append((char) ('0' + halfbyte));
        } else {
          buf.append((char) ('a' + (halfbyte - 10)));
        }
        halfbyte = data[i] & 0x0F;
      } while (two_halfs++ < 1);
    }
    return buf.toString();
  }

  public static String md5Calc(File f) {
    int i;

    byte[] buffer = new byte[1024];
    int read = 0;
    String md5hash;
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      InputStream is = new FileInputStream(f);
      while ((read = is.read(buffer)) > 0) {
        digest.update(buffer, 0, read);
      }
      byte[] md5sum = digest.digest();
      BigInteger bigInt = new BigInteger(1, md5sum);
      md5hash = bigInt.toString(16);
      is.close();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

    if (md5hash.length() != 33) {
      String tmp = "";
      for (i = 1; i < (33 - md5hash.length()); i++) {
        tmp = tmp.concat("0");
      }
      md5hash = tmp.concat(md5hash);
    }

    return md5hash;
  }

  public static String computeHmacSha1(String value, String keyString)
      throws InvalidKeyException, IllegalStateException, UnsupportedEncodingException,
      NoSuchAlgorithmException {
    SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), "HmacSHA1");
    Mac mac = Mac.getInstance("HmacSHA1");
    mac.init(key);

    byte[] bytes = mac.doFinal(value.getBytes("UTF-8"));

    return convToHex(bytes);
  }

  public static <T extends FragmentActivity> void pushLoadingFragment(T context, int container,
      String str) {
    LoadingFragment loadingFragment = new LoadingFragment();
    loadingFragment.setText(str);
    if (!context.isFinishing()) {
      context.getSupportFragmentManager()
          .beginTransaction()
          .addToBackStack("LoadingFragment")
          .replace(container, loadingFragment)
          .commit();
    }
  }

  public static void popLoadingFragment(FragmentActivity context) {
    if (context.getSupportFragmentManager()
        .getBackStackEntryCount() > 0) {
      context.getSupportFragmentManager()
          .popBackStack("LoadingFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
  }

  public static void checkSpiceManagerPendingContent(final SpiceManager spiceManager,
      final String cacheKey, PendingRequestListener pendingRequestListener, Class clazz) {

    spiceManager.addListenerIfPending(clazz, cacheKey, pendingRequestListener);

    spiceManager.getFromCache(clazz, cacheKey, DurationInMillis.ONE_SECOND * 5,
        pendingRequestListener);
  }

  public static void hideKeyboard(FragmentActivity activity, View view) {
    // Hide Keyboard
    InputMethodManager imm =
        (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }
}
