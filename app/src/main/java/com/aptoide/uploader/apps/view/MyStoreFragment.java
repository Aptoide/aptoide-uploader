package com.aptoide.uploader.apps.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.aptoide.uploader.R;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.permission.PermissionManager;
import com.aptoide.uploader.apps.permission.PermissionService;
import com.aptoide.uploader.view.android.FragmentView;
import com.jakewharton.rxbinding2.widget.RxAdapterView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class MyStoreFragment extends FragmentView implements MyStoreView {

  private RecyclerView recyclerView;
  private MyAppsAdapter adapter;
  private TextView storeNameText;
  private Spinner spinner;

  public static MyStoreFragment newInstance() {
    return new MyStoreFragment();
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
    spinner = view.findViewById(R.id.sort_spinner);
    prepareSpinner(R.array.sort_spinner_array);
    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
    recyclerView.addItemDecoration(new GridDividerItemDecoration(
        getResources().getDimensionPixelSize(R.dimen.apps_grid_item_margin)));
    adapter = new MyAppsAdapter(new ArrayList<>());
    recyclerView.setAdapter(adapter);
    new MyStorePresenter(this,
        ((UploaderApplication) getContext().getApplicationContext()).getAppsManager(),
        new CompositeDisposable(), AndroidSchedulers.mainThread(), new PermissionManager(),
        (PermissionService) getContext()).present();
  }

  @Override public void onDestroyView() {
    adapter = null;
    spinner = null;
    storeNameText = null;
    recyclerView.setAdapter(null);
    recyclerView = null;
    super.onDestroyView();
  }

  private void prepareSpinner(int arrayId) {
    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(getActivity(), arrayId, R.layout.spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
  }

  @Override public void showApps(@NotNull List<InstalledApp> appsList) {
    adapter.setList(appsList);
  }

  @Override public void showStoreName(@NotNull String storeName) {
    storeNameText.setText(storeName);
  }

  @Override public Observable<List<InstalledApp>> submitAppEvent() {
    return Observable.empty();
  }

  @Override public Observable<SortingOrder> orderByEvent() {
    return RxAdapterView.itemSelections(spinner)
        .map(integer -> {
          if (integer == 0) {
            return SortingOrder.DATE;
          } else {
            return SortingOrder.NAME;
          }
        });
  }
}
