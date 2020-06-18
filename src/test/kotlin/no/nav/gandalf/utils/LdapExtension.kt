package no.nav.gandalf.utils

import no.nav.gandalf.ldap.InMemoryLdap
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource

class LdapExtension : BeforeAllCallback, CloseableResource {
    private val inMemoryLdap = InMemoryLdap()

    override fun beforeAll(context: ExtensionContext) {
        if (!started) {
            started = true
            inMemoryLdap.start()
            // Your "before all tests" startup logic goes here
            // The following line registers a callback hook when the root test context is shut down
            context.root.getStore(GLOBAL).put("any unique name", this)
        }
    }

    override fun close() {
        inMemoryLdap.stop()
        started = false
        // Your "after all tests" logic goes here
    }

    companion object {
        private var started = false
    }
}
