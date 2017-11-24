package com.aptoide.uploader.apps.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.aptoide.uploader.R;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.apps.App;
import com.aptoide.uploader.view.android.FragmentView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class MyAppsFragment extends FragmentView implements MyAppsView {

  private RecyclerView recyclerView;
  private MyAppsAdapter adapter;
  private TextView storeNameText;

  public static MyAppsFragment newInstance() {
    return new MyAppsFragment();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_my_apps, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    recyclerView = view.findViewById(R.id.fragment_my_apps_list);
    storeNameText = view.findViewById(R.id.fragment_my_apps_store_name);
    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
    recyclerView.addItemDecoration(
        new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    recyclerView.addItemDecoration(
        new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL));
    adapter = new MyAppsAdapter(getLayoutInflater(), new ArrayList<>());
    recyclerView.setAdapter(adapter);
    new MyAppsPresenter(this,
        ((UploaderApplication) getContext().getApplicationContext()).getAppsManager(),
        new CompositeDisposable(), AndroidSchedulers.mainThread()).present();
  }

  @Override public void onDestroyView() {
    adapter = null;
    recyclerView.setAdapter(null);
    recyclerView = null;
    super.onDestroyView();
  }

  @Override public void showApps(@NotNull List<App> appsList) {
    adapter.setList(appsList);
  }

  @Override public void showStoreName(@NotNull String storeName) {
    storeNameText.setText(storeName);
  }
}
