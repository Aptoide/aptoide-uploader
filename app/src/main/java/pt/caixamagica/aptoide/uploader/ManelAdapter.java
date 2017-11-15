/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.manuelpeinado.multichoiceadapter.CheckableFrameLayout;
import com.manuelpeinado.multichoiceadapter.MultiChoiceBaseAdapter;
import com.squareup.picasso.Picasso;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import pt.caixamagica.aptoide.uploader.webservices.json.UserCredentialsJson;

/**
 * Created by neuro on 09-02-2015.
 */
public class ManelAdapter extends MultiChoiceBaseAdapter {

  private final FragmentActivity context;
  private final Fragment baseFragment;
  public List<SelectablePackageInfo> mDataset;
  Comparator comparator = new Comparator<PriorityRunnable>() {
    @Override public int compare(PriorityRunnable lhs, PriorityRunnable rhs) {
      return (int) (lhs.priority - rhs.priority);
    }
  };

  ThreadPoolExecutor threadPool = new ThreadPoolExecutor(5, 5, 1, TimeUnit.SECONDS,
      new PriorityBlockingQueue<Runnable>(5, comparator));

  boolean actionModeActivaded = false;

  ManelAdapterShowListener listener;

  private UserCredentialsJson userCredentialsJson;

  public ManelAdapter(Bundle savedInstanceState, View view, Fragment fragment,
      List<SelectablePackageInfo> mDataset, UserCredentialsJson userCredentialsJson) {
    super(savedInstanceState);
    this.userCredentialsJson = userCredentialsJson;
    this.baseFragment = fragment;
    this.context = fragment.getActivity();
    this.mDataset = mDataset;
  }

  public void setListener(ManelAdapterShowListener listener, View view) {
    this.listener = listener;
    if (actionModeActivaded) {
      if (view.findViewById(R.id.submitAppsButton) != null) {
        view.findViewById(R.id.submitAppsButton)
            .setVisibility(View.VISIBLE);
      }
      listener.hide();
    }
  }

  @Override protected View getViewImpl(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      int layout = R.layout.manel_checkable;
      LayoutInflater inflater = LayoutInflater.from(getContext());
      convertView = inflater.inflate(layout, parent, false);
    }

    CheckableFrameLayout checkableRelativeLayout = (CheckableFrameLayout) convertView;
    ImageView appIcon = (ImageView) convertView.findViewById(R.id.appIcon);
    ImageView cloudIcon = (ImageView) convertView.findViewById(R.id.appInCloud);
    final TextView appName = (TextView) convertView.findViewById(R.id.appName);
    final SelectablePackageInfo apk = mDataset.get(position);

    Uri uri = Uri.parse("android.resource://" + apk.packageName + "/" + apk.applicationInfo.icon);
    Picasso.with(AptoideUploaderApplication.getContext())
        .load(uri)
        .into(appIcon);

    if (apk.isUploaded()) {
      cloudIcon.setVisibility(View.VISIBLE);
    } else {
      cloudIcon.setVisibility(View.GONE);
    }

    threadPool.execute(new PriorityRunnable() {
      @Override public void run() {
        final CharSequence charSequence = apk.applicationInfo.loadLabel(
            AptoideUploaderApplication.getContext()
                .getPackageManager());

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
          @Override public void run() {
            appName.setText(charSequence);
          }
        });
      }
    });

    return checkableRelativeLayout;
  }

  @Override public void onDestroyActionMode(ActionMode mode) {
    super.onDestroyActionMode(mode);
    listener.show();
    actionModeActivaded = false;

    baseFragment.setHasOptionsMenu(true);
  }

  @Override public int getCount() {
    return mDataset.size();
  }

  @Override public SelectablePackageInfo getItem(int position) {
    return mDataset.get(position);
  }

  @Override public long getItemId(int position) {
    return position;
  }

  void ffinishActionMode() {
    finishActionMode();
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    MenuInflater inflater = null;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      inflater = mode.getMenuInflater();
      inflater.inflate(R.menu.menu_actionmode, menu);
    }

    if (listener != null) {
      listener.hide();
    }

    baseFragment.setHasOptionsMenu(false);

    actionModeActivaded = true;
    return true;
  }

  @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    return false;
  }

  @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    return false;
  }

  public void uncheckAll() {
    for (Long checked : getCheckedItems()) {
      setItemChecked(checked, false);
    }
  }

  public interface ManelAdapterShowListener {

    void show();

    void hide();
  }
}
