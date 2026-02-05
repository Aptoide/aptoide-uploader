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
import androidx.appcompat.widget.Toolbar
import com.aptoide.uploader.R
import com.aptoide.uploader.UploaderApplication
import com.aptoide.uploader.account.view.LoginNavigator
import com.aptoide.uploader.view.android.FragmentView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

class CheckYourEmailFragment : FragmentView(), CheckYourEmailView {

  lateinit var openEmailAppButton: Button
  lateinit var openEmailBody: TextView
  private var email: String? = null

  private lateinit var toolbar: Toolbar
  private lateinit var fragmentLoginLoadingTextView: TextView
  private lateinit var checkYourEmailLayout: View
  private lateinit var fragmentLoginProgressContainer: View

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
    bindViews(view)
    setupToolbar()
    setupViews(view)
    val app = (requireContext().applicationContext as UploaderApplication)
    CheckYourEmailPresenter(this, CheckYourEmailNavigator(activity), app.accountManager,
        AndroidSchedulers.mainThread(),
        LoginNavigator(fragmentManager, requireContext().applicationContext)).present()
  }

  private fun bindViews(view: View) {
    toolbar = view.findViewById(R.id.toolbar)
    fragmentLoginLoadingTextView = view.findViewById(R.id.fragment_login_loading_text_view)
    checkYourEmailLayout = view.findViewById(R.id.check_your_email_layout)
    fragmentLoginProgressContainer = view.findViewById(R.id.fragment_login_progress_container)
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

  override fun showLoadingWithoutUserName() {
    fragmentLoginLoadingTextView.text = getString(R.string.logging_in)
    checkYourEmailLayout.visibility = View.GONE
    fragmentLoginProgressContainer.visibility = View.VISIBLE
  }

  override fun hideLoading() {
    checkYourEmailLayout.visibility = View.VISIBLE
    fragmentLoginProgressContainer.visibility = View.GONE
  }

}
