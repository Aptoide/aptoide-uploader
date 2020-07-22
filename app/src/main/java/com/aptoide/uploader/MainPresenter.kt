package com.aptoide.uploader

import android.net.Uri
import com.aptoide.authentication.AuthenticationException
import com.aptoide.uploader.account.AgentPersistence
import com.aptoide.uploader.account.AptoideAccountManager
import com.aptoide.uploader.account.AptoideCredentials
import com.aptoide.uploader.account.AutoLoginManager
import com.aptoide.uploader.apps.UploadManager
import com.aptoide.uploader.view.Presenter
import com.aptoide.uploader.view.View
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.OnErrorNotImplementedException

class MainPresenter(val view: MainView, val accountManager: AptoideAccountManager,
                    var autoLoginManager: AutoLoginManager,
                    val agentPersistence: AgentPersistence, val viewScheduler: Scheduler,
                    val uploadManager: UploadManager, val mainNavigator: MainNavigator) :
    Presenter {

  private lateinit var compositeDisposable: CompositeDisposable

  override fun present() {
    compositeDisposable = CompositeDisposable()
    handleLoginView()
    handleIntentEvents()
    onDestroyDisposeComposite()
  }

  private fun isNullOrEmpty(str: String?): Boolean {
    if (str != null && !str.trim().isEmpty())
      return false
    return true
  }

  private fun handleLoginView() {
    autoLoginManager.storedUserCredentials
    if (isNullOrEmpty(autoLoginManager.autoLoginCredentials.email)) {
      mainNavigator.navigateToLoginFragment()
    } else {
      if (isNullOrEmpty(autoLoginManager.autoLoginCredentials.storeName)) {
        mainNavigator.navigateToAutoLoginFragment(autoLoginManager.autoLoginCredentials.email)
      } else {
        mainNavigator.navigateToAutoLoginFragment(autoLoginManager.autoLoginCredentials.storeName)
      }
    }
  }

  private fun handleIntentEvents() {
    compositeDisposable.add(
        view.getIntentEvents()
            .flatMap { intent ->
              when (intent.action) {
                "navigateToSubmitAppFragment" -> {
                  return@flatMap handleNavigateToSubmitAppsIntent(intent)
                }
                "dismissNotification" -> {
                  return@flatMap handleDismissNotificationIntent(intent)
                }
                "android.intent.category.BROWSABLE",
                "android.intent.category.DEFAULT",
                "android.intent.action.VIEW" -> {
                  return@flatMap handleActionViewIntent(intent)
                }
                else -> return@flatMap Observable.just(intent)
              }
            }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleNavigateToSubmitAppsIntent(intentData: IntentData): Observable<IntentData> {
    return Observable.just(intentData)
        .doOnNext {
          val md5: String = intentData.extras["md5"] as String
          val appName: String = intentData.extras["appName"] as String
          mainNavigator.navigateToSubmitAppView(md5, appName)
        }
  }

  private fun handleDismissNotificationIntent(intentData: IntentData): Observable<IntentData> {
    val md5: String = intentData.extras["md5"] as String
    return uploadManager.removeUploadFromPersistence(md5)
        .doOnComplete { uploadManager.removeUploadFromQueue(md5) }
        .andThen(Observable.just(intentData))
  }

  private fun handleActionViewIntent(intentData: IntentData): Observable<IntentData>? {
    var uri: Uri? = null
    try {
      uri = Uri.parse(intentData.data)
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return safeLetArg<Uri, String, String, Observable<IntentData>?>(uri, uri?.host,
        uri?.path) { u, host, path ->
      when {
        u.scheme.equals("aptoideauth", ignoreCase = true) -> {
          val token: String = intentData.data.split("aptoideauth://").toTypedArray()[1]
          return@safeLetArg authenticate(token)
              .andThen(Observable.just(intentData))
        }
        host.contains("app.aptoide.com") && path.contains("auth/code/") -> {
          val token = path.split("auth/code/")[1]
          return@safeLetArg authenticate(token)
              .andThen(Observable.just(intentData))
        }
        else -> return@safeLetArg Observable.just(intentData)
      }
    }

    return Observable.just(intentData)
  }

  private fun authenticate(authToken: String): Completable {
    return accountManager.login(AptoideCredentials(agentPersistence.getEmail(), authToken, true,
        agentPersistence.getAgent(), agentPersistence.getState()))
        .observeOn(viewScheduler)
        .doOnSubscribe { view.showLoadingView() }
        .doOnComplete { view.hideLoadingView() }
        .doOnError { throwable ->
          view.hideLoadingView()
          if (throwable is AuthenticationException
              && (throwable.code in 400..499)) {
            mainNavigator.navigateToLoginError()
          } else {
            view.showGenericErrorMessage()
          }
        }
        .onErrorComplete()
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