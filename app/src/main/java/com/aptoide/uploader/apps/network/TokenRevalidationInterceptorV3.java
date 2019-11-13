package com.aptoide.uploader.apps.network;

import android.util.Log;
import com.aptoide.uploader.security.AuthenticationProvider;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;

public class TokenRevalidationInterceptorV3 extends TokenRevalidationInterceptor
    implements Interceptor {

  private final String TAG = getClass().getSimpleName();

  public TokenRevalidationInterceptorV3(AuthenticationProvider authenticationProvider) {
    super(authenticationProvider);
  }

  @Override public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();

    Response response = chain.proceed(request);
    MediaType contentType = response.body()
        .contentType();
    String responseBodyString = response.body()
        .string();
    String error = "";
    try {
      JSONObject responseBody = new JSONObject(responseBodyString);
      error = responseBody.get("error")
          .toString();
    } catch (JSONException e) {
      Log.d(TAG, "No error");
    }
    if (response.code() == 401 && error.equals("invalid_token")) {
      Request newRequest = makeTokenRefreshCall(request);
      return chain.proceed(newRequest);
    }
    Log.d(TAG, "INTERCEPTED:$ " + response.toString());
    return response.newBuilder()
        .body(ResponseBody.create(contentType, responseBodyString))
        .build();
  }
}
