package com.aptoide.uploader.apps.network;

import android.util.Log;
import com.aptoide.uploader.upload.AptoideAccountProvider;
import java.io.IOException;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

public class TokenRevalidationInterceptor {

  private final AptoideAccountProvider aptoideAccountProvider;

  private final String TAG = getClass().getSimpleName();

  public TokenRevalidationInterceptor(AptoideAccountProvider aptoideAccountProvider) {
    this.aptoideAccountProvider = aptoideAccountProvider;
  }

  Request makeTokenRefreshCall(Request request) {
    Log.d(TAG, "Retrying new request");
    String oldToken = aptoideAccountProvider.getToken()
        .blockingGet();
    String newToken = getNewAccessToken();
    RequestBody requestBody = processFormDataRequestBody(request.body(), newToken, oldToken);
    Request newRequest = request.newBuilder()
        .post(requestBody)
        .build();
    return newRequest;
  }

  private String getNewAccessToken() {
    return aptoideAccountProvider.revalidateAccessToken()
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

