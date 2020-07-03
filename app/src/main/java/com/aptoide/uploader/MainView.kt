package com.aptoide.uploader

import com.aptoide.uploader.view.IntentView

interface MainView : IntentView {
  fun showLoadingView()
  fun hideLoadingView()
  fun showGenericErrorMessage()

}