/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import pt.caixamagica.aptoide.uploader.retrofit.request.OAuth2AuthenticationRequest;

/**
 * Created by neuro on 18-09-2015.
 */
@Data
@AllArgsConstructor(suppressConstructorProperties = true)
public class UserInfo implements Serializable {

	private String username;
	private String password;
	private String oauthToken;
	private OAuth2AuthenticationRequest.Mode mode;
	//    private CheckUserCredentialsRequest.Mode mode;
	private String nameForGoogle;
	private String repo;
	private String privacyUsername;
	private String privacyPassword;
	private int createRepo = 0;
}
