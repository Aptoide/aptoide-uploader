package com.aptoide.uploader.account.sendmagiclink

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.aptoide.uploader.R

class SendMagicLinkNavigator(private val fragmentManager: FragmentManager?) {

  fun navigateToCheckYourEmail(email: String) {
    navigateTo(CheckYourEmailFragment.newInstance(email))
  }

  private fun navigateTo(fragment: Fragment) {
    fragmentManager?.beginTransaction()
        ?.replace(R.id.activity_main_container, fragment)
        ?.addToBackStack(null)
        ?.commitAllowingStateLoss()
  }
}