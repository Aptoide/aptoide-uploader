package com.aptoide.uploader.apps.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.aptoide.uploader.R;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.view.android.FragmentView;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class AutoUploadFragment extends FragmentView implements AutoUploadView {
  private Toolbar toolbar;
  private RecyclerView recyclerView;
  private AutoUploadAppsAdapter adapter;

  public static AutoUploadFragment newInstance() {
    return new AutoUploadFragment();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    toolbar = view.findViewById(R.id.fragment_autoupload_toolbar);
    recyclerView = view.findViewById(R.id.fragment_autoupload_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.addItemDecoration(
        new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    adapter = new AutoUploadAppsAdapter(new ArrayList<>(), SortingOrder.DATE);
    recyclerView.setAdapter(adapter);
  }

  @Override public void onDestroyView() {
    toolbar = null;
    recyclerView.setAdapter(null);
    recyclerView = null;
    adapter = null;
    super.onDestroyView();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_auto_upload, container, false);
  }

  @Override public void showApps(@NotNull List<InstalledApp> appsList) {
    adapter.setInstalledApps(appsList);
    recyclerView.scheduleLayoutAnimation();
  }

  @Override public void refreshApps(@NotNull List<InstalledApp> appsList) {
    adapter.refreshInstalledApps(appsList);
    recyclerView.scheduleLayoutAnimation();
  }

  @Override public Single<List<InstalledApp>> getSelectedApps() {
    return Single.just(adapter.getSelected());
  }
}
