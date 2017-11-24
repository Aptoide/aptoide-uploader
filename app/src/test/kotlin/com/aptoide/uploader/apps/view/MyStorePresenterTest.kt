package com.aptoide.uploader.apps.view

import com.aptoide.uploader.account.AccountPersistence
import com.aptoide.uploader.account.AccountService
import com.aptoide.uploader.account.AptoideAccount
import com.aptoide.uploader.account.AptoideAccountManager
import com.aptoide.uploader.apps.AccountStoreNameProvider
import com.aptoide.uploader.apps.App
import com.aptoide.uploader.apps.PackageProvider
import com.aptoide.uploader.apps.StoreManager
import com.aptoide.uploader.view.View
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.toSingle
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
class MyStorePresenterTest : Spek({
    describe("a my apps presenter") {
        it("should display store name and installed apps when view is created") {
            val view = mock<MyStoreView> {}
            val packageProvider = mock<PackageProvider> {}
            val accountService = mock<AccountService> {}
            val accountPersistence = mock<AccountPersistence> {}
            val storeNameProvider = AccountStoreNameProvider(AptoideAccountManager(accountService, accountPersistence))
            val appsManager = StoreManager(packageProvider, storeNameProvider)
            val installedAppsPresenter = MyStorePresenter(view, appsManager, CompositeDisposable(), Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val facebook = App("https://myicon.com/facebook", "Facebook", false)
            val aptoide = App("https://myicon.com/aptoide", "Aptoide", true)
            val appList = mutableListOf(facebook, aptoide)
            val storeName = "marcelo"

            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(packageProvider.installedApps).doReturn(appList.toSingle())
            whenever(accountPersistence.account).doReturn(AptoideAccount(true, true, storeName).toSingle().toObservable())

            installedAppsPresenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            verify(view).showStoreName("marcelo")
            verify(view).showApps(mutableListOf(facebook))
        }
    }
})