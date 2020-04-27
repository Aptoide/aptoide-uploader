package com.aptoide.uploader.account.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.aptoide.uploader.R;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.view.android.FragmentView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class MaintenanceFragment extends FragmentView implements MaintenanceView {

  private View progressbar;
  private View maintenanceView;
  private TextView title;
  private TextView message_first;
  private TextView message_second;
  private TextView blog;
  private View socialLogins;

  public static MaintenanceFragment newInstance() {
    return new MaintenanceFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    progressbar = view.findViewById(R.id.fragment_maintenance_progressbar);
    maintenanceView = view.findViewById(R.id.fragment_maintenance_view);
    title = view.findViewById(R.id.fragment_maintenance_title);
    message_first = view.findViewById(R.id.fragment_maintenance_message1);
    message_second = view.findViewById(R.id.fragment_maintenance_message2);
    blog = view.findViewById(R.id.fragment_maintenance_blog);
    //socialLogins = view.findViewById(R.id.fragment_maintenance_logins);
    // TODO: 4/27/20 create group and hide them at the same time !
    new MaintenancePresenter(this, new MaintenanceNavigator(),
        ((UploaderApplication) getContext().getApplicationContext()).getMaintenanceManager(),
        new CompositeDisposable(), AndroidSchedulers.mainThread()).present();
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
  }

  @Nullable @Override
  public android.view.View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.frament_maintenance, container, false);
  }

  @Override public void onDestroy() {
    super.onDestroy();
  }

  @Override public void showNoLoginView() {
    progressbar.setVisibility(View.GONE);
    maintenanceView.setVisibility(View.VISIBLE);
    title.setText("We're working!");
    message_first.setText(
        "Due to some security concerns, Aptoide Uploader is temporarily unavailable. Our top one priority is our user security and that's why we've temporarily disabled the login function.");
    message_second.setText(
        "We're working hard for it to be back very soon and safer than ever, so just stay tuned!");
    blog.setText("Check our blog");
  }

  @Override public void showSocialLoginMaintenanceView() {
    progressbar.setVisibility(View.GONE);
    maintenanceView.setVisibility(View.VISIBLE);
    title.setText("We've got news!");
    message_first.setText(
        "Our users' security is our top one priority, and that's why we're developing a new login system using your email address. At the moment, you can only access your account using social media accounts.");
    message_second.setText("We're working hard for email login to come back soon, so stay tuned!");
    blog.setText("Check our blog");
    //    socialLogins.setVisibility(View.VISIBLE);
  }
}
