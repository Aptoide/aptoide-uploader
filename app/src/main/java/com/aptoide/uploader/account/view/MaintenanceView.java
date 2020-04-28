package com.aptoide.uploader.account.view;

import com.aptoide.uploader.view.View;
import io.reactivex.Observable;

interface MaintenanceView extends View {

  void showNoLoginView();

  void showSocialLoginMaintenanceView();

  Observable<Integer> clickOnBlog();

  void hideProgressBar();
}
