package pt.caixamagica.aptoide.uploader.retrofit;

import pt.caixamagica.aptoide.uploader.webservices.WebserviceOptions;

/**
 * Created by pedroribeiro on 05/04/17.
 */

public class RetrofitSpiceServiceUploaderSecondary extends RetrofitSpiceServiceUploader {
  @Override protected String getServerUrl() {
    return WebserviceOptions.BASE_HOST_SECONDARY;
  }
}
