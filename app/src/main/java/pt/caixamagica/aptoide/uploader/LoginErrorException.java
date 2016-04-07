/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader;

import com.octo.android.robospice.exception.NetworkException;

/**
 * Created by neuro on 30-01-2015.
 */
public class LoginErrorException extends NetworkException {

	public LoginErrorException() {
		super("This login was unsuccessful");
	}
}
