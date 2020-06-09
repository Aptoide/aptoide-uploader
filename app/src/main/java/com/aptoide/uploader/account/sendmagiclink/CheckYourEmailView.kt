package com.aptoide.uploader.account.sendmagiclink

import com.aptoide.uploader.view.View
import io.reactivex.Observable

interface CheckYourEmailView : View {

  fun getCheckYourEmailClick(): Observable<Any>
  fun showLoadingWithoutUserName()
  fun hideLoading()
}