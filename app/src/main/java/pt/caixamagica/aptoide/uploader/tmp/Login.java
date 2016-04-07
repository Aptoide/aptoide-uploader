/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.tmp;

import pt.caixamagica.aptoide.uploader.UploaderUtils;

/**
 * Created with IntelliJ IDEA. User: rmateus Date: 31-07-2013 Time: 15:29 To change this template use File | Settings | File Templates.
 */
public class Login {

	private boolean fromAccountManager = false;
	private String username;
	private String password;
	private String repo;
	private boolean isPrivateRepo;
	private String privateRepoUsername;
	private String privateRepoPassword;
	private boolean fromSignUp;
	private boolean fromUpdate = false;
	private LoginMode loginMode;
	private String oAuthAccessToken;
	private String oAuthMode;
	private String oAuthUsername;

	public Login(String username, String password, boolean fromSignUp) {
		this.username = username;
		this.password = password;
		this.fromSignUp = fromSignUp;
		setLoginMode(LoginMode.REGULAR);
	}

	public Login(String username, String oAuthAccessToken, String oAuthMode) {
		this.username = username;
		this.oAuthAccessToken = oAuthAccessToken;
		this.oAuthMode = oAuthMode;
		setLoginMode(LoginMode.FACEBOOK_OAUTH);
	}

	public Login(String username, String oAuthAccessToken, String oAuthMode, String oAuthUsername) {
		this.username = username;
		this.oAuthAccessToken = oAuthAccessToken;
		this.oAuthMode = oAuthMode;
		this.oAuthUsername = oAuthUsername;
		setLoginMode(LoginMode.GOOGLE_OAUTH);
	}

	public Login() {
		this("", "", false);
	}

	public boolean isFromAccountManager() {
		return fromAccountManager;
	}

	public void setFromAccountManager(boolean fromAccountManager) {
		this.fromAccountManager = fromAccountManager;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getPasswordSha1() {

		if (fromUpdate) {
			return password;
		}

		return UploaderUtils.computeSHA1sum(getPassword());
	}

	public void setRepo(String repo, boolean privateRepo) {
		this.repo = repo;
		this.isPrivateRepo = privateRepo;
	}

	public String getRepo() {
		return repo;
	}

	public void setRepo(String repo) {
		this.repo = repo;
	}

	public boolean isRepoPrivate() {
		return isPrivateRepo;
	}

	public LoginMode getLoginMode() {
		return loginMode;
	}

	public void setLoginMode(LoginMode loginMode) {
		this.loginMode = loginMode;
	}

	public String getoAuthAccessToken() {
		return oAuthAccessToken;
	}

	public String getoAuthMode() {
		return oAuthMode;
	}

	public String getoAuthUsername() {
		return oAuthUsername;
	}

	public boolean isFromSignUp() {
		return fromSignUp;
	}

	public void setFromSignUp(boolean fromSignup) {
		this.fromSignUp = fromSignup;
	}

	public void setFromUpdate(boolean fromUpdate) {
		this.fromUpdate = fromUpdate;
	}

	public boolean isUpdate() {
		return fromUpdate;
	}

	public String getPrivateRepoUsername() {
		return privateRepoUsername;
	}

	public void setPrivateRepoUsername(String privateRepoUsername) {
		this.privateRepoUsername = privateRepoUsername;
	}

	public String getPrivateRepoPassword() {
		return privateRepoPassword;
	}

	public void setPrivateRepoPassword(String privateRepoPassword) {
		this.privateRepoPassword = privateRepoPassword;
	}

	public enum LoginMode {
		REGULAR, FACEBOOK_OAUTH, GOOGLE_OAUTH
	}
}
