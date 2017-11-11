package com.aptoide.uploader

import com.nhaarman.mockito_kotlin.*
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
import java.io.IOException

@RunWith(JUnitPlatform::class)
class AccountPresenterTest : Spek({
    describe("an account presenter") {

        it("should navigate to apps view when user taps login button with valid e-mail and password") {
            val view = mock<AccountView> {}
            val navigator = mock<AccountNavigator> {}
            val serviceV3 = mock<RetrofitAccountService.ServiceV3> {}
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>() {}
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3, serviceV7, AccountResponseMapper()))
            val accountPresenter = AccountPresenter(view, accountManager, navigator, CompositeDisposable(), Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val loginEvent = PublishSubject.create<AccountView.CredentialsViewModel>()
            val username = "marcelo@aptoide.com"

            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(view.loginEvent).doReturn(loginEvent)
            whenever(serviceV3.oauth2Authentication(any()))
                    .doReturn(OAuth("abc", "def", null, null).toSingle().toObservable())
            whenever(serviceV7.getUserInfo(any())).doReturn(AccountResponse(AccountResponse
                    .Nodes(AccountResponse.GetUserMeta(AccountResponse.GetUserMeta
                            .Data(AccountResponse.Store("marcelo", "http://aptoide.com/avatar", 1)))), ResponseV7.Info(ResponseV7.Info.Status.OK), null)
                    .toSingle().toObservable())

            accountPresenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            loginEvent.onNext(AccountView
                    .CredentialsViewModel(username, "aptoide1234"))
            verify(view).showLoading(username)
            verify(view).hideLoading()
            verify(navigator).navigateToAppsView()
        }

        it("should show error when user taps login button without internet") {
            val view = mock<AccountView> {}
            val navigator = mock<AccountNavigator> {}
            val serviceV3 = mock<RetrofitAccountService.ServiceV3> {}
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>() {}
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3, serviceV7, AccountResponseMapper()))
            val accountPresenter = AccountPresenter(view, accountManager, navigator, CompositeDisposable(), Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val loginEvent = PublishSubject.create<AccountView.CredentialsViewModel>()
            val username = "marcelo@aptoide.com"

            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(view.loginEvent).doReturn(loginEvent)
            whenever(serviceV3.oauth2Authentication(any())).doReturn(Observable.error<OAuth>(IOException()))
            whenever(serviceV7.getUserInfo(any())).doReturn(Observable.error<AccountResponse>(IOException()))

            accountPresenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            loginEvent.onNext(AccountView.CredentialsViewModel(username, "aptoide1234"))
            verify(view).showLoading(username)
            verify(view).hideLoading()
            verify(view).showNetworkError()
        }

        it("should show error when user taps login button with incorrect credentials") {
            val view = mock<AccountView> {}
            val navigator = mock<AccountNavigator> {}
            val serviceV3 = mock<RetrofitAccountService.ServiceV3> {}
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>() {}
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3, serviceV7, AccountResponseMapper()))
            val accountPresenter = AccountPresenter(view, accountManager, navigator, CompositeDisposable(), Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val loginEvent = PublishSubject.create<AccountView.CredentialsViewModel>()
            val username = "marcelo@aptoide.com"

            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(view.loginEvent).doReturn(loginEvent)
            whenever(serviceV3.oauth2Authentication(any()))
                    .doReturn(OAuth(null, null, "Invalid Credentials", "AUTH-1").toSingle().toObservable())

            accountPresenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            loginEvent.onNext(AccountView.CredentialsViewModel(username, "aptoide1234"))
            verify(view).showLoading(username)
            verify(view).hideLoading()
            verify(view).showError()
        }
    }
})