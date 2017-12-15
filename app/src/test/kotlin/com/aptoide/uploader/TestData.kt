package com.aptoide.uploader

import com.aptoide.uploader.account.network.OAuth
import retrofit2.Response


class TestData {
    companion object {
        val STORE_NAME = "marcelo"
        val USER_NAME = "marcelo@aptoide.com"
        val USER_PASSWORD = "aptoide1234"
        val STORE_USER = "marcelo@aptoide.com"
        val STORE_PASSWORD = "aptoide1234"
        val SUCCESS_RESPONSE = Response.success(OAuth("abc", "def",
                null, null))

        val APPINFO_NAME = "Testing"
        val PROPOSED_APP_RATING = 1
        val APPINFO_CATEGORY = 18
        val APPINFO_LANGUAGE = "en"
        val APPINFO_DESCRIPTION ="Aptoide is an open source independent Android app store that allows you to install and discover apps in an easy, exciting and safe way."
        val APPINFO_PHONE_NR = "123456789"
        val APPINFO_EMAIL = "aptoide@aptoide.com"
        val APPINFO_WEBSITE ="http://aptoide.com"
    }
}
