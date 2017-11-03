package com.aptoide.uploader

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
            on { getLoginEvent() } doReturn (AccountView.CredentialsViewModel("marcelo@aptoide.com", "aptoide1234")).toSingle().toObservable()
        }

        val navigator = mock<AccountNavigator> {}

        val accountManager = AptoideAccountManager();

        val accountPresenter = AccountPresenter(view, accountManager, navigator, CompositeDisposable())

        it("should navigate to apps view when user taps login button with valid e-mail and password") {
            accountPresenter.present()
            verify(navigator).navigateToAppsView()
        }
    }
})