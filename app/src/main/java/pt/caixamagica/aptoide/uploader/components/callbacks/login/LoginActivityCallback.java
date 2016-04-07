/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.components.callbacks.login;

import pt.caixamagica.aptoide.uploader.model.UserInfo;

public interface LoginActivityCallback {

	void submitAuthentication(UserInfo userInfo);
}