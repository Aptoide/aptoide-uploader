package com.aptoide.uploader

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.toSingle
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
class AccountPresenterTest : Spek({
    describe("a account presenter") {

        val view = mock<AccountView> {
            on { getLifecycle() } doReturn View.LifecycleEvent.CREATE.toSingle().toObservable()
            on { getLoginEvent() } doReturn (AccountView
                    .CredentialsViewModel("marcelo@aptoide.com", "aptoide1234"))
                    .toSingle().toObservable()
        }

        val navigator = mock<AccountNavigator> {}
        val oauth = OAuth("abc", "def", null, null)
        val retrofitService = mock<RetrofitAccountService.Service> {
            on { oauth2Authentication(any()) } doReturn oauth.toSingle().toObservable()
            on { getUserInfo(any()) } doReturn AccountResponse(AccountResponse
                    .Nodes(AccountResponse.GetUserMeta(AccountResponse.GetUserMeta
                            .Data(AccountResponse.Store("marcelo", "http://aptoide.com/avatar", 1)))), ResponseV7.Info(ResponseV7.Info.Status.OK), null)
                    .toSingle().toObservable()
        }

        val accountManager = AptoideAccountManager(RetrofitAccountService(retrofitService, AccountResponseMapper()))

        val accountPresenter = AccountPresenter(view, accountManager, navigator, CompositeDisposable())

        it("should navigate to apps view when user taps login button with valid e-mail and password") {
            accountPresenter.present()
            verify(navigator).navigateToAppsView()
        }
    }
})