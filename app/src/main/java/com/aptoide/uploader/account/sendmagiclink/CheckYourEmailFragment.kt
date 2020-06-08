package com.aptoide.uploader.account.sendmagiclink

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aptoide.uploader.R
import com.aptoide.uploader.view.android.FragmentView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_check_your_email.*

class CheckYourEmailFragment : FragmentView(), CheckYourEmailView {

  lateinit var openEmailAppButton: Button
  lateinit var openEmailBody: TextView
  private var email: String? = null

  companion object {
    private const val EMAIL = "email"

    fun newInstance(email: String) = CheckYourEmailFragment().apply {
      arguments = Bundle().apply {
        putString(EMAIL, email)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let { args ->
      email = args.getString(EMAIL)
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_check_your_email, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupToolbar()
    setupViews(view)
    CheckYourEmailPresenter(this, CheckYourEmailNavigator(activity)).present()
  }

  private fun setupViews(view: View) {
    openEmailAppButton = view.findViewById(R.id.open_email_app_button)
    openEmailBody = view.findViewById(R.id.check_your_email_body_text)

    email?.let { e ->
      val emailString: String = getString(R.string.login_check_email_body, e)
      val spannable = SpannableString(emailString)
      spannable.setSpan(NonbreakingSpan(), emailString.indexOf(e),
          emailString.indexOf(e) + e.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
      openEmailBody.text = spannable
    }
  }

  private fun setupToolbar() {
    (activity as AppCompatActivity?)?.let { activity ->
      activity.setSupportActionBar(toolbar)
      val actionBar = activity.supportActionBar
      actionBar?.setDisplayHomeAsUpEnabled(true)
      actionBar?.title = toolbar.title
      toolbar.title = ""
    }
  }

  override fun getCheckYourEmailClick(): Observable<Any> {
    return RxView.clicks(openEmailAppButton)
  }

}