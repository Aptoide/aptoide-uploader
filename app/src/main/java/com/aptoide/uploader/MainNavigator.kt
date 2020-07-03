package com.aptoide.uploader

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.aptoide.uploader.account.view.LoginFragment
import com.aptoide.uploader.apps.view.AppFormFragment

class MainNavigator(val activity: AppCompatActivity) {

  fun navigateToSubmitAppView(md5: String?, appName: String?) {
    navigateToWithoutBackSave(R.id.activity_main_container,
        AppFormFragment.newInstance(md5, appName), true)
  }

  private fun navigateToWithoutBackSave(containerId: Int,
                                        fragment: Fragment,
                                        replace: Boolean) {
    var fragmentTransaction: FragmentTransaction =
        activity.supportFragmentManager.beginTransaction()
    fragmentTransaction = if (replace) {
      fragmentTransaction.replace(containerId, fragment)
    } else {
      fragmentTransaction.add(containerId, fragment)
    }
    fragmentTransaction.commit()
  }

  fun navigateToLoginError() {
    var fragmentTransaction: FragmentTransaction =
        activity.supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out)
    fragmentTransaction =
        fragmentTransaction.replace(R.id.activity_main_container, LoginFragment.newInstance(true,
            activity.getString(R.string.login_error_magic_link_expired)), null)
    fragmentTransaction.commit()
  }
}