package com.aptoide.uploader.login.facebook

import org.junit.runner.RunWith

/**
 * [Testing Fundamentals](http://d.android.com/tools/testing/testing_android.html)
 */
@RunWith(JUnitPlatform::class)
class Scenario2 : Spek({
    val user = MockFacebookUser()

    given("facebook_user = $user") {
        val loginInteractor = LoginInteractor()
        val account = loginInteractor.authenticate(user)

        it("should have a valid account") {
            assertEquals(true, account.hasValidAccount())
        }

        it("should have a store") {
            assertEquals(true, account.hasStore())
        }

    }

})