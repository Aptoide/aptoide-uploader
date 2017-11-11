package com.aptoide.uploader

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

/**
 * Created by pedroribeiro on 10/11/17.
 */
@RunWith(JUnitPlatform::class)
class InstalledAppsPresenterTest : Spek({
    describe("a installed apps presenter") {
        it("should display installed apps when view is created") {
            val view = mock<InstalledAppsView> {}
            val packageProvider = mock<PackageProvider> {}
            val appsManager = AppsManager(packageProvider)
            val installedAppsPresenter = InstalledAppsPresenter(view, appsManager, CompositeDisposable())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val appList = mutableListOf<App>()

            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(packageProvider.installedApps).doReturn(appList.toSingle().toObservable())

            installedAppsPresenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            verify(view).showApps(appList)
        }
    }
})