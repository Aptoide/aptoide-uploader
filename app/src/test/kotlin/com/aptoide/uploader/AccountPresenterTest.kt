package com.aptoide.uploader

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.toSingle
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

@RunWith(JUnitPlatform::class)
class AccountPresenterTest : Spek({
    describe("a account presenter") {

        val view = mock<AccountView> {
            on { getLifecycle() } doReturn View.LifecycleEvent.CREATE.toSingle().toObservable()
            on { getLoginEvent() } doReturn (AccountView.CredentialsViewModel("marcelo@aptoide.com", "aptoide1234")).toSingle().toObservable()
        }

        val navigator = mock<AccountNavigator> {}

        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("{\n" +
                "    \"access_token\": \"8a3662580a0313cf94e8deabc834c2ae317e5d14\",\n" +
                "    \"expires_in\": 3600,\n" +
                "    \"token_type\": \"Bearer\",\n" +
                "    \"scope\": null,\n" +
                "    \"refresh_token\": \"35c51f4ec12b3f2047b0fff34d2b184474af053c\"\n" +
                "}"))
        server.start()
        val okHttpClient = OkHttpClient()

        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.trampoline()))
                .addConverterFactory(MoshiConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(server.url("/").toString())
                .build()

        val accountManager = AptoideAccountManager(RetrofitAccountService(retrofit.create(RetrofitAccountService.Service::class.java)))

        val accountPresenter = AccountPresenter(view, accountManager, navigator, CompositeDisposable())

        it("should navigate to apps view when user taps login button with valid e-mail and password") {
            accountPresenter.present()
            verify(navigator).navigateToAppsView()
        }
    }
})