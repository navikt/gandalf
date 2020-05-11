package no.nav.gandalf.ldap

import org.springframework.ldap.core.DirContextOperations
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper

class NAVLdapUserDetailsMapper : LdapUserDetailsMapper() {
    private val SURNAME_ATTRIBUTE = "sn"
    private val GIVEN_NAME_ATTRIBUTE = "givenName"

    private val SERVICE_USER_DISPLAY_NAME = "Service User"

    override fun mapUserFromContext(ctx: DirContextOperations, username: String?, authorities: Collection<GrantedAuthority?>?): UserDetails? {
        val p = LdapUserDetailsImpl.Essence(ctx)
        p.setUsername(username)
        p.setAuthorities(authorities)
        p.setDn(getDisplayName(ctx))
        return p.createUserDetails()
    }

    private fun getDisplayName(ctx: DirContextOperations): String? {
        return if (ctx.attributeExists(GIVEN_NAME_ATTRIBUTE) && ctx.attributeExists(SURNAME_ATTRIBUTE)) {
            ctx.getStringAttributes(GIVEN_NAME_ATTRIBUTE)[0].toString() + " " + ctx.getStringAttributes(SURNAME_ATTRIBUTE)[0]
        } else {
            SERVICE_USER_DISPLAY_NAME
        }
    }
}
