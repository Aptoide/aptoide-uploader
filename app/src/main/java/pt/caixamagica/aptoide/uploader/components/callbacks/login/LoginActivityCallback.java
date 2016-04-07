/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.components.callbacks.login;

import pt.caixamagica.aptoide.uploader.model.UserInfo;

public interface LoginActivityCallback {

	void submitAuthentication(UserInfo userInfo);
//    public void submitAuthentication(String username, String password, OAuth2AuthenticationRequest.Mode mode, String nameForGoogle, String repo, String privateName, String
// privatePassword);

	void tmp();
}