package com.aptoide.uploader.apps.view

import com.aptoide.uploader.TestData
import com.aptoide.uploader.apps.*
import com.aptoide.uploader.view.View
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.toSingle
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
        val language = "PT-BR"
        val aptoide = InstalledApp("https://myicon.com/aptoide", "Aptoide", true, "cm.aptoide.pt", "/Files/aptoide.apk")

        it("should present an empty form and return all form data") {
            val mockAppInfo = UserProposedAppInfo(
                    TestData.APPINFO_NAME, TestData.PROPOSED_APP_RATING,
                    TestData.APPINFO_CATEGORY, TestData.APPINFO_LANGUAGE,
                    TestData.APPINFO_DESCRIPTION, TestData.APPINFO_PHONE_NR,
                    TestData.APPINFO_EMAIL, TestData.APPINFO_WEBSITE
            )

            val view = mock<AppInformationFormView> {}
            val languageManager = mock<LanguageManager> {}
            val appInfoService = mock<RemoteAppInformationService>()
            val remoteAppInfoManager = RemoteAppInformationManager(appInfoService, languageManager)
            val appInfoFormPresenter = AppInformationFormPresenter(view, remoteAppInfoManager, CompositeDisposable(), Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val submitEvent = PublishSubject.create<UserProposedAppInfo>()

            whenever(view.lifecycleEvent)
                    .doReturn(lifecycleEvent)
            whenever(view.submitEvent)
                    .doReturn(submitEvent)
            whenever(remoteAppInfoManager.uploadInfo(any())).doReturn(Completable.complete())

            appInfoFormPresenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            submitEvent.onNext(mockAppInfo)

            verify(view).showProposedAppInfo(RemoteProposedAppInfo())
            verify(remoteAppInfoManager).uploadInfo(mockAppInfo)
        }

        it("should present a pre-filled form and return all form data") {
            fail("to do")
        }
    }
})
