package com.aptoide.uploader

import com.aptoide.uploader.account.network.OAuth
import retrofit2.Response


class TestData {
    companion object {
        val STORE_NAME = "marcelo"
        val USER_NAME = "marcelo@aptoide.com"
        val USER_PASSWORD = "aptoide1234"
        val SUCCESS_RESPONSE = Response.success(OAuth("abc", "def",
                null, null))

    }
}
