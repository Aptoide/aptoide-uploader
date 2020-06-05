package com.aptoide.uploader.account.sendmagiclink

import android.content.Context
import android.text.SpannedString
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.aptoide.uploader.R
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.send_magic_link_layout.view.*

class SendMagicLinkView : FrameLayout {
  private var currentState: State? = null

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
      defStyleAttr) {
    inflate(context, R.layout.send_magic_link_layout, this)
    setupViews()
    isSaveEnabled = true
  }

  private fun setupViews() {
    val string: SpannedString = buildSpannedString {
      bold {
        append(context.getText(R.string.login_safe_body_1))
      }
      append(" - ")
      append(context.getText(R.string.login_safe_body_2))
    }
    login_benefits_textview.text = string
  }

  fun setState(state: State) {
    when (state) {
      State.Initial ->
        setInitialState()
      is State.Error ->
        setErrorState(state.message, state.isTextFieldError)
    }
    currentState = state
  }

  fun getMagicLinkSubmit(): Observable<String> {
    return RxView.clicks(send_magic_link_button)
        .map { email.text.toString() }
  }

  fun getEmailChangeEvent(): Observable<String> {
    return RxTextView.textChangeEvents(email)
        .map { email.text.toString() }
  }

  private fun setInitialState() {
    tip.visibility = View.VISIBLE
    tip_error.visibility = View.GONE

    email.setTextColor(resources.getColor(R.color.white))
    email.setBackgroundResource(R.drawable.button_border_grey)
  }

  private fun setErrorState(message: String, textFieldError: Boolean) {
    tip.visibility = View.GONE
    tip_error.visibility = View.VISIBLE
    tip_error.text = message

    if (textFieldError) {
      email.setTextColor(resources.getColor(R.color.darker_red))
      email.setBackgroundResource(R.drawable.button_border_red)
    }
  }

  fun resetTextFieldError() {
    currentState?.let { state ->
      if (state is State.Error && state.isTextFieldError) {
        setState(State.Initial)
      }
    }
  }

  sealed class State {
    object Initial : State()
    data class Error(val message: String, val isTextFieldError: Boolean) : State()
  }
}