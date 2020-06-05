package com.aptoide.uploader.account.sendmagiclink

import com.aptoide.uploader.account.AptoideAccountManager
import com.aptoide.uploader.view.Presenter
import com.aptoide.uploader.view.View.LifecycleEvent
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.OnErrorNotImplementedException


class SendMagicLinkPresenter(
    private val view: MagicLinkView,
    private val accountManager: AptoideAccountManager,
    private val navigator: SendMagicLinkNavigator,
    private val viewScheduler: Scheduler,
    private val agentPersistence: AgentPersistence) : Presenter {

  private lateinit var compositeDisposable: CompositeDisposable

  override fun present() {
    compositeDisposable = CompositeDisposable()
    handleSendMagicLinkClick()
    handleEmailChangeEvents()
    onDestroyDisposeComposite()
  }

  private fun handleEmailChangeEvents() {
    compositeDisposable.add(
        view.lifecycleEvent
            .filter { lifecycleEvent -> LifecycleEvent.CREATE == lifecycleEvent }
            .flatMap {
              view.getEmailTextChangeEvent()
                  .doOnNext { view.removeTextFieldError() }
                  .retry()
            }
            .subscribe({}, { e -> e.printStackTrace() })
    )

  }

  private fun handleSendMagicLinkClick() {
    compositeDisposable.add(
        view.lifecycleEvent
            .filter { lifecycleEvent -> LifecycleEvent.CREATE == lifecycleEvent }
            .flatMap {
              view.getMagicLinkClick()
                  .flatMap { email ->
                    validateEmail(email)
                        .filter { valid -> valid }
                        .observeOn(viewScheduler)
                        .doOnNext { view.setLoadingScreen() }
                        .flatMapSingle {
                          accountManager.sendMagicLink(email)
                              .observeOn(viewScheduler)
                              .doOnSuccess {
                                agentPersistence.persistAgent(it.agent, it.state, it.email)
                                view.removeLoadingScreen()
                                navigator.navigateToCheckYourEmail(email)
                              }
                        }.observeOn(viewScheduler).doOnError {
                          view.removeLoadingScreen()
                          view.showUnknownError()
                          it.printStackTrace()
                        }
                  }
                  .retry()
            }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun validateEmail(email: String): Observable<Boolean> {
    return accountManager.isEmailValid(email)
        .toObservable()
        .observeOn(viewScheduler)
        .doOnNext { isValid ->
          if (!isValid) {
            view.setEmailInvalidError()
          }
        }
  }

  private fun onDestroyDisposeComposite() {
    compositeDisposable.add(view.lifecycleEvent
        .filter { event: LifecycleEvent -> event == LifecycleEvent.DESTROY }
        .doOnNext { compositeDisposable.clear() }
        .subscribe({}) { throwable ->
          throw OnErrorNotImplementedException(throwable)
        })
  }

}