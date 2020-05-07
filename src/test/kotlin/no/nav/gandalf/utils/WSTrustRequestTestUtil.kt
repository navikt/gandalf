package no.nav.gandalf.utils

import org.apache.commons.codec.binary.Base64

internal fun getOidcToSamlRequest(brukerNavn: String, passord: String, oidcToken: String) = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" +
        "<soapenv:Header><wsse:Security xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"><wsse:UsernameToken wsu:Id=\"UsernameToken-D565636FC9452BE5F91481269604546492\">" +
        "<wsse:Username>" + brukerNavn + "</wsse:Username>" +
        "<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">" + passord + "</wsse:Password></wsse:UsernameToken></wsse:Security></soapenv:Header>" +
        "<soapenv:Body>" +
        "<wst:RequestSecurityToken xmlns:wst=\"http://docs.oasis-open.org/ws-sx/ws-trust/200512\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">" +
        "<wst:SecondaryParameters>" +
        "<wst:TokenType>http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0</wst:TokenType>" +
        "</wst:SecondaryParameters>" +
        "<wst:RequestType>http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue</wst:RequestType>" +
        "<wst:KeyType>http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer</wst:KeyType>" +
        "<wst:OnBehalfOf>" + getWrappedOidcToken(oidcToken) + "</wst:OnBehalfOf>" +
        "<wst:Renewing Allow=\"false\"/>" +
        "</wst:RequestSecurityToken>" +
        "</soapenv:Body>" +
        "</soapenv:Envelope>"

// datapower sts forventer "dobbel" base64 encoding
internal fun getWrappedOidcToken(oidcToken: String) = ("<wsse:BinarySecurityToken EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\" ValueType=\"urn:ietf:params:oauth:token-type:jwt\">" +
        Base64.encodeBase64URLSafeString(oidcToken.toByteArray()) +
        "</wsse:BinarySecurityToken>")

internal fun getSamlRequest(brukerNavn: String, passord: String) = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        "<soap:Header>" +
        "<wsse:Security soap:mustUnderstand=\"1\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" +
        "<wsse:UsernameToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"UsernameToken-abac\">" +
        "<wsse:Username>" + brukerNavn + "</wsse:Username>" +
        "<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">" + passord + "</wsse:Password>" +
        "</wsse:UsernameToken>" +
        "</wsse:Security>" +
        "</soap:Header>" +
        "<soap:Body>" +
        "<wst:RequestSecurityToken xmlns:wst=\"http://docs.oasis-open.org/ws-sx/ws-trust/200512\">" +
        "<wst:TokenType>http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0</wst:TokenType>" +
        "<wst:KeyType>http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer</wst:KeyType>" +
        "<wst:RequestType>http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue</wst:RequestType>" +
        "<wst:Renewing Allow=\"false\"/>" +
        "</wst:RequestSecurityToken>" +
        "</soap:Body>" +
        "</soap:Envelope>"

internal fun getValidateSamlRequest(brukerNavn: String, passord: String, samlToken: String) = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        "<soap:Header>" +
        "<wsse:Security soap:mustUnderstand=\"1\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" +
        "<wsse:UsernameToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"UsernameToken-abac\">" +
        "<wsse:Username>" + brukerNavn + "</wsse:Username>" +
        "<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">" + passord + "</wsse:Password>" +
        "</wsse:UsernameToken>" +
        "</wsse:Security>" +
        "</soap:Header>" +
        "<soap:Body>" +
        "<wst:RequestSecurityToken xmlns:wst=\"http://docs.oasis-open.org/ws-sx/ws-trust/200512\">" +
        "<wst:SecondaryParameters>" +
        "<wst:TokenType>http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0</wst:TokenType>" +
        "</wst:SecondaryParameters>" +
        "<wst:KeyType>http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer</wst:KeyType>" +
        "<wst:RequestType>http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate</wst:RequestType>" +
        "<wst:ValidateTarget>" +
        "<wsse:SecurityTokenReference xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" +
        "<wsse:Embedded>" + samlToken + "</wsse:Embedded>" +
        "</wsse:SecurityTokenReference>" +
        "</wst:ValidateTarget>" +
        "</wst:RequestSecurityToken>" +
        "</soap:Body>" +
        "</soap:Envelope>"
