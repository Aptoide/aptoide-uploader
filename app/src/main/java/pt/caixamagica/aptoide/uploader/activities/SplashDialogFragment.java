/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.activities;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import pt.caixamagica.aptoide.uploader.R;

/**
 * Created by Neurophobic Animal on 01-07-2015.
 */
public class SplashDialogFragment extends DialogFragment {

	OnHeadlineSelectedListener mCallback;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnHeadlineSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_Light);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.splash_screen, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			ImageView viewById = (ImageView) view.findViewById(R.id.splashscreen);
			viewById.setImageDrawable(getResources().getDrawable(R.drawable.splash_landscape));
		}

		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mCallback.checkStoredCredentialsCallback();
				dismiss();
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	// Container Activity must implement this interface
	public interface OnHeadlineSelectedListener {

		void checkStoredCredentialsCallback();
	}
}
