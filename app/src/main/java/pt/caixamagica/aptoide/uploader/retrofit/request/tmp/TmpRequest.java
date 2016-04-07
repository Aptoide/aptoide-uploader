/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.retrofit.request.tmp;

import android.text.TextUtils;
import android.util.Pair;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;
import pt.caixamagica.aptoide.uploader.UploaderUtils;
import retrofit.http.EncodedPath;
import retrofit.http.GET;

/**
 * Created by neuro on 11-02-2015.
 */
public class TmpRequest extends RetrofitSpiceRequest<TmpResponse, TmpRequest.Webservice> {

	private String apkid;

	@Getter @Setter private String repo;

	@Getter @Setter private String apkPath;

	public TmpRequest() {
		super(TmpResponse.class, TmpRequest.Webservice.class);
	}

	public String getApkid() {
		return apkid;
	}

	public void setApkid(String apkid) {
		this.apkid = apkid;
	}

	@Override
	public TmpResponse loadDataFromNetwork() throws Exception {

		ArrayList<Pair> options = new ArrayList<>();

		options.add(new StringedPair("option1", "value1"));
		options.add(new StringedPair("option2", "value2"));
		options.add(new StringedPair("option3", "value3"));

		HashMap<String, String> parameters = new HashMap<String, String>();

		parameters.put("mode", "json");

		if (repo != null) parameters.put("repo", repo);

		String join = TextUtils.join(",", options);

		options = new ArrayList<>();

		if (apkPath != null) {
			parameters.put("identif", "md5sum:" + UploaderUtils.md5Calc(new File(apkPath)));
		} else parameters.put("identif", "package:" + apkid);

		return getService().getApkInfo("ws2.aptoide.com/api/7/getStore/store_id/15/nview/response/context/store/lang/pt-pt");
	}

	public interface Webservice {

		@GET("/{owner}")
		TmpResponse getApkInfo(@EncodedPath("owner") String owner);
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
