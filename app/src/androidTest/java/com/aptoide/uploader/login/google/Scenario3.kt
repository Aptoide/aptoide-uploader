package com.aptoide.uploader.login.google

import org.junit.runner.RunWith

/**
 * [Testing Fundamentals](http://d.android.com/tools/testing/testing_android.html)
 */
@RunWith(JUnitPlatform::class)
class Scenario3 : Spek({
    val user = MockGoogleUser()

    given("google_user = $user") {
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