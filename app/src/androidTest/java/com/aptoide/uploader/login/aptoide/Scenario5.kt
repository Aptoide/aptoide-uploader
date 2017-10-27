package com.aptoide.uploader.login.aptoide

import org.junit.runner.RunWith

/**
 * [Testing Fundamentals](http://d.android.com/tools/testing/testing_android.html)
 */
@RunWith(JUnitPlatform::class)
class Scenario5 : Spek({
    val email = "user5@aptoide.com"
    val pass = "passwordtest5"

    given("email = $email and password = $pass") {
        val loginInteractor = LoginInteractor()
        val account = loginInteractor.authenticate(email, pass)

        it("should have a valid account") {
            assertEquals(true, account.hasValidAccount())
        }

        it("should not have a store") {
            assertEquals(false, account.hasStore())
        }

    }

})