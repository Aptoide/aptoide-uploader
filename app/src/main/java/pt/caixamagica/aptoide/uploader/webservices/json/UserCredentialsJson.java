/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.webservices.json;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by brutus on 09-12-2013.
 */
@ToString
@Data
public class UserCredentialsJson implements Serializable {

	public String status;

	@Setter @Getter
	public String refreshToken;
	public String token;
	public String avatar;
	public String username;
	public String email;
	public String queueName;
	public List<Error> errors;
	public Settings settings;
	String repo;
	@Getter private String error;
	@Getter private String error_description;

	public Settings getSettings() {
		return settings;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public List<Error> getErrors() {
		return errors;
	}

	public void setErrors(List<Error> errors) {
		this.errors = errors;
	}

	public String getQueue() {
		return queueName;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRepo() {
		return repo;
	}

	public static class Settings implements Serializable {

		public String timeline;

		public String getTimeline() {
			return timeline;
		}
	}
}
