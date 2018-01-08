package com.aptoide.uploader.account.view

import com.aptoide.uploader.TestData
import com.aptoide.uploader.account.AccountPersistence
import com.aptoide.uploader.account.AptoideAccount
import com.aptoide.uploader.account.AptoideAccountManager
import com.aptoide.uploader.account.CredentialsValidator
import com.aptoide.uploader.account.network.*
import com.aptoide.uploader.security.AptoideAccessTokenProvider
import com.aptoide.uploader.security.SecurityAlgorithms
import com.aptoide.uploader.security.SharedPreferencesAuthenticationPersistence
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
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import retrofit2.Response
import java.io.IOException

@RunWith(JUnitPlatform::class)
class LoginPresenterTest : Spek({

    describe("an account presenter") {

        val credentialsViewModel = LoginView.CredentialsViewModel(
                TestData.USER_NAME, TestData.USER_PASSWORD
        )

        it("should navigate to my apps view when user taps login button with correct credentials") {
            val view = mock<LoginView>()
            val navigator = mock<LoginNavigator>()
            val serviceV3Account = mock<RetrofitAccountService.ServiceV3>()
            val serviceV3 = mock<AptoideAccessTokenProvider.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val authenticationPersistance = mock<SharedPreferencesAuthenticationPersistence>()
            val credentialsValidator = mock<CredentialsValidator>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3Account, serviceV7,
                    SecurityAlgorithms(), AccountResponseMapper(), AptoideAccessTokenProvider(authenticationPersistance, serviceV3)), accountPersistence, credentialsValidator)
            val presenter = LoginPresenter(view, accountManager, navigator, CompositeDisposable(), Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val accounts = PublishSubject.create<AptoideAccount>()
            val loginEvent = PublishSubject.create<LoginView.CredentialsViewModel>()

            val loginResponse = Response.success(OAuth("abc", "def",
                    null, null))
            val accountResponse = Response.success(AccountResponse(AccountResponse
                    .Nodes(AccountResponse.GetUserMeta(AccountResponse.GetUserMeta
                            .Data(AccountResponse.Store(TestData.STORE_NAME,
                                    "http://aptoide.com/avatar", 1)))),
                    ResponseV7.Info(ResponseV7.Info.Status.OK), null))


            whenever(accountPersistence.account).doReturn(accounts)
            whenever(accountPersistence.save(any())).doReturn(Completable.fromAction({
                accounts.onNext(AptoideAccount(true, true, TestData.STORE_NAME))
            }))
            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(view.loginEvent).doReturn(loginEvent)
            whenever(serviceV3.oauth2Authentication(any()))
                    .doReturn(loginResponse.toSingle().toObservable())
            whenever(serviceV7.getUserInfo(any())).doReturn(accountResponse
                    .toSingle().toObservable())

            presenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            loginEvent.onNext(credentialsViewModel)
            verify(view).showLoading(TestData.USER_NAME)
            verify(view).hideLoading()
            verify(navigator).navigateToMyAppsView()
        }

        it("should show error when user taps login button without internet") {
            val view = mock<LoginView>()
            val navigator = mock<LoginNavigator>()
            val serviceV3Account = mock<RetrofitAccountService.ServiceV3>()
            val serviceV3 = mock<AptoideAccessTokenProvider.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val authenticationPersistance = mock<SharedPreferencesAuthenticationPersistence>()
            val credentialsValidator = mock<CredentialsValidator>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3Account, serviceV7,
                    SecurityAlgorithms(), AccountResponseMapper(), AptoideAccessTokenProvider(authenticationPersistance, serviceV3)), accountPersistence, credentialsValidator)
            val presenter = LoginPresenter(view, accountManager, navigator, CompositeDisposable(), Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val loginEvent = PublishSubject.create<LoginView.CredentialsViewModel>()

            whenever(accountPersistence.account).doReturn(PublishSubject.create<AptoideAccount>())
            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(view.loginEvent).doReturn(loginEvent)
            whenever(serviceV3.oauth2Authentication(any())).doReturn(Observable.error<Response<OAuth>>(IOException()))
            whenever(serviceV7.getUserInfo(any())).doReturn(Observable.error<Response<AccountResponse>>(IOException()))

            presenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            loginEvent.onNext(credentialsViewModel)
            verify(view).showLoading(TestData.USER_NAME)
            verify(view).hideLoading()
            verify(view).showNetworkError()
        }

        it("should show error when user taps login button with incorrect credentials") {
            val view = mock<LoginView>()
            val navigator = mock<LoginNavigator>()
            val serviceV3Account = mock<RetrofitAccountService.ServiceV3>()
            val serviceV3 = mock<AptoideAccessTokenProvider.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val authenticationPersistance = mock<SharedPreferencesAuthenticationPersistence>()
            val credentialsValidator = mock<CredentialsValidator>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3Account, serviceV7,
                    SecurityAlgorithms(), AccountResponseMapper(), AptoideAccessTokenProvider(authenticationPersistance, serviceV3)), accountPersistence, credentialsValidator)
            val presenter = LoginPresenter(view, accountManager, navigator, CompositeDisposable(), Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val loginEvent = PublishSubject.create<LoginView.CredentialsViewModel>()
            val loginResponse = Response.success(OAuth(null, null,
                    "Invalid Credentials", "AUTH-1"))

            whenever(accountPersistence.account).doReturn(PublishSubject.create<AptoideAccount>())
            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(view.loginEvent).doReturn(loginEvent)
            whenever(serviceV3.oauth2Authentication(any()))
                    .doReturn(loginResponse.toSingle().toObservable())

            presenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            loginEvent.onNext(credentialsViewModel)
            verify(view).showLoading(TestData.USER_NAME)
            verify(view).hideLoading()
            verify(view).showCrendentialsError()
        }

        it("should navigate to create store view when user taps login button with correct credentials " +
                "but account has no store associated") {
            val view = mock<LoginView>()
            val navigator = mock<LoginNavigator>()
            val serviceV3Account = mock<RetrofitAccountService.ServiceV3>()
            val serviceV3 = mock<AptoideAccessTokenProvider.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val authenticationPersistance = mock<SharedPreferencesAuthenticationPersistence>()
            val credentialsValidator = mock<CredentialsValidator>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3Account, serviceV7,
                    SecurityAlgorithms(), AccountResponseMapper(), AptoideAccessTokenProvider(authenticationPersistance, serviceV3)), accountPersistence, credentialsValidator)
            val presenter = LoginPresenter(view, accountManager, navigator, CompositeDisposable(), Schedulers.trampoline())

            val accounts = PublishSubject.create<AptoideAccount>()
            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val loginEvent = PublishSubject.create<LoginView.CredentialsViewModel>()

            val loginResponse = Response.success(OAuth("abc", "def",
                    null, null))
            val accountResponse = Response.success(AccountResponse(AccountResponse
                    .Nodes(AccountResponse.GetUserMeta(AccountResponse.GetUserMeta
                            .Data(null))),
                    ResponseV7.Info(ResponseV7.Info.Status.OK), null))

            whenever(accountPersistence.account).doReturn(accounts)
            whenever(accountPersistence.save(any())).doReturn(Completable.fromAction({
                accounts.onNext(AptoideAccount(false, true, TestData.STORE_NAME))
            }))
            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(view.loginEvent).doReturn(loginEvent)
            whenever(serviceV3.oauth2Authentication(any()))
                    .doReturn(loginResponse.toSingle().toObservable())
            whenever(serviceV7.getUserInfo(any())).doReturn(accountResponse
                    .toSingle().toObservable())

            presenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            loginEvent.onNext(credentialsViewModel)
            verify(view).showLoading(TestData.USER_NAME)
            verify(view).hideLoading()
            verify(navigator).navigateToCreateStoreView()
        }

        it("should automatically navigate to my apps when user is already logged in and has a store") {
            val view = mock<LoginView>()
            val navigator = mock<LoginNavigator>()
            val serviceV3Account = mock<RetrofitAccountService.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val credentialsValidator = mock<CredentialsValidator>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3Account, serviceV7,
                    SecurityAlgorithms(), AccountResponseMapper(), null), accountPersistence, credentialsValidator)
            val presenter = LoginPresenter(view, accountManager, navigator, CompositeDisposable(), Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val loginEvent = PublishSubject.create<LoginView.CredentialsViewModel>()

            whenever(view.loginEvent).doReturn(loginEvent)
            whenever(accountPersistence.account).doReturn(AptoideAccount(true, true, TestData.STORE_NAME)
                    .toSingle().toObservable())
            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)

            presenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            verify(navigator).navigateToMyAppsView()
        }

        it("should automatically navigate to create store view when user is already logged in and does not have a store") {
            val view = mock<LoginView>()
            val navigator = mock<LoginNavigator>()
            val serviceV3Account = mock<RetrofitAccountService.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val credentialsValidator = mock<CredentialsValidator>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3Account, serviceV7,
                    SecurityAlgorithms(), AccountResponseMapper(), null), accountPersistence, credentialsValidator)
            val presenter = LoginPresenter(view, accountManager, navigator, CompositeDisposable(), Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val loginEvent = PublishSubject.create<LoginView.CredentialsViewModel>()

            whenever(view.loginEvent).doReturn(loginEvent)
            whenever(accountPersistence.account).doReturn(AptoideAccount(false, true, TestData.STORE_NAME)
                    .toSingle().toObservable())
            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)

            presenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            verify(navigator).navigateToCreateStoreView()
        }

        it("should navigate to create account view") {
            val navigator = mock<LoginNavigator>()
            val serviceV3Account = mock<RetrofitAccountService.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val credentialsValidator = mock<CredentialsValidator>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3Account, serviceV7,
                    SecurityAlgorithms(), AccountResponseMapper(), null), accountPersistence, credentialsValidator)
            val accounts = PublishSubject.create<AptoideAccount>()
            val view = mock<LoginView>()
            val presenter = LoginPresenter(view, accountManager, navigator, CompositeDisposable(), Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val clickGoToCreateAccountViewEvent = PublishSubject.create<LoginView.CredentialsViewModel>()

            whenever(accountPersistence.account).doReturn(accounts)
            whenever(accountPersistence.save(any())).doReturn(Completable.fromAction({
                accounts.onNext(AptoideAccount(true, true, TestData.STORE_NAME))
            }))

            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(view.openCreateAccountView).doReturn(clickGoToCreateAccountViewEvent)

            presenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            clickGoToCreateAccountViewEvent.onNext(credentialsViewModel)

            verify(navigator).navigateToCreateAccountView()
        }

    }
})
