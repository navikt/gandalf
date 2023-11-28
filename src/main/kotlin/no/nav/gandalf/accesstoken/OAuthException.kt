package no.nav.gandalf.accesstoken

import com.nimbusds.oauth2.sdk.ErrorObject

data class OAuthException(
    val errorObject: ErrorObject? = null,
    val throwable: Throwable? = null,
) : RuntimeException(errorObject?.toJSONObject()?.toJSONString(), throwable)
