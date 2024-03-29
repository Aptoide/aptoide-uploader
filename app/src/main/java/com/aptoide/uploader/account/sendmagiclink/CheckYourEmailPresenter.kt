package com.aptoide.uploader.account.sendmagiclink

import com.aptoide.uploader.account.AptoideAccountManager
import com.aptoide.uploader.account.view.LoginNavigator
import com.aptoide.uploader.view.Presenter
import com.aptoide.uploader.view.View
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.OnErrorNotImplementedException

class CheckYourEmailPresenter(private val view: CheckYourEmailView,
                              private val navigator: CheckYourEmailNavigator,
                              private val accountManager: AptoideAccountManager,
                              private val viewScheduler: Scheduler,
                              private val loginNavigator: LoginNavigator) : Presenter {
  private lateinit var compositeDisposable: CompositeDisposable

  override fun present() {
    compositeDisposable = CompositeDisposable()

    handleCheckEmailAppClick()
    onDestroyDisposeComposite()
    handleAccountEvents()
  }

  private fun handleAccountEvents() {
    compositeDisposable.add(view.lifecycleEvent
        .filter { lifecycleEvent -> View.LifecycleEvent.CREATE == lifecycleEvent }
        .flatMap { accountManager.getAccount() }
        .observeOn(viewScheduler)
        .doOnNext { account ->
          if (account.isLoggedIn) {
            view.showLoadingWithoutUserName()
            if (account.hasStore()) {
              loginNavigator.navigateToMyAppsView()
            } else {
              loginNavigator.navigateToCreateStoreView()
            }
          } else {
            view.hideLoading()
          }
        }
        .subscribe({}, { e -> e.printStackTrace() })
    )

  }

  private fun handleCheckEmailAppClick() {
    compositeDisposable.add(
        view.lifecycleEvent
            .filter { lifecycleEvent -> View.LifecycleEvent.CREATE == lifecycleEvent }
            .flatMap {
              view.getCheckYourEmailClick()
                  .doOnNext { navigator.navigateToEmailApp() }
                  .retry()
            }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun onDestroyDisposeComposite() {
    compositeDisposable.add(view.lifecycleEvent
        .filter { event: View.LifecycleEvent -> event == View.LifecycleEvent.DESTROY }
        .doOnNext { compositeDisposable.clear() }
        .subscribe({}) { throwable ->
          throw OnErrorNotImplementedException(throwable)
        })
  }
}