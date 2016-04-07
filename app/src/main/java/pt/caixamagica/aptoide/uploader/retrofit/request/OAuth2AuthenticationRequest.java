/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.retrofit.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pt.caixamagica.aptoide.uploader.retrofit.LoginErrorException;
import pt.caixamagica.aptoide.uploader.webservices.json.OAuth;
import retrofit.RetrofitError;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by neuro on 29-01-2015.
 */
@ToString
public class OAuth2AuthenticationRequest extends RetrofitSpiceRequest<OAuth, OAuth2AuthenticationRequest.Webservice> {

	public final Bean bean = new Bean();

	public OAuth2AuthenticationRequest() {
		super(OAuth.class, Webservice.class);
	}

	@Override
	public OAuth loadDataFromNetwork() throws Exception {

//        if (!getMode().equals(Mode.APTOIDE)) {
//            setUsername(null);
//        }

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		String s = objectMapper.writeValueAsString(bean);

		HashMap hashMap = objectMapper.convertValue(bean, HashMap.class);
		s = objectMapper.writeValueAsString(hashMap);

//        System.out.println("Debug: JsonShit: " + s);
//        System.out.println("Debug: JsonShit: " + bean.getAuthMode());

//        System.out.println("Debug: loadDataFromNetwork: ");
//        System.out.println("Debug: " + toString());

		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("grant_type", "password");
		parameters.put("client_id", "Aptoide");
		parameters.put("mode", "json");

		if (bean.getAuthMode() != null) {
			switch (bean.getAuthMode()) {
				case aptoide:
					parameters.put("username", bean.getUsername());
					parameters.put("password", bean.getPassword());
					break;
				case google:
					parameters.put("authMode", "google");
					parameters.put("oauthUserName", bean.getNameForGoogle());
					parameters.put("oauthToken", bean.getPassword());
					break;
				case facebook:
					parameters.put("authMode", "facebook");
					parameters.put("oauthToken", bean.getPassword());
					break;
			}
		} else {
			parameters.put("username", bean.getUsername());
			parameters.put("password", bean.getPassword());
		}

		if (bean.getRepo() != null && (bean.getAuthMode() == Mode.facebook || bean.getAuthMode() == Mode.google)) {
//            System.out.println("Debug: Passei no público");
			parameters.put("oauthCreateRepo", "1");
			parameters.put("repo", bean.getRepo());
			if (bean.getPrivacy_user() != null && bean.getPrivacy_pass() != null) {
//                System.out.println("Debug: Passei no Privado");
				parameters.put("privacy_user", bean.getPrivacy_user());
				parameters.put("privacy_pass", bean.getPrivacy_pass());
			}
		}

		OAuth response = null;

		try {
//            response = getService().oauth2Authentication(parameters);
			response = getService().oauth2Authentication(hashMap);
		} catch (RetrofitError error) {
			error.printStackTrace();

			throw new LoginErrorException();
		}

		return response;
	}

//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }

	public enum Mode {
		aptoide, google, facebook;

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	public interface Webservice {

		@POST("/3/oauth2Authentication")
		@FormUrlEncoded
		OAuth oauth2Authentication(@FieldMap HashMap<String, String> args);
	}

	public class Bean {

		@JsonProperty("mode") public final String tmp = "json";
		public final String grant_type = "password";
		public final String client_id = "Aptoide";
		@Getter @Setter private String username;
		@Getter @Setter private String password;
		@Getter @Setter private String oauthToken;
		@Getter @Setter private String oauthUserName;
		@Getter @Setter private String oauthCreateRepo;
		@Getter @Setter private String isPrivate;
		@Getter @Setter private String repo;
		// Private store
		@Getter @Setter private String privacy_user;
		@Getter @Setter private String privacy_pass;
		//        @JsonProperty("authMode")
//        @Getter @Setter private String mode;
		@Getter @Setter private Mode authMode;
		@Getter @Setter private String nameForGoogle;

//        public String getAuthMode() {
//            return mode.toString();
//        }
//
//        public void setAuthMode(String authMode) {
//            mode = Mode.valueOf(authMode.toUpperCase());
//        }
	}
}
