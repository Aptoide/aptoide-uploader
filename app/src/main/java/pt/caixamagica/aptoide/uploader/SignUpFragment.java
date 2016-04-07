/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

import pt.caixamagica.aptoide.uploader.components.callbacks.login.LoginActivityCallback;
import pt.caixamagica.aptoide.uploader.model.UserInfo;
import pt.caixamagica.aptoide.uploader.retrofit.RetrofitSpiceServiceUploader;
import pt.caixamagica.aptoide.uploader.retrofit.request.SignUpRequest;
import pt.caixamagica.aptoide.uploader.webservices.json.Error;
import pt.caixamagica.aptoide.uploader.webservices.json.SignUpJson;

/**
 * Created by neuro on 13-04-2015.
 */
public class SignUpFragment extends Fragment {

	private SpiceManager spiceManager = new SpiceManager(RetrofitSpiceServiceUploader.class);

	private EditText emailEditText;

	private EditText passwordEditText;

	private EditText storeEditText;

	private EditText storeUsernameEditText;

	private EditText storePasswordEditText;

	private RadioGroup storeVisibilityRadioGroup;

	private boolean publicStore = true;

	private LoginActivityCallback mCallback;

	private Button createAccountButton;

	private View rootView;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (LoginActivityCallback) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement MainActivityCallback");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.sign_up_fragment, container, false);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setupViewAttributes();

		setDefaultStoreVisibility();
		setupBackLink();
		setupRecoverPasswordLink();

		setupStoreVisibilityListeners();

		setupSubmitButton();
	}

	@Override
	public void onStart() {
		super.onStart();
		spiceManager.start(getActivity());
	}

	@Override
	public void onStop() {
		super.onStop();
		spiceManager.shouldStop();
	}

	private void setupViewAttributes() {
		emailEditText = (EditText) rootView.findViewById(R.id.email);
		passwordEditText = (EditText) rootView.findViewById(R.id.password);
		storeEditText = (EditText) rootView.findViewById(R.id.store);
		storeUsernameEditText = (EditText) rootView.findViewById(R.id.store_username);
		storePasswordEditText = (EditText) rootView.findViewById(R.id.store_password);
		storeVisibilityRadioGroup = (RadioGroup) rootView.findViewById(R.id.store_visibility);
		createAccountButton = (Button) rootView.findViewById(R.id.create_Account);
	}

	private void setupSubmitButton() {
		createAccountButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (validateFields(emailEditText, passwordEditText, storeEditText)) {
					if (publicStore || (!publicStore && validateFields(storeUsernameEditText, storePasswordEditText))) {
						createAccount();
						UploaderUtils.hideKeyboard(getActivity(), getView());
					} else Toast.makeText(getActivity(), "Missing Fields", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(), "Missing Fields", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void createAccount() {
		final SignUpRequest signUpRequest = new SignUpRequest();

		String passhash = UploaderUtils.computeSHA1sum(passwordEditText.getText().toString());

		signUpRequest.setEmail(emailEditText.getText().toString()).setPasshash(passhash).setRepo(storeEditText.getText().toString()).setPrivacy(!publicStore);

		if (!storeUsernameEditText.getText().toString().equals("") && !storePasswordEditText.getText().toString().equals(""))
			signUpRequest.setPrivacy_user(storeUsernameEditText.getText().toString()).setPrivacy_pass(storePasswordEditText.getText().toString());

		spiceManager.execute(signUpRequest, new RequestListener<SignUpJson>() {
			@Override
			public void onRequestFailure(SpiceException spiceException) {
				Toast.makeText(getActivity(), "Sorry, an error occurred", Toast.LENGTH_SHORT);
			}

			@Override
			public void onRequestSuccess(SignUpJson signUpJson) {
				if (signUpJson.getErrors() == null) {
					// Loja criada com sucesso, redirecciona para a AppsView
					mCallback.submitAuthentication(new UserInfo(signUpRequest.getEmail(), passwordEditText.getText().toString(), null, null, null, null, null, null, 0));
				} else {
					List<String> errors = new LinkedList<>();
					for (Error error : signUpJson.getErrors()) {
						if (error.getMsg() != null) errors.add(error.getMsg());
						else errors.add(error.getCode());
					}
					String message = StringUtils.join(errors.toArray(), ", ");
					Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	private boolean validateFields(EditText... editTexts) {

		for (EditText editText : editTexts) {
			if (TextUtils.isEmpty(editText.getText())) {
				return false;
			}
		}
		return true;
	}

	private void setupStoreVisibilityListeners() {
		storeVisibilityRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int selected = checkedId;
				if (selected == R.id.store_private) {
					publicStore = false;
					storeUsernameEditText.setVisibility(View.VISIBLE);
					storePasswordEditText.setVisibility(View.VISIBLE);
				} else {
					publicStore = true;
					storeUsernameEditText.setVisibility(View.GONE);
					storePasswordEditText.setVisibility(View.GONE);
				}
			}
		});
	}

	private void setDefaultStoreVisibility() {
		((RadioButton) rootView.findViewById(R.id.store_public)).setChecked(true);
	}

	private void setupBackLink() {
		View view = rootView.findViewById(R.id.already_have_an_account);
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager().popBackStack();
			}
		});
	}

	private void setupRecoverPasswordLink() {
		View view = rootView.findViewById(R.id.forgot_password);
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String download_link = "https://www.aptoide.com/account/password-recovery";
				Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(download_link));
				startActivity(myIntent);
				getFragmentManager().popBackStack();
			}
		});
	}
}
