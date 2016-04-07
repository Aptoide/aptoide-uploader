/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.retrofit;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.android.robospice.retrofit.RetrofitJackson2SpiceService;

import pt.caixamagica.aptoide.uploader.webservices.WebserviceOptions;
import retrofit.RestAdapter;
import retrofit.converter.Converter;
import retrofit.converter.JacksonConverter;

public class RetrofitSpiceServiceUploader extends RetrofitJackson2SpiceService {

	@Override
	protected Converter createConverter() {

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		return new JacksonConverter(objectMapper);
	}

	@Override
	protected String getServerUrl() {
		return WebserviceOptions.WebServicesLink;
	}

	protected RestAdapter.Builder createRestAdapterBuilder() {

		RestAdapter.Builder builder = new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.NONE).setEndpoint(getServerUrl()).setConverter(getConverter());

		return builder;
	}

	@Override
	public int getThreadCount() {
		return 2;
	}
}