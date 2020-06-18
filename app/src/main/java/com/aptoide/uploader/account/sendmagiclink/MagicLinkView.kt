package com.aptoide.uploader.account.sendmagiclink

import com.aptoide.uploader.view.View
import io.reactivex.Observable

interface MagicLinkView : View {

  fun getMagicLinkClick(): Observable<String>

  fun setInitialState()

  fun removeTextFieldError()

  fun setEmailInvalidError()

  fun setLoadingScreen()

  fun removeLoadingScreen()

  fun getEmailTextChangeEvent(): Observable<String>

  fun showUnknownError()

}