/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.retrofit.request;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.HashMap;

import pt.caixamagica.aptoide.uploader.webservices.json.UserCredentialsJson;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by neuro on 30-01-2015.
 */
public class UserCredentialsRequest extends RetrofitSpiceRequest<UserCredentialsJson, UserCredentialsRequest.Webservice> {

	private String token;

	public UserCredentialsRequest() {
		super(UserCredentialsJson.class, UserCredentialsRequest.Webservice.class);
	}

	@Override
	public UserCredentialsJson loadDataFromNetwork() throws Exception {

		HashMap<String, String> parameters = new HashMap<String, String>();

		parameters.put("access_token", token);
		parameters.put("mode", "json");

		return getService().getUserInfo(parameters);
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public interface Webservice {

		@FormUrlEncoded
		@POST("/3/getUserInfo")
		UserCredentialsJson getUserInfo(@FieldMap HashMap<String, String> args);
	}
}
