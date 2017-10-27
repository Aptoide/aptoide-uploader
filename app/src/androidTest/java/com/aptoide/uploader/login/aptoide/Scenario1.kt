package com.aptoide.uploader.login.aptoide

import org.junit.runner.RunWith

/**
 * [Testing Fundamentals](http://d.android.com/tools/testing/testing_android.html)
 */
@RunWith(JUnitPlatform::class)
class Scenario1 : Spek({
    val email = "user1@aptoide.com"
    val pass = "passwordtest1"

    given("email = $email and password = $pass") {
        val loginInteractor = LoginInteractor()
        val account = loginInteractor.authenticate(email, pass)

        it("should have a valid account") {
            assertEquals(true, account.hasValidAccount())
        }

        it("should have a store") {
            assertEquals(true, account.hasStore())
        }

    }

})