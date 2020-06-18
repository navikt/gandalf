package no.nav.gandalf.util

import io.prometheus.client.Histogram
import no.nav.gandalf.ldap.LDAPAuthentication
import no.nav.gandalf.ldap.LDAPConnectionSetup
import no.nav.gandalf.metric.ApplicationMetric
import no.nav.gandalf.model.User

internal fun authenticate(ldapConnectionSetup: LDAPConnectionSetup, user: User): Boolean {
    val requestTimer: Histogram.Timer = ApplicationMetric.ldapDuration.startTimer()
    return try {
        LDAPAuthentication(ldapConnectionSetup).result(user = user)
    } finally {
        requestTimer.observeDuration()
    }
}
