package com.aptoide.uploader.account.view

import com.aptoide.uploader.TestData
import com.aptoide.uploader.account.AccountPersistence
import com.aptoide.uploader.account.AccountValidationException
import com.aptoide.uploader.account.AptoideAccount
import com.aptoide.uploader.account.AptoideAccountManager
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
class CreateAccountPresenterTest : Spek({
    describe("a create account presenter") {

        val publicCreateAccountModel = CreateAccountView.ViewModel(
                TestData.USER_NAME, TestData.USER_PASSWORD, TestData.STORE_NAME
        )

        val privateCreateAccountModel = CreateAccountView.ViewModel(
                TestData.USER_NAME, TestData.USER_PASSWORD, TestData.STORE_NAME,
                TestData.STORE_USER, TestData.STORE_PASSWORD
        )

        it("should navigate to my apps when the user creates a new account in aptoide inserting valid data: email, password, store name and store privacy") {
            val accountErrorMapper = mock<AccountErrorMapper>()
            val navigator = mock<CreateAccountNavigator>()
            val serviceV3 = mock<AptoideAccessTokenProvider.ServiceV3>()
            val serviceV3Account = mock<RetrofitAccountService.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val authenticationPersistance = mock<SharedPreferencesAuthenticationPersistence>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3Account, serviceV7,
                    SecurityAlgorithms(), AccountResponseMapper(), AptoideAccessTokenProvider(authenticationPersistance, serviceV3)), accountPersistence)

            val view = mock<CreateAccountView>()
            val presenter = CreateAccountPresenter(view, accountManager, navigator, CompositeDisposable(), accountErrorMapper, Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val accounts = PublishSubject.create<AptoideAccount>()
            val createAccountEvent = PublishSubject.create<CreateAccountView.ViewModel>()
            val createAccountResponse = TestData.SUCCESS_RESPONSE
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
            whenever(view.createAccountEvent).doReturn(createAccountEvent)

            whenever(serviceV3Account.createAccount(any()))
                    .doReturn(createAccountResponse.toSingle().toObservable())
            whenever(serviceV7.getUserInfo(any())).doReturn(accountResponse
                    .toSingle().toObservable())

            presenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            createAccountEvent.onNext(publicCreateAccountModel)

            verify(view).showLoading()
            verify(view).hideLoading()
            verify(navigator).navigateToMyAppsView()
        }

        it("should navigate to my apps when the user creates a new account in aptoide inserting valid data: email, password, store name, store privacy, store user and store pass") {
            val accountErrorMapper = mock<AccountErrorMapper>()
            val navigator = mock<CreateAccountNavigator>()
            val serviceV3Account = mock<RetrofitAccountService.ServiceV3>()
            val serviceV3 = mock<AptoideAccessTokenProvider.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val authenticationPersistance = mock<SharedPreferencesAuthenticationPersistence>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3Account, serviceV7,
                    SecurityAlgorithms(), AccountResponseMapper(), AptoideAccessTokenProvider(authenticationPersistance, serviceV3)), accountPersistence)

            val view = mock<CreateAccountView>()
            val presenter = CreateAccountPresenter(view, accountManager, navigator, CompositeDisposable(), accountErrorMapper, Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val accounts = PublishSubject.create<AptoideAccount>()
            val createAccountEvent = PublishSubject.create<CreateAccountView.ViewModel>()
            val createAccountResponse = TestData.SUCCESS_RESPONSE
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
            whenever(view.createAccountEvent).doReturn(createAccountEvent)

            whenever(serviceV3Account.createAccount(any()))
                    .doReturn(createAccountResponse.toSingle().toObservable())
            whenever(serviceV7.getUserInfo(any())).doReturn(accountResponse
                    .toSingle().toObservable())

            presenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            createAccountEvent.onNext(privateCreateAccountModel)

            verify(view).showLoading()
            verify(view).hideLoading()
            verify(navigator).navigateToMyAppsView()
        }

        it("should show an error saying that this user already exists") {
            val accountErrorMapper = mock<AccountErrorMapper>()
            val navigator = mock<CreateAccountNavigator>()
            val serviceV3Account = mock<RetrofitAccountService.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3Account, serviceV7,
                    SecurityAlgorithms(), AccountResponseMapper(), null), accountPersistence)

            val view = mock<CreateAccountView>()
            val presenter = CreateAccountPresenter(view, accountManager, navigator, CompositeDisposable(), accountErrorMapper, Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val accounts = PublishSubject.create<AptoideAccount>()
            val createAccountEvent = PublishSubject.create<CreateAccountView.ViewModel>()
            val accountResponse = Response.success(OAuth(null, null,
                    "This user already has a valid repo.", "REPO-7"))

            whenever(accountPersistence.account).doReturn(accounts)
            whenever(accountPersistence.save(any())).doReturn(Completable.fromAction({
                accounts.onNext(AptoideAccount(true, true, TestData.STORE_NAME))
            }))
            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(view.createAccountEvent).doReturn(createAccountEvent)

            whenever(serviceV3Account.createAccount(any()))
                    .doReturn(accountResponse.toSingle().toObservable())

            presenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            createAccountEvent.onNext(publicCreateAccountModel)

            verify(view).showLoading()
            verify(view).hideLoading()
            verify(view).showErrorUserAlreadyExists()
        }

        it("should show error when user taps create account button without internet") {
            val accountErrorMapper = mock<AccountErrorMapper>()
            val navigator = mock<CreateAccountNavigator>()
            val serviceV3Account = mock<RetrofitAccountService.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3Account, serviceV7,
                    SecurityAlgorithms(), AccountResponseMapper(), null), accountPersistence)

            val view = mock<CreateAccountView>()
            val presenter = CreateAccountPresenter(view, accountManager, navigator, CompositeDisposable(), accountErrorMapper, Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val accounts = PublishSubject.create<AptoideAccount>()
            val createAccountEvent = PublishSubject.create<CreateAccountView.ViewModel>()

            whenever(accountPersistence.account).doReturn(accounts)
            whenever(accountPersistence.save(any())).doReturn(Completable.fromAction({
                accounts.onNext(AptoideAccount(true, true, TestData.STORE_NAME))
            }))
            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(view.createAccountEvent).doReturn(createAccountEvent)

            whenever(serviceV3Account.createAccount(any()))
                    .doReturn(Observable.error<Response<OAuth>>(IOException()))
            whenever(serviceV7.getUserInfo(any())).doReturn(Observable.error<Response<AccountResponse>>(IOException()))

            presenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            createAccountEvent.onNext(privateCreateAccountModel)

            verify(view).showLoading()
            verify(view).hideLoading()
            verify(view).showNetworkError()
        }

        it("should show an error saying that this store already exists") {
            val accountErrorMapper = mock<AccountErrorMapper>()
            val navigator = mock<CreateAccountNavigator>()
            val serviceV3Account = mock<RetrofitAccountService.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3Account, serviceV7,
                    SecurityAlgorithms(), AccountResponseMapper(), null), accountPersistence)

            val view = mock<CreateAccountView>()
            val presenter = CreateAccountPresenter(view, accountManager, navigator, CompositeDisposable(), accountErrorMapper, Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val accounts = PublishSubject.create<AptoideAccount>()
            val createAccountEvent = PublishSubject.create<CreateAccountView.ViewModel>()
            val accountResponse = Response.success(OAuth(null, null,
                    "That store name is already taken, you need to choose another one.", "REPO-6"))

            whenever(accountPersistence.account).doReturn(accounts)
            whenever(accountPersistence.save(any())).doReturn(Completable.fromAction({
                accounts.onNext(AptoideAccount(true, true, TestData.STORE_NAME))
            }))
            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(view.createAccountEvent).doReturn(createAccountEvent)

            whenever(serviceV3Account.createAccount(any()))
                    .doReturn(accountResponse.toSingle().toObservable())

            presenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            createAccountEvent.onNext(publicCreateAccountModel)

            verify(view).showLoading()
            verify(view).hideLoading()
            verify(view).showErrorStoreAlreadyExists()
        }

        it("should navigate to login view") {
            val accountErrorMapper = mock<AccountErrorMapper>()
            val navigator = mock<CreateAccountNavigator>()
            val serviceV3Account = mock<RetrofitAccountService.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3Account, serviceV7,
                    SecurityAlgorithms(), AccountResponseMapper(), null), accountPersistence)
            val accounts = PublishSubject.create<AptoideAccount>()
            val view = mock<CreateAccountView>()
            val presenter = CreateAccountPresenter(view, accountManager, navigator, CompositeDisposable(), accountErrorMapper, Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val clickGoToLoginViewEvent = PublishSubject.create<CreateAccountView.ViewModel>()

            whenever(accountPersistence.account).doReturn(accounts)
            whenever(accountPersistence.save(any())).doReturn(Completable.fromAction({
                accounts.onNext(AptoideAccount(true, true, TestData.STORE_NAME))
            }))

            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(view.openLoginView).doReturn(clickGoToLoginViewEvent)

            presenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            clickGoToLoginViewEvent.onNext(publicCreateAccountModel)

            verify(navigator).navigateToLoginView()
        }

        it("should navigate to recover password view") {
            val accountErrorMapper = mock<AccountErrorMapper>()
            val navigator = mock<CreateAccountNavigator>()
            val serviceV3Account = mock<RetrofitAccountService.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV3Account, serviceV7,
                    SecurityAlgorithms(), AccountResponseMapper(), null), accountPersistence)
            val accounts = PublishSubject.create<AptoideAccount>()
            val view = mock<CreateAccountView>()
            val presenter = CreateAccountPresenter(view, accountManager, navigator, CompositeDisposable(), accountErrorMapper, Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val clickGoToRecoverPasswordViewEvent = PublishSubject.create<CreateAccountView.ViewModel>()

            whenever(accountPersistence.account).doReturn(accounts)
            whenever(accountPersistence.save(any())).doReturn(Completable.fromAction({
                accounts.onNext(AptoideAccount(true, true, TestData.STORE_NAME))
            }))

            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(view.openRecoverPasswordView).doReturn(clickGoToRecoverPasswordViewEvent)

            presenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            clickGoToRecoverPasswordViewEvent.onNext(publicCreateAccountModel)

            verify(navigator).navigateToRecoverPassView()
        }
        it("should display an error message when user wants to create an account with invalid fields") {
            val accountErrorMapper = mock<AccountErrorMapper>()
            val accountValidationException = mock<AccountValidationException>()
            val navigator = mock<CreateAccountNavigator>()
            val accountManager = mock<AptoideAccountManager>()

            val view = mock<CreateAccountView>()
            val presenter = CreateAccountPresenter(view, accountManager, navigator, CompositeDisposable(), accountErrorMapper, Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val createAccountEvent = PublishSubject.create<CreateAccountView.ViewModel>()

            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(view.createAccountEvent).doReturn(createAccountEvent)
            whenever(accountManager.create(any(), any(), any())).doReturn(Completable.error(accountValidationException))
            whenever(accountErrorMapper.map(accountValidationException)).doReturn(buildString { })

            presenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            createAccountEvent.onNext(publicCreateAccountModel)
            verify(view).showLoading()
            verify(view).hideLoading()
            verify(view).showInvalidFieldError(any())
        }
    }
})
