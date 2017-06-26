/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.retrofit;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.android.robospice.retrofit.RetrofitJackson2SpiceService;
import com.squareup.okhttp.OkHttpClient;
import java.util.concurrent.TimeUnit;
import pt.caixamagica.aptoide.uploader.webservices.WebserviceOptions;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.Converter;
import retrofit.converter.JacksonConverter;

public class RetrofitSpiceServiceUploader extends RetrofitJackson2SpiceService {

  @Override protected Converter createConverter() {

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    return new JacksonConverter(objectMapper);
  }

  @Override public int getThreadCount() {
    return 2;
  }

  @Override protected String getServerUrl() {
    return WebserviceOptions.WebServicesLink;
  }

  protected RestAdapter.Builder createRestAdapterBuilder() {

    OkHttpClient client = new OkHttpClient();
    client.setConnectTimeout(25, TimeUnit.SECONDS);
    client.setReadTimeout(25, TimeUnit.SECONDS);
    client.setWriteTimeout(25, TimeUnit.SECONDS);

    RestAdapter.Builder builder = new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.NONE)
        .setEndpoint(getServerUrl())
        .setClient(new OkClient(client))
        .setConverter(getConverter());
    return builder;
  }
}