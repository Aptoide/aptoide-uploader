package com.aptoide.uploader.apps.network;

import android.util.Log;
import com.aptoide.uploader.security.AuthenticationProvider;
import java.io.IOException;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

public class TokenRevalidationInterceptor {

  private final AuthenticationProvider authenticationProvider;

  private final String TAG = getClass().getSimpleName();

  public TokenRevalidationInterceptor(AuthenticationProvider authenticationProvider) {
    this.authenticationProvider = authenticationProvider;
  }

  Request makeTokenRefreshCall(Request request) {
    Log.d(TAG, "Retrying new request");
    String oldToken = authenticationProvider.getAccessToken()
        .blockingGet();
    String newToken = getNewAccessToken();
    RequestBody requestBody = processFormDataRequestBody(request.body(), newToken, oldToken);
    Request newRequest = request.newBuilder()
        .post(requestBody)
        .build();
    return newRequest;
  }

  private String getNewAccessToken() {
    return authenticationProvider.getNewAccessToken()
        .blockingGet();
  }

  private RequestBody processFormDataRequestBody(RequestBody requestBody, String newToken,
      String oldToken) {
    String bodyString = bodyToString(requestBody);
    String newBodyString = bodyString.replace(oldToken, newToken);
    return RequestBody.create(requestBody.contentType(), newBodyString);
  }

  private String bodyToString(final RequestBody request) {
    try {
      final RequestBody copy = request;
      final Buffer buffer = new Buffer();
      if (copy != null) {
        copy.writeTo(buffer);
      } else {
        return "";
      }
      return buffer.readUtf8();
    } catch (final IOException e) {
      return "did not work";
    } catch (final IllegalStateException e) {
      return "";
    }
  }
}

