/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.util;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import lombok.Setter;
import pt.caixamagica.aptoide.uploader.R;

/**
 * Created by neuro on 23-03-2015.
 */
public class LoadingFragment extends Fragment {

  @Setter String text;

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
  }

  @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    if (savedInstanceState != null) text = savedInstanceState.getString("text");

    View inflate = inflater.inflate(R.layout.loading_fragment, container, false);
    ((TextView) inflate.findViewById(R.id.loadingText)).setText(text);

    return inflate;
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    outState.putString("text", text);
    super.onSaveInstanceState(outState);
  }
}
