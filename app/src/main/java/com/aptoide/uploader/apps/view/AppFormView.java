package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.Metadata;
import com.aptoide.uploader.view.View;
import io.reactivex.Observable;
import java.util.List;

public interface AppFormView extends View {

  void setAppName();

  void showMandatoryFieldError();

  void showGeneralError();

  void showForm();

  void showCategories(List<String> categoriesList);

  Metadata getMetadata();

  Observable<Metadata> submitAppEvent();
}
