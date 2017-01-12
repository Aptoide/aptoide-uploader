package pt.caixamagica.aptoide.uploader.retrofit;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.ValidationException;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import pt.caixamagica.aptoide.uploader.AptoideUploaderApplication;
import pt.caixamagica.aptoide.uploader.retrofit.request.OAuth2AuthenticationRequest;
import pt.caixamagica.aptoide.uploader.util.SimpleTimedFuture;
import pt.caixamagica.aptoide.uploader.webservices.json.OAuth;
import pt.caixamagica.aptoide.uploader.webservices.json.UserCredentialsJson;

/**
 * Created by pedroribeiro on 12/01/17.
 */

public class OAuth2Request {

    private String refreshToken;
    private String grantType;
    private Context context = AptoideUploaderApplication.getContext();
    private static final byte[] SALT = new byte[]{-46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -21, 77, -117, -36, -113, -11, 32, -64, 89};
    String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    SpiceManager spiceManager = new SpiceManager(RetrofitSpiceServiceUploader.class);
    SharedPreferences preferences = context.getSharedPreferences("UploaderPrefs2", AptoideUploaderApplication.getContext().MODE_PRIVATE);
    AESObfuscator aesObfuscator = new AESObfuscator(SALT, context.getPackageName(), deviceId);

    public OAuth2Request() {
        try {
            refreshToken = aesObfuscator.unobfuscate(preferences.getString("refreshToken", ""), "refreshToken");
        } catch (ValidationException e) {
            e.printStackTrace();
        }
        ;
        grantType = "refresh_token";
        spiceManager.start(AptoideUploaderApplication.getContext());
    }

    public String builder() {
        final String[] refreshedToken = {""};

        OAuth2AuthenticationRequest oAuth2AuthenticationRequest = new OAuth2AuthenticationRequest();
        oAuth2AuthenticationRequest.bean.setGrant_type(grantType);
        oAuth2AuthenticationRequest.bean.setRefresh_token(refreshToken);

        final SimpleTimedFuture<String> objectSimpleTimedFuture = new SimpleTimedFuture<>();
        spiceManager.execute(oAuth2AuthenticationRequest, new RequestListener<OAuth>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                spiceException.printStackTrace();
            }

            @Override
            public void onRequestSuccess(OAuth oAuth) {
                objectSimpleTimedFuture.set(oAuth.getAccess_token());
            }
        });


        return objectSimpleTimedFuture.get(60*1000);
    }

}
