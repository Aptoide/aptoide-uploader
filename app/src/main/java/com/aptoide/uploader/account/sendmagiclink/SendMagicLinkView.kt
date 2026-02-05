package com.aptoide.uploader.account.sendmagiclink

import android.content.Context
import android.text.SpannedString
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.aptoide.uploader.R
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable

class SendMagicLinkView : FrameLayout {
  private var currentState: State? = null

  private lateinit var loginBenefitsTextview: TextView
  private lateinit var sendMagicLinkButton: Button
  private lateinit var emailEditText: EditText
  private lateinit var tip: TextView
  private lateinit var tipError: TextView

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
      defStyleAttr) {
    inflate(context, R.layout.send_magic_link_layout, this)
    bindViews()
    setupViews()
    isSaveEnabled = true
  }

  private fun bindViews() {
    loginBenefitsTextview = findViewById(R.id.login_benefits_textview)
    sendMagicLinkButton = findViewById(R.id.send_magic_link_button)
    emailEditText = findViewById(R.id.email)
    tip = findViewById(R.id.tip)
    tipError = findViewById(R.id.tip_error)
  }

  private fun setupViews() {
    val string: SpannedString = buildSpannedString {
      bold {
        append(context.getText(R.string.login_safe_body_1))
      }
      append(" - ")
      append(context.getText(R.string.login_safe_body_2))
    }
    loginBenefitsTextview.text = string
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
    return RxView.clicks(sendMagicLinkButton)
        .map { emailEditText.text.toString() }
  }

  fun getEmailChangeEvent(): Observable<String> {
    return RxTextView.textChangeEvents(emailEditText)
        .map { emailEditText.text.toString() }
  }

  fun getSecureLoginTextClick(): Observable<Any> {
    return RxView.clicks(loginBenefitsTextview)
  }

  private fun setInitialState() {
    tip.visibility = View.VISIBLE
    tipError.visibility = View.GONE

    emailEditText.setTextColor(resources.getColor(R.color.white))
    emailEditText.setBackgroundResource(R.drawable.button_border_grey)
  }

  private fun setErrorState(message: String, textFieldError: Boolean) {
    tip.visibility = View.GONE
    tipError.visibility = View.VISIBLE
    tipError.text = message

    if (textFieldError) {
      emailEditText.setTextColor(resources.getColor(R.color.darker_red))
      emailEditText.setBackgroundResource(R.drawable.button_border_red)
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
