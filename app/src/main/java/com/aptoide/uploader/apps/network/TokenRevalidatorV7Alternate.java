package com.aptoide.uploader.apps.network;

import android.util.Log;
import com.aptoide.uploader.security.AuthenticationProvider;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TokenRevalidatorV7Alternate extends TokenRevalidationInterceptor
    implements Interceptor {

  private final String TAG = getClass().getSimpleName();

  public TokenRevalidatorV7Alternate(AuthenticationProvider authenticationProvider) {
    super(authenticationProvider);
  }

  @Override public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    Response response = chain.proceed(request);
    MediaType contentType = response.body()
        .contentType();
    String responseBodyString = response.body()
        .string();
    String errorCode = "";
    try {
      JSONObject responseBody = new JSONObject(responseBodyString);
      JSONArray errors = responseBody.getJSONArray("errors");
      errorCode = errors.getJSONObject(0)
          .getString("code");
    } catch (JSONException e) {
    }
    if (response.code() == 401 && errorCode.equals("AUTH-2")) {
      Request newRequest = makeTokenRefreshCall(request);
      return chain.proceed(newRequest);
    }
    Log.d(TAG, "INTERCEPTED: " + response.toString());
    return response.newBuilder()
        .body(ResponseBody.create(contentType, responseBodyString))
        .build();
  }
}
