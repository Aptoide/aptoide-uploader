package com.aptoide.uploader.apps.view

import com.aptoide.uploader.apps.App
import com.aptoide.uploader.apps.AppsManager
import com.aptoide.uploader.apps.PackageProvider
import com.aptoide.uploader.view.View
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.toSingle
import io.reactivex.subjects.PublishSubject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
class MyAppsPresenterTest : Spek({
    describe("a my apps presenter") {
        it("should display installed apps when view is created") {
            val view = mock<MyAppsView> {}
            val packageProvider = mock<PackageProvider> {}
            val appsManager = AppsManager(packageProvider)
            val installedAppsPresenter = MyAppsPresenter(view, appsManager, CompositeDisposable())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val appList = mutableListOf<App>(App("https://myicon.com/facebook", "Facebook"),
                    App("https://myicon.com/aptoide", "Aptoide"))

            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(packageProvider.installedApps).doReturn(appList.toSingle())

            installedAppsPresenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            verify(view).showApps(appList)
        }
    }
})