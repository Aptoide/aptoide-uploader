package com.aptoide.uploader.apps.view

import android.content.DialogInterface
import com.aptoide.uploader.TestData
import com.aptoide.uploader.account.AccountPersistence
import com.aptoide.uploader.account.AccountService
import com.aptoide.uploader.account.AptoideAccount
import com.aptoide.uploader.account.AptoideAccountManager
import com.aptoide.uploader.account.network.AccountResponseMapper
import com.aptoide.uploader.account.network.RetrofitAccountService
import com.aptoide.uploader.apps.*
import com.aptoide.uploader.security.SecurityAlgorithms
import com.aptoide.uploader.view.View
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.Observable
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
class MyStorePresenterTest : Spek({
    describe("a my apps presenter") {

        val storeName = "Marcelo"
        val language = "PT-BR"
        val facebook = InstalledApp("https://myicon.com/facebook", "Facebook", false, "cm.aptoide.pt", "/Files/facebook.apk", 0)
        val aptoide = InstalledApp("https://myicon.com/aptoide", "Aptoide", true, "cm.aptoide.pt", "/Files/aptoide.apk", 1)
        val aptoide2 = InstalledApp("https://myicon.com/aptoide", "Aptoide", false, "cm.aptoide.pt", "/Files/aptoide.apk", 1)

        it("should display store name and installed apps when view is created") {
            val view = mock<MyStoreView> {}
            val navigator = mock<MyStoreNavigator>()
            val packageProvider = mock<InstalledAppsProvider> {}
            val accountService = mock<AccountService> {}
            val serviceV2 = mock<RetrofitAccountService.ServiceV2>()
            val serviceV3 = mock<RetrofitAccountService.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV2, serviceV3,
                    serviceV7, SecurityAlgorithms(), AccountResponseMapper()), accountPersistence)
            val uploadManager = mock<UploadManager> {}
            val languageManager = mock<LanguageManager> {}
            val storeNameProvider = AccountStoreNameProvider(AptoideAccountManager(accountService, accountPersistence))
            val storeManager = StoreManager(packageProvider, storeNameProvider, uploadManager, languageManager, accountManager)
            val installedAppsPresenter = MyStorePresenter(view, storeManager, CompositeDisposable(), navigator, Schedulers.trampoline())
            val appList = mutableListOf(facebook, aptoide)

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()

            whenever(view.lifecycleEvent)
                    .doReturn(lifecycleEvent)
            whenever(view.submitAppEvent())
                    .doReturn(Observable.empty())
            whenever(languageManager.currentLanguageCode)
                    .doReturn(language.toSingle())
            whenever(packageProvider.installedApps)
                    .doReturn(appList.toSingle())
            whenever(accountPersistence.account)
                    .doReturn(AptoideAccount(true, true, TestData.STORE_NAME).toSingle().toObservable())

            installedAppsPresenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            verify(view).showStoreName(TestData.STORE_NAME)
            verify(view).showApps(mutableListOf(facebook))
        }

        it("should upload selected apps when submit button is clicked") {

            val view = mock<MyStoreView> {}
            val navigator = mock<MyStoreNavigator>()
            val packageProvider = mock<InstalledAppsProvider> {}
            val accountService = mock<AccountService> {}
            val serviceV2 = mock<RetrofitAccountService.ServiceV2>()
            val serviceV3 = mock<RetrofitAccountService.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV2, serviceV3,
                    serviceV7, SecurityAlgorithms(), AccountResponseMapper()), accountPersistence)
            val uploadManager = mock<UploadManager> {}
            val languageManager = mock<LanguageManager> {}
            val storeNameProvider = AccountStoreNameProvider(AptoideAccountManager(accountService, accountPersistence))
            val storeManager = StoreManager(packageProvider, storeNameProvider, uploadManager, languageManager, accountManager)
            val installedAppsPresenter = MyStorePresenter(view, storeManager, CompositeDisposable(), navigator, Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val submitAppEvent = PublishSubject.create<MutableList<InstalledApp>>()

            whenever(view.lifecycleEvent)
                    .doReturn(lifecycleEvent)
            whenever(view.submitAppEvent())
                    .doReturn(submitAppEvent)
            whenever(languageManager.currentLanguageCode)
                    .doReturn(language.toSingle())
            whenever(packageProvider.installedApps)
                    .doReturn(mutableListOf(aptoide).toSingle())
            whenever(uploadManager.upload(storeName, language, aptoide))
                    .doReturn(Completable.complete())
            whenever(accountPersistence.account)
                    .doReturn(AptoideAccount(true, true, storeName).toSingle().toObservable())

            installedAppsPresenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            submitAppEvent.onNext(mutableListOf(aptoide))
            verify(uploadManager).upload(storeName, language, aptoide)
        }

        it("should sort list of apps by installed date") {

            val view = mock<MyStoreView> {}
            val navigator = mock<MyStoreNavigator>()
            val packageProvider = mock<InstalledAppsProvider> {}
            val accountService = mock<AccountService> {}
            val serviceV2 = mock<RetrofitAccountService.ServiceV2>()
            val serviceV3 = mock<RetrofitAccountService.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV2, serviceV3,
                    serviceV7, SecurityAlgorithms(), AccountResponseMapper()), accountPersistence)
            val uploadManager = mock<UploadManager> {}
            val languageManager = mock<LanguageManager> {}
            val storeNameProvider = AccountStoreNameProvider(AptoideAccountManager(accountService, accountPersistence))
            val storeManager = StoreManager(packageProvider, storeNameProvider, uploadManager, languageManager, accountManager)
            val installedAppsPresenter = MyStorePresenter(view, storeManager, CompositeDisposable(), navigator, Schedulers.trampoline())

            val unSortedAppList = listOf(aptoide, aptoide2, facebook)
            val sortedAppList = listOf(facebook, aptoide2)

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val orderByDateEvent = PublishSubject.create<SortingOrder>()

            whenever(view.lifecycleEvent)
                    .doReturn(lifecycleEvent)
            whenever(view.submitAppEvent())
                    .doReturn(Observable.empty())
            whenever(languageManager.currentLanguageCode)
                    .doReturn(language.toSingle())
            whenever(packageProvider.installedApps).doReturn(unSortedAppList.toSingle())
            whenever(accountPersistence.account)
                    .doReturn(AptoideAccount(true, true, TestData.STORE_NAME).toSingle().toObservable())

            whenever(view.orderByEvent())
                    .doReturn(orderByDateEvent)

            installedAppsPresenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            reset(view)
            orderByDateEvent.onNext(SortingOrder.DATE)

            verify(view).showApps(sortedAppList)

        }

        it("should sort list of apps by name") {

            val view = mock<MyStoreView> {}
            val navigator = mock<MyStoreNavigator>()
            val packageProvider = mock<InstalledAppsProvider> {}
            val accountService = mock<AccountService> {}
            val serviceV2 = mock<RetrofitAccountService.ServiceV2>()
            val serviceV3 = mock<RetrofitAccountService.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV2, serviceV3,
                    serviceV7, SecurityAlgorithms(), AccountResponseMapper()), accountPersistence)
            val uploadManager = mock<UploadManager> {}
            val languageManager = mock<LanguageManager> {}
            val storeNameProvider = AccountStoreNameProvider(AptoideAccountManager(accountService, accountPersistence))
            val storeManager = StoreManager(packageProvider, storeNameProvider, uploadManager, languageManager, accountManager)
            val installedAppsPresenter = MyStorePresenter(view, storeManager, CompositeDisposable(), navigator, Schedulers.trampoline())

            val unSortedAppList = listOf(facebook, aptoide, aptoide2)
            val sortedAppList = listOf(aptoide2, facebook)

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val orderByDateEvent = PublishSubject.create<SortingOrder>()

            whenever(view.lifecycleEvent)
                    .doReturn(lifecycleEvent)
            whenever(view.submitAppEvent())
                    .doReturn(Observable.empty())
            whenever(languageManager.currentLanguageCode)
                    .doReturn(language.toSingle())
            whenever(packageProvider.installedApps).doReturn(unSortedAppList.toSingle())
            whenever(accountPersistence.account)
                    .doReturn(AptoideAccount(true, true, TestData.STORE_NAME).toSingle().toObservable())

            whenever(view.orderByEvent())
                    .doReturn(orderByDateEvent)

            installedAppsPresenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            reset(view)
            orderByDateEvent.onNext(SortingOrder.NAME)

            verify(view).showApps(sortedAppList)
        }

        it("should navigate to login view after signout confirmation is given") {
            val view = mock<MyStoreView> {}
            val dialogInterface = mock<DialogInterface> {}
            val navigator = mock<MyStoreNavigator>()
            val storeManager = mock<StoreManager> {}
            val installedAppsPresenter = MyStorePresenter(view, storeManager, CompositeDisposable(), navigator, Schedulers.trampoline())

            val click = PublishSubject.create<DialogInterface>()
            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()

            whenever(view.lifecycleEvent)
                    .doReturn(lifecycleEvent)
            whenever(view.positiveClick()).doReturn(click)
            whenever(storeManager.logout()).doReturn(Completable.complete())

            installedAppsPresenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            click.onNext(dialogInterface)
            verify(storeManager).logout()
            verify(navigator).navigateToLoginView()
        }

        it("should navigate to app information form view if it is needed before upload") {
            fail("To Do")
        }

    }
})
