package pt.caixamagica.aptoide.uploader.retrofit.request;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import lombok.Data;
import pt.caixamagica.aptoide.uploader.webservices.json.CategoriesResponse;
import retrofit.http.POST;

/**
 * Created by filipe on 30-10-2017.
 */

public class CategoriesRequest
    extends RetrofitSpiceRequest<CategoriesResponse, CategoriesRequest.Webservice> {

  private Body body;

  public CategoriesRequest(int offset, String language) {
    super(CategoriesResponse.class, CategoriesRequest.Webservice.class);
    this.body = new Body(offset, language);
  }

  @Override public CategoriesResponse loadDataFromNetwork() throws Exception {
    return getService().get(body);
  }

  public interface Webservice {
    @POST("/apks/groups/get/groups_depth=1") CategoriesResponse get(@retrofit.http.Body Body args);
  }

  @Data private class Body {

    private final int offset;
    private final String language;

    private Body(int offset, String language) {
      this.offset = offset;
      this.language = language;
    }
  }
}
