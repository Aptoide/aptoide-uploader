package com.aptoide.uploader.apps.view;

import com.aptoide.uploader.apps.RemoteProposedAppInfo;
import com.aptoide.uploader.apps.UserProposedAppInfo;
import com.aptoide.uploader.view.View;

import org.jetbrains.annotations.NotNull;


import io.reactivex.Observable;

/**
 * Created by franciscoaleixo on 15/12/2017.
 */

public interface AppInformationFormView extends View {

    Observable<UserProposedAppInfo> getSubmitEvent();

    void showProposedAppInfo(@NotNull RemoteProposedAppInfo remoteProposedAppInfo);
}
