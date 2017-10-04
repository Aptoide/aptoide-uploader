package pt.caixamagica.aptoide.uploader.retrofit.request;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import java.util.HashMap;
import pt.caixamagica.aptoide.uploader.webservices.json.GetProposedResponse;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by pedroribeiro on 03/04/17.
 */

public class GetProposedRequest
    extends RetrofitSpiceRequest<GetProposedResponse, GetProposedRequest.Webservice> {

  private String language_code;
  private String package_name;
  private int filter_vercode;

  public GetProposedRequest(String languageCode, String packageName, int filterVercode) {
    super(GetProposedResponse.class, GetProposedRequest.Webservice.class);
    language_code = languageCode;
    package_name = packageName;
    filter_vercode = filterVercode;
  }

  @Override public GetProposedResponse loadDataFromNetwork() throws Exception {
    final HashMap<String, String> parameters = new HashMap<>();

    parameters.put("language_code", language_code);
    parameters.put("package_name", package_name);
    parameters.put("filter_vercode", Integer.toString(filter_vercode));

    return getService().getProposed(parameters);
  }

  public interface Webservice {
    @POST("/apks/package/translations/getProposed") @FormUrlEncoded GetProposedResponse getProposed(
        @FieldMap HashMap<String, String> args);
  }
}
