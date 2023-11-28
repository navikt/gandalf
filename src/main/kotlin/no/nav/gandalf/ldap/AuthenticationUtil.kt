package no.nav.gandalf.ldap

import io.prometheus.client.Histogram
import no.nav.gandalf.metric.ApplicationMetric
import no.nav.gandalf.model.User
import org.slf4j.MDC

internal fun authenticate(
    ldapConnectionSetup: LDAPConnectionSetup,
    user: User,
): Boolean {
    val requestTimer: Histogram.Timer = ApplicationMetric.ldapDuration.startTimer()
    return try {
        LDAPAuthentication(ldapConnectionSetup).result(user = user)
    } finally {
        requestTimer.observeDuration()
    }
}

internal fun CustomAuthenticationProvider.authenticate(
    providedUsername: String?,
    providedPassword: String?,
): Boolean {
    val username = providedUsername ?: throw RuntimeException("Missing username")
    val password = providedPassword ?: throw RuntimeException("Missing password")
    return this.externalAuth(username, password).also { if (it) MDC.put("client_id", username) }
}
