package com.aptoide.uploader.login.aptoide

import org.junit.runner.RunWith

/**
 * [Testing Fundamentals](http://d.android.com/tools/testing/testing_android.html)
 */
@RunWith(JUnitPlatform::class)
class Scenario4 : Spek({
    val accountProvider = MockAccountProvider()

    given("That I have an account provider = $accountProvider") {
        val account = accountProvider.account

        it("should have a valid account") {
            assertEquals(true, account.isValid())
        }

        it("should have a store") {
            assertEquals(true, account.hasStore())
        }

    }

})