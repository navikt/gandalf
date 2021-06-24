package no.nav.gandalf.accesstoken.saml

internal fun getUnsignedSaml(samlObject: SamlObject): String {
    val format = samlObject.format
    return "<saml2:Assertion xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\" ID=\"" + samlObject.id + "\"" +
        " IssueInstant=\"" + samlObject.issueInstant!!.format(format) + "\" Version=\"2.0\">" +
        "<saml2:Issuer>" + samlObject.issuer + "</saml2:Issuer>" +
        "<saml2:Subject><saml2:NameID Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\">" + samlObject.nameID +
        "</saml2:NameID><saml2:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\">" +
        "<saml2:SubjectConfirmationData NotBefore=\"" + samlObject.dateNotBefore!!.format(format) + "\" NotOnOrAfter=\"" + samlObject.notOnOrAfter + "\"/></saml2:SubjectConfirmation></saml2:Subject>" +
        "<saml2:Conditions NotBefore=\"" + samlObject.dateNotBefore!!.format(format) + "\" NotOnOrAfter=\"" + samlObject.notOnOrAfter!!.format(
        format
    ) + "\"/><saml2:AttributeStatement>" +
        "<saml2:Attribute Name=\"identType\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\"><saml2:AttributeValue>" + samlObject.identType + "</saml2:AttributeValue></saml2:Attribute>" +
        (if (samlObject.authenticationLevel != null) "<saml2:Attribute Name=\"authenticationLevel\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\"><saml2:AttributeValue>${samlObject.authenticationLevel}</saml2:AttributeValue></saml2:Attribute>" else "") +
        (if (samlObject.consumerId != null) "<saml2:Attribute Name=\"consumerId\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\"><saml2:AttributeValue>${samlObject.consumerId}</saml2:AttributeValue></saml2:Attribute>" else "") +
        (if (samlObject.auditTrackingId != null) "<saml2:Attribute Name=\"auditTrackingId\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\"><saml2:AttributeValue>${samlObject.auditTrackingId}</saml2:AttributeValue></saml2:Attribute>" else "") +
        "</saml2:AttributeStatement></saml2:Assertion>"
}
