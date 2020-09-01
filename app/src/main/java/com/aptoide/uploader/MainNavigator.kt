package com.aptoide.uploader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.aptoide.uploader.account.Navigator
import com.aptoide.uploader.account.view.AutoLoginFragment
import com.aptoide.uploader.account.view.LoginFragment
import com.aptoide.uploader.apps.view.AppFormFragment

class MainNavigator(val activity: AppCompatActivity) : Navigator() {

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

  override fun navigateToAutoLoginFragment(name: String?, avatarPath: String?) {
    var fragment = AutoLoginFragment.newInstance(name, avatarPath)
    var fragmentTransaction: FragmentTransaction =
        activity.supportFragmentManager.beginTransaction()
    fragmentTransaction.replace(R.id.activity_main_container, fragment, null)
    fragmentTransaction.commit()
  }

  override fun navigateToAutoLoginFragment(name: String?) {
    var fragment = AutoLoginFragment.newInstance(name, null)
    var fragmentTransaction: FragmentTransaction =
        activity.supportFragmentManager.beginTransaction()
    fragmentTransaction.replace(R.id.activity_main_container, fragment, null)
    fragmentTransaction.commit()
  }

  override fun navigateToLoginFragment() {
    var fragmentTransaction: FragmentTransaction =
        activity.supportFragmentManager.beginTransaction()
    fragmentTransaction.replace(R.id.activity_main_container, LoginFragment.newInstance(), null)
    fragmentTransaction.commit()
  }
}