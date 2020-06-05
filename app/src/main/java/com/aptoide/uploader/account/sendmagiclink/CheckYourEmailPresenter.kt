package com.aptoide.uploader.account.sendmagiclink

import com.aptoide.uploader.view.Presenter
import com.aptoide.uploader.view.View
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.OnErrorNotImplementedException

class CheckYourEmailPresenter(private val view: CheckYourEmailView,
                              private val navigator: CheckYourEmailNavigator) : Presenter {
  private lateinit var compositeDisposable: CompositeDisposable

  override fun present() {
    handleCheckEmailAppClick()
    onDestroyDisposeComposite()
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