package com.aptoide.uploader.login.aptoide

import org.junit.runner.RunWith

/**
 * [Testing Fundamentals](http://d.android.com/tools/testing/testing_android.html)
 */
@RunWith(JUnitPlatform::class)
class AccountProviderReturnsInvalidAccount : Spek({
    val accountProvider = MockAccountProvider()

    given("That I have an account provider = $accountProvider") {
        val account = accountProvider.account

        it("should have an invalid account") {
            assertEquals(false, account.isValid())
        }

    }

})