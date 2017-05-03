/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;
import pt.caixamagica.aptoide.uploader.R;

/**
 * Created by neuro on 24-04-2015.
 */
public class ConfirmationDialog extends DialogFragment {

  private ConfirmCallback callback;

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.callback = (ConfirmCallback) activity;
  }

  @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

    AlertDialog alertDialog =
        new AlertDialog.Builder(getActivity()).setIcon(R.drawable.ic_dialog_alert_holo_light)
            .setTitle(R.string.quit_message)
            .setMessage(R.string.exit_confirmation)
            .setPositiveButton(R.string.yes, callback)
            .setNegativeButton(R.string.no, null)
            .show();

    int textViewId =
        alertDialog.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
    TextView tv = (TextView) alertDialog.findViewById(textViewId);
    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        .setTextColor(getResources().getColor(R.color.wallet_holo_blue_light));
    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        .setTextColor(getResources().getColor(R.color.wallet_holo_blue_light));
    tv.setTextColor(Color.BLACK);

    return alertDialog;
  }

  public interface ConfirmCallback extends DialogInterface.OnClickListener {

  }
}
