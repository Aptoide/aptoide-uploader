package com.aptoide.authenticationrx

import com.aptoide.authentication.AptoideAuthentication
import com.aptoide.authentication.model.CodeAuth
import com.aptoide.authentication.model.OAuth2
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle

class AptoideAuthenticationRx(private val aptoideAuthentication: AptoideAuthentication) {

    /**
     * Sends a magic link to the user's email for authentication.
     *
     * @param email The email address of the user.
     * @return A [Single] emitting the [CodeAuth] object containing the transaction ID and the code.
     */
    fun sendMagicLink(email: String): Single<CodeAuth> {
        return rxSingle { aptoideAuthentication.sendMagicLink(email) }
    }

    /**
     * Authenticates the user with the provided magic code, state and agent.
     *
     * @param magicCode The magic code provided in the email.
     * @param state The state of the authentication request.
     * @param agent The user agent of the client making the request.
     * @return A [Single] emitting the [OAuth2] object containing the access token and its type.
     */
    fun authenticate(magicCode: String, state: String, agent: String): Single<OAuth2> {
        return rxSingle { aptoideAuthentication.authenticate(magicCode, state, agent) }
    }
}
