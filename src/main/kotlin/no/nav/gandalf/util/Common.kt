package no.nav.gandalf.util

import no.nav.gandalf.config.LdapConfig
import no.nav.gandalf.ldap.LDAPAuthentication
import no.nav.gandalf.ldap.LDAPConnectionSetup
import no.nav.gandalf.model.User

internal fun authenticate(ldapConfig: LdapConfig, user: User) =
    LDAPAuthentication(
        LDAPConnectionSetup(ldapConfig = ldapConfig)
    ).result(user = user)
