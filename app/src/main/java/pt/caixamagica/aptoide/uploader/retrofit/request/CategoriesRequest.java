package pt.caixamagica.aptoide.uploader.retrofit.request;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import pt.caixamagica.aptoide.uploader.webservices.json.CategoriesResponse;
import retrofit.http.POST;

/**
 * Created by filipe on 30-10-2017.
 */

public class CategoriesRequest
    extends RetrofitSpiceRequest<CategoriesResponse, CategoriesRequest.Webservice> {

  public CategoriesRequest() {
    super(CategoriesResponse.class, CategoriesRequest.Webservice.class);
  }

  @Override public CategoriesResponse loadDataFromNetwork() throws Exception {
    //webservice has no arguments
    return getService().get();
  }

  public interface Webservice {
    @POST("/apks/groups/get") CategoriesResponse get();
  }
}
