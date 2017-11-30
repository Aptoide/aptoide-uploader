package com.aptoide.uploader.apps.view

import com.aptoide.uploader.TestData
import com.aptoide.uploader.account.AccountPersistence
import com.aptoide.uploader.account.AccountService
import com.aptoide.uploader.account.AptoideAccount
import com.aptoide.uploader.account.AptoideAccountManager
import com.aptoide.uploader.apps.*
import com.aptoide.uploader.view.View
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
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
            val uploadManager = mock<UploadManager> {}
            val languageManager = mock<LanguageManager> {}
            val storeNameProvider = AccountStoreNameProvider(AptoideAccountManager(accountService, accountPersistence))
            val storeManager = StoreManager(packageProvider, storeNameProvider, uploadManager, languageManager)
            val installedAppsPresenter = MyStorePresenter(view, storeManager, CompositeDisposable(), Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val facebook = App("https://myicon.com/facebook", "Facebook", false, "cm.aptoide.pt")
            val aptoide = App("https://myicon.com/aptoide", "Aptoide", true, "cm.aptoide.pt")
            val appList = mutableListOf(facebook, aptoide)

            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(view.submitAppEvent()).doReturn(Observable.empty())
            whenever(languageManager.currentLanguageCode).doReturn("PT-BR".toSingle())
            whenever(packageProvider.installedApps).doReturn(appList.toSingle())
            whenever(accountPersistence.account).doReturn(AptoideAccount(true, true, TestData.STORE_NAME).toSingle().toObservable())

            installedAppsPresenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            verify(view).showStoreName(TestData.STORE_NAME)
            verify(view).showApps(mutableListOf(facebook))
        }

        it("should upload selected apps when submit button is clicked") {

            val view = mock<MyStoreView> {}
            val packageProvider = mock<PackageProvider> {}
            val accountService = mock<AccountService> {}
            val accountPersistence = mock<AccountPersistence> {}
            val uploadManager = mock<UploadManager> {}
            val languageManager = mock<LanguageManager> {}
            val storeNameProvider = AccountStoreNameProvider(AptoideAccountManager(accountService, accountPersistence))
            val storeManager = StoreManager(packageProvider, storeNameProvider, uploadManager, languageManager)
            val installedAppsPresenter = MyStorePresenter(view, storeManager, CompositeDisposable(), Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val submitAppEvent = PublishSubject.create<MutableList<App>>()
            val aptoide = App("https://myicon.com/aptoide", "Aptoide", true, "cm.aptoide.pt")
            val storeName = "Marcelo"

            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(view.submitAppEvent()).doReturn(submitAppEvent)
            whenever(languageManager.currentLanguageCode).doReturn("PT-BR".toSingle())
            whenever(packageProvider.installedApps).doReturn(mutableListOf(aptoide).toSingle())
            whenever(uploadManager.upload("Marcelo", "PT-BR", aptoide)).doReturn(Completable.complete())
            whenever(accountPersistence.account)
                    .doReturn(AptoideAccount(true, true, storeName).toSingle().toObservable())

            installedAppsPresenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            submitAppEvent.onNext(mutableListOf(aptoide))
            verify(uploadManager).upload("Marcelo", "PT-BR", aptoide)
        }
    }
})
