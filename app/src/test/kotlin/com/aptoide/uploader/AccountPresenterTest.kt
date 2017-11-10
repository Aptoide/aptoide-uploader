package com.aptoide.uploader

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.toSingle
import io.reactivex.schedulers.Schedulers
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
class AccountPresenterTest : Spek({
    describe("an account presenter") {

        val view = mock<AccountView> {
            on { getLifecycleEvent() } doReturn View.LifecycleEvent.CREATE.toSingle().toObservable()
            on { getLoginEvent() } doReturn (AccountView
                    .CredentialsViewModel("marcelo@aptoide.com", "aptoide1234"))
                    .toSingle().toObservable()
        }

        val navigator = mock<AccountNavigator> {}
        val oauth = OAuth("abc", "def", null, null)
        val serviceV3 = mock<RetrofitAccountService.ServiceV3> {
            on { oauth2Authentication(any()) } doReturn oauth.toSingle().toObservable()
        }

        val serviceV7 = mock<RetrofitAccountService.ServiceV7>() {
            on { getUserInfo(any()) } doReturn AccountResponse(AccountResponse
                    .Nodes(AccountResponse.GetUserMeta(AccountResponse.GetUserMeta
                            .Data(AccountResponse.Store("marcelo", "http://aptoide.com/avatar", 1)))), ResponseV7.Info(ResponseV7.Info.Status.OK), null)
                    .toSingle().toObservable()
        }

        val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3, serviceV7, AccountResponseMapper()))

        val accountPresenter = AccountPresenter(view, accountManager, navigator, CompositeDisposable(), Schedulers.trampoline())

        it("should navigate to apps view when user taps login button with valid e-mail and password") {
            accountPresenter.present()
            verify(navigator).navigateToAppsView()
        }
    }
})