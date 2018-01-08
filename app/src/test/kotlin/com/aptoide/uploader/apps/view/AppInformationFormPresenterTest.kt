package com.aptoide.uploader.apps.view

import com.aptoide.uploader.TestData
import com.aptoide.uploader.apps.InstalledApp
import com.aptoide.uploader.apps.RemoteAppInformationManager
import com.aptoide.uploader.apps.UserProposedAppInfo
import com.aptoide.uploader.view.View
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.Assert.fail
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith


@RunWith(JUnitPlatform::class)
class AppInformationFormPresenterTest : Spek({
    describe("a app upload form presenter") {

        it("should present an empty form and return all form data") {
            val mockAppInfo = UserProposedAppInfo(
                    TestData.APPINFO_NAME, TestData.PROPOSED_APP_RATING,
                    TestData.APPINFO_CATEGORY, TestData.APPINFO_LANGUAGE,
                    TestData.APPINFO_DESCRIPTION, TestData.APPINFO_PHONE_NR,
                    TestData.APPINFO_EMAIL, TestData.APPINFO_WEBSITE
            )

            val view = mock<AppInformationFormView> {}
            val remoteAppInfoManager = mock<RemoteAppInformationManager> {}
            val appInfoFormPresenter = AppInformationFormPresenter(view, remoteAppInfoManager, CompositeDisposable(), Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val submitEvent = PublishSubject.create<UserProposedAppInfo>()

            whenever(view.lifecycleEvent)
                    .doReturn(lifecycleEvent)
            whenever(view.submitEvent)
                    .doReturn(submitEvent)

            appInfoFormPresenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            submitEvent.onNext(mockAppInfo)

            verify(remoteAppInfoManager).uploadInfo(mockAppInfo)
        }

        it("should present a pre-filled form and return all form data") {
            val language = "PT-BR"
            val aptoide = InstalledApp("https://myicon.com/aptoide", "Aptoide", false, "cm.aptoide.pt", "/Files/aptoide.apk", 0)

            fail("to do")
        }
    }
})
