/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.webservices.json;

import java.util.List;

/**
 * Created by rmateus on 01-07-2014.
 */
public class OAuth {

	public String access_token;

	public String refresh_token;

	public String error_description;

	public List<Error> errors;

	public String status;

	public String getStatus() {
		return status;
	}

	public String getAccess_token() {
		return access_token;
	}

	public String getRefreshToken() {
		return refresh_token;
	}

	public void setRefreshToken(String refreshToken) {
		this.refresh_token = refreshToken;
	}

	public List<Error> getError() {
		return errors;
	}

	public String getError_description() {
		return error_description;
	}

	@Override
	public String toString() {

//        String separator = System.getProperty("line.separator");
		String separator = " ";

		StringBuilder builder = new StringBuilder();

		builder.append("Status: " + status + separator);
		if (errors != null) builder.append("Error Size: " + errors.size() + separator);
		builder.append("Error description: " + error_description + separator);

		return builder.toString();
	}
}
