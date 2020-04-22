package no.nav.gandalf

import java.security.cert.X509Certificate
import javax.xml.crypto.AlgorithmMethod
import javax.xml.crypto.KeySelector
import javax.xml.crypto.KeySelectorException
import javax.xml.crypto.KeySelectorResult
import javax.xml.crypto.XMLCryptoContext
import javax.xml.crypto.XMLStructure
import javax.xml.crypto.dsig.keyinfo.KeyInfo
import javax.xml.crypto.dsig.keyinfo.X509Data

class TestKeySelector : KeySelector() {
    @Throws(KeySelectorException::class)
    override fun select(keyInfo: KeyInfo, purpose: Purpose, method: AlgorithmMethod, context: XMLCryptoContext): KeySelectorResult {
        for (`object` in keyInfo.content) {
            val info = `object` as XMLStructure
            if (info is X509Data) {
                for (cert in info.content) {
                    if (cert is X509Certificate) {
                        return KeySelectorResult { cert.publicKey }
                    }
                }
            }
        }
        throw KeySelectorException("Failed to find X509 Certificate")
    }
}