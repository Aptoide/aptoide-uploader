/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import java.util.regex.Pattern;
import pt.caixamagica.aptoide.uploader.R;
import pt.caixamagica.aptoide.uploader.components.callbacks.login.LoginActivityCallback;
import pt.caixamagica.aptoide.uploader.model.UserInfo;

/**
 * Created by neuro on 16-09-2015.
 */
public class RepoCreatorDialog extends DialogFragment {

  LoginActivityCallback mCallback;
  UserInfo userInfo;
  private FragmentActivity context;
  private EditText repository;
  private RadioButton privateButton;
  private RadioButton publicButton;
  private EditText repoUsername;
  private EditText repoPassword;
  private boolean logoutOnDismiss = true;

  public RepoCreatorDialog() {
  }

  public static void showRepoCreatorDialog(FragmentActivity context, UserInfo userInfo) {
    DialogFragment dialog = new RepoCreatorDialog();
    Bundle bundle = new Bundle();
    bundle.putSerializable("userInfo", userInfo);
    dialog.setArguments(bundle);
    dialog.show(context.getSupportFragmentManager(), "RepoCreatorDialog");
  }

  @Override public void onResume() {
    super.onResume();

    if (privateButton.isChecked()) {
      repoPassword.setVisibility(View.VISIBLE);
      repoUsername.setVisibility(View.VISIBLE);
    }
  }

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.context = (FragmentActivity) activity;
    userInfo = (UserInfo) getArguments().getSerializable("userInfo");
    try {
      mCallback = (LoginActivityCallback) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement MainActivityCallback");
    }
  }

  @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

    if (savedInstanceState != null) {
      userInfo = (UserInfo) savedInstanceState.getSerializable("userInfo");
    }

    ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.RepoDialog);

    AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);

    LayoutInflater inflater = LayoutInflater.from(contextThemeWrapper);
    View view = inflater.inflate(R.layout.repo_creator, null);

    repository = (EditText) view.findViewById(R.id.repository);
    privateButton = (RadioButton) view.findViewById(R.id.private_store);
    publicButton = (RadioButton) view.findViewById(R.id.public_store);
    repoUsername = (EditText) view.findViewById(R.id.repo_username);
    repoPassword = (EditText) view.findViewById(R.id.repo_password);

    builder.setTitle(R.string.repo_creation_title)
        .setView(view)
        .setPositiveButton(R.string.create_repo, new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            // apagar esta overriden abaixo!
            if (validateNotEmptyFields()) {
              fillNewUserInfo();

              mCallback.submitAuthentication(userInfo);
            } else {
              Toast.makeText(context, "Please fill empty fields!", Toast.LENGTH_SHORT).show();
            }

            //                                if (repository.getText() != null) {
            //
            //                                    boolean isRepoPrivate = privateButton.isChecked();
            //
            //                                    login.setRepo(repository.getText().toString(), isRepoPrivate);
            //
            //                                    if (isRepoPrivate) {
            //                                        login.setPrivateRepoUsername(repoUsername.getText().toString().trim());
            //                                        login.setPrivateRepoPassword(repoPassword.getText().toString().trim());
            //                                    }
            //
            //                                    if (login.getRepo() != null && login.getRepo().length() != 0) {
            //                                        if (isRepoPrivate) {
            //                                            if (login.getPrivateRepoUsername() == null || login.getPrivateRepoUsername().length() == 0) {
            //                                                Toast.makeText(getActivity(), getString(R.string.store_username_undefined), Toast.LENGTH_SHORT).show();
            //                                                showRepoCreatorDialog(context, "jonao");
            //                                            } else if (login.getPrivateRepoPassword() == null || login.getPrivateRepoPassword().length() == 0) {
            //                                                Toast.makeText(getActivity(), getString(R.string.store_password_undefined), Toast.LENGTH_SHORT).show();
            //                                                showRepoCreatorDialog(context, "jonao");
            //                                            } else {
            ////                                                new CheckUserCredentials(activity).execute(login);
            //                                            }
            //                                        } else {
            ////                                            new CheckUserCredentials(activity).execute(login);
            //                                        }
            //                                    } else {
            //                                        Toast.makeText(getActivity(), getString(R.string.store_without_name), Toast.LENGTH_SHORT).show();
            //                                        showRepoCreatorDialog(context, "jonao");
            //                                    }
            //
            //                                }
            //
            //                                logoutOnDismiss = false;
            //                            }
            //                        }
            //                )
            //                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            //                    @Override
            //                    public void onClick(DialogInterface dialog, int which) {
            //                        RepoCreatorDialog.this.getDialog().cancel();
            ////                        BusProvider.getInstance().post(new LogoutEvent());
          }
        });

    //        repoUsername.setText(login.getUsername().split("@")[0]);
    //        repoUsername.setText("Teste");
    repoUsername.setText(userInfo.getUsername());

    privateButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (privateButton.isChecked()) {
          repoPassword.setVisibility(View.VISIBLE);
          repoUsername.setVisibility(View.VISIBLE);
        }
      }
    });

    publicButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (publicButton.isChecked()) {
          repoPassword.setVisibility(View.GONE);
          repoUsername.setVisibility(View.GONE);
        }
      }
    });

    InputFilter filter = new InputFilter() {
      private Pattern p = Pattern.compile("^[a-zA-Z0-9-]*$");

      @Override
      public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart,
          int dend) {
        for (int i = start; i < end; i++) {
          if (!p.matcher(Character.toString(source.charAt(i))).find()) {
            return "";
          }
        }
        return null;
      }
    };

    repository.setFilters(new InputFilter[] { filter });

    return builder.create();
  }

  @Override public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    if (logoutOnDismiss) {
      //            BusProvider.getInstance().post(new LogoutEvent());
    }
  }

  @Override public void onStart() {
    super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
    AlertDialog d = (AlertDialog) getDialog();
    if (d != null) {
      Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
      positiveButton.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          if (validateNotEmptyFields()) {
            fillNewUserInfo();

            userInfo.setCreateRepo(1);
            mCallback.submitAuthentication(userInfo);

            dismiss();
          } else {
            Toast.makeText(context, "Please fill empty fields!", Toast.LENGTH_SHORT).show();
          }
        }
      });
    }
  }

  private void fillNewUserInfo() {
    userInfo.setRepo(repository.getText().toString());

    if (privateButton.isChecked()) {
      userInfo.setPrivacyUsername(repoUsername.getText().toString());
      userInfo.setPrivacyPassword(repoPassword.getText().toString());
    }
  }

  private boolean validateNotEmptyFields() {

    if (repository.getText().toString().isEmpty()) {
      return false;
    }
    if (privateButton.isChecked()) {
      if (repoUsername.getText().toString().isEmpty() || repoPassword.getText()
          .toString()
          .isEmpty()) {
        return false;
      }
    }

    return true;
  }
}