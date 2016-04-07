//package pt.caixamagica.aptoide.uploader.retrofit.request.tmp;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
//
//import java.util.HashMap;
//
//import lombok.Getter;
//import lombok.Setter;
//import pt.caixamagica.aptoide.uploader.UploaderUtils;
//import pt.caixamagica.aptoide.uploader.retrofit.LoginErrorException;
//import retrofit.RetrofitError;
//import retrofit.http.FieldMap;
//import retrofit.http.FormUrlEncoded;
//import retrofit.http.POST;
//
///**
// * Created by neuro on 21-09-2015.
// */
//public class CheckUserCredentialsRequest extends RetrofitSpiceRequest<CheckUserCredentialsResponse, CheckUserCredentialsRequest.Webservice> {
//
//    public final Bean bean = new Bean();
//
//    public class Bean {
//        @Getter @Setter private String user;
//        @Getter @Setter private String passhash;
//        @Getter @Setter private String repo;
//        @Getter @Setter private String createRepo;
//        @Getter @Setter private CheckUserCredentialsRequest.Mode authMode;
//        @Getter @Setter private String oauthToken;
//        @Getter @Setter private String oauthUserName;
//        @Getter @Setter private String oauthCreateRepo;
//        @Getter @Setter private String privacy;
//        @Getter @Setter private String privacy_user;
//        @Getter @Setter private String privacy_pass;
//        @Getter @Setter private String device_id;
//        @Getter @Setter private String model;
//        @Getter @Setter private String maxScreen;
//        @Getter @Setter private Integer maxSdk;
//        @Getter @Setter private Float maxGles;
//        @Getter @Setter private Integer myDensity;
//        @Getter @Setter private String myCpu;
//
//        @Getter private final String mode = "json";
//
//        public void setPasshash(String pass) {
//            passhash = UploaderUtils.computeSHA1sum(pass);
//        }
//    }
//
//    public enum Mode {
//        aptoide, google, facebook;
//    }
//
//    public CheckUserCredentialsRequest() {
//        super(CheckUserCredentialsResponse.class, Webservice.class);
//    }
//
//    @Override
//    public CheckUserCredentialsResponse loadDataFromNetwork() throws Exception {
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//
//        HashMap hashMap = objectMapper.convertValue(bean, HashMap.class);
//
//        CheckUserCredentialsResponse response = null;
//
//        try {
////            response = getService().oauth2Authentication(parameters);
//            response = getService().checkUserCredentials(hashMap);
//        } catch (RetrofitError error) {
//            error.printStackTrace();
//
//            throw new LoginErrorException();
//        }
//
//
//        return response;
//    }
//
//    public interface Webservice {
//        @POST("/2/checkUserCredentials")
//        @FormUrlEncoded
//        CheckUserCredentialsResponse checkUserCredentials(@FieldMap HashMap<String, String> args);
//    }
//}
