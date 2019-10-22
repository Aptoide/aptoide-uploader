package com.aptoide.uploader.apps.network;

import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import com.aptoide.uploader.BuildConfig;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class UserAgentInterceptor implements Interceptor {

  private final IdsRepository idsRepository;
  private final DisplayMetrics displayMetrics = new DisplayMetrics();

  public UserAgentInterceptor(IdsRepository idsRepository) {
    this.idsRepository = idsRepository;
  }

  @Override public Response intercept(Chain chain) throws IOException {
    Request originalRequest = chain.request();

    String userAgent = null;
    try {
      userAgent = getDefaultUserAgent();
    } catch (Exception e) {

    }

    Response response;
    try {
      if (!TextUtils.isEmpty(userAgent)) {
        Request requestWithUserAgent = originalRequest.newBuilder()
            .header("User-Agent", userAgent)
            .build();
        response = chain.proceed(requestWithUserAgent);
      } else {
        response = chain.proceed(originalRequest);
      }
      return response;
    } catch (IOException e) {
      throw e;
    }
  }

  private String getDefaultUserAgent() {

    String screen = displayMetrics.widthPixels + "x" + displayMetrics.heightPixels;

    final StringBuilder sb = new StringBuilder(
        "uploader-" + BuildConfig.VERSION_NAME + ";" + TERMINAL_INFO + ";" + screen + ";id:");

    String uniqueIdentifier = idsRepository.getUniqueIdentifier();
    if (uniqueIdentifier != null) {
      sb.append(uniqueIdentifier);
    }
    sb.append(";");
    sb.append(";");
    return sb.toString();
  }

  public static final String TERMINAL_INFO =
      getModel() + "(" + getProduct() + ")" + ";v" + getRelease() + ";" + System.getProperty(
          "os.arch");

  public static String getProduct() {
    return Build.PRODUCT.replace(";", " ");
  }

  public static String getModel() {
    return Build.MODEL.replaceAll(";", " ");
  }

  public static String getRelease() {
    return Build.VERSION.RELEASE.replaceAll(";", " ");
  }
}