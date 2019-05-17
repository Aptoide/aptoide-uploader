package pt.caixamagica.aptoide.uploader.retrofit;

import pt.caixamagica.aptoide.uploader.webservices.WebserviceOptions;

public class RetrofitSpiceServiceUploaderPrimary extends RetrofitSpiceServiceUploader {
  @Override protected String getServerUrl() {
    return WebserviceOptions.BASE_HOST;
  }
}