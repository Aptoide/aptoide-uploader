/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.retrofit.request;

import android.util.Pair;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;
import pt.caixamagica.aptoide.uploader.UploaderUtils;
import pt.caixamagica.aptoide.uploader.webservices.json.GetApkInfoJson;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by neuro on 11-02-2015.
 */
public class GetApkInfoRequest extends RetrofitSpiceRequest<GetApkInfoJson, GetApkInfoRequest.Webservice> {

	private String apkid;

	@Getter @Setter private String repo;

	@Getter @Setter private String apkPath;

	public GetApkInfoRequest() {
		super(GetApkInfoJson.class, GetApkInfoRequest.Webservice.class);
	}

	public String getApkid() {
		return apkid;
	}

	public void setApkid(String apkid) {
		this.apkid = apkid;
	}

	@Override
	public GetApkInfoJson loadDataFromNetwork() throws Exception {

		ArrayList<Pair> options = new ArrayList<>();

		options.add(new StringedPair("option1", "value1"));
		options.add(new StringedPair("option2", "value2"));
		options.add(new StringedPair("option3", "value3"));

		HashMap<String, String> parameters = new HashMap<String, String>();

		parameters.put("mode", "json");

		if (repo != null) parameters.put("repo", repo);

		if (apkPath != null) {
			parameters.put("identif", "md5sum:" + UploaderUtils.md5Calc(new File(apkPath)));
		} else parameters.put("identif", "package:" + apkid);

		return getService().getApkInfo(parameters);
	}

	public interface Webservice {

		@FormUrlEncoded
		@POST("/3/getApkInfo")
		GetApkInfoJson getApkInfo(@FieldMap HashMap<String, String> args);
	}

	public class StringedPair extends Pair {

		/**
		 * Constructor for a Pair.
		 *
		 * @param first  the first object in the Pair
		 * @param second the second object in the pair
		 */
		public StringedPair(Object first, Object second) {
			super(first, second);
		}

		@Override
		public String toString() {
			return first + ":" + second;
		}
	}
}
