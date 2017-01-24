/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.retrofit.request;

import android.text.TextUtils;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import pt.caixamagica.aptoide.uploader.UploaderUtils;
import pt.caixamagica.aptoide.uploader.webservices.json.SignUpJson;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by neuro on 14-04-2015.
 */
@Data @Accessors(chain = true) @EqualsAndHashCode(callSuper = false) public class SignUpRequest
    extends RetrofitSpiceRequest<SignUpJson, SignUpRequest.Webservice> {

  String email;

  String passhash;

  String name;

  String repo;

  boolean privacy;

  String privacy_user;

  String privacy_pass;

  String update;

  String hmac;

  String oem_id;

  public SignUpRequest() {
    super(SignUpJson.class, SignUpRequest.Webservice.class);
  }

  @Override public SignUpJson loadDataFromNetwork() throws Exception {

    Map<String, Object> parameters = new HashMap<>();

    parameters.put("mode", "json");
    parameters.put("email", email);
    parameters.put("passhash", passhash);
    parameters.put("name", name);
    parameters.put("repo", repo);
    parameters.put("privacy", privacy);
    parameters.put("privacy_user", privacy_user);
    parameters.put("privacy_pass", privacy_pass);
    parameters.put("update", update);

    // Enviar só os que fazem falta!
    hmac = computeHmac(email, passhash, name, repo, String.valueOf(privacy), privacy_user,
        privacy_pass, update);

    parameters.put("hmac", hmac);
    parameters.put("oem_id", oem_id);

    return getService().signUp(parameters);
  }

  public String computeHmac(String... fields) {

    List<String> strings = new LinkedList<>(Arrays.asList(fields));
    strings.removeAll(Collections.singleton(null));

    String field = TextUtils.join("", strings);

    try {
      return UploaderUtils.computeHmacSha1(field, "bazaar_hmac");
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    return null;
  }

  public interface Webservice {

    @FormUrlEncoded @POST("/2/createUser") SignUpJson signUp(@FieldMap Map<String, Object> params);
  }
}
