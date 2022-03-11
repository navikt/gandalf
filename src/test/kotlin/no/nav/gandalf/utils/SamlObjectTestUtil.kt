package no.nav.gandalf.utils

import no.nav.gandalf.model.IdentType

internal fun getSamlToken() =
    """<saml2:Assertion xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" ID="SAML-4161a46a-ebc3-403f-9d3d-4eff65a070ae" IssueInstant="2019-05-14T07:47:06.255Z" Version="2.0"><saml2:Issuer>IS02</saml2:Issuer><Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/><SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/><Reference URI="#SAML-4161a46a-ebc3-403f-9d3d-4eff65a070ae"><Transforms><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/><Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/></Transforms><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><DigestValue>+JCjyWxegtHXJKHTkHPlEegluxc=</DigestValue></Reference></SignedInfo><SignatureValue>CnpPln7EcTsBR7nIT8EYLkWIwoT5GlRWcZCO5d2mwBqEmGNKd3lF2luLBjHJEhgCufgQCUivcwmP
u4tFDA5Sa8rqoCuyrFNi7UVEYrWWrUwK72vTCX25tnGUgplsukxH/YO2V9NVAKTDQg+sgQ7IpvTS
9OeQ+dtI0ezIOq+KkjC5auqQw49dyHrjlDhmYU9QjdGtvq0hegp/ksIms3ehNamzDoq+NxEA6BKC
tDnK84naoirZJjgqLIJjUa2QjetwKdr5jPt7JNKI2zKWQW7XOc8fNRLuPQ3A9J36of8Ror5LrPpW
Bny7DWyWBO/cXpdwEwDDADvKteoeKBgfvcCRTQ==</SignatureValue><KeyInfo><X509Data><X509Certificate>MIIGrDCCBZSgAwIBAgITGgABFhwGtarSCQSJDQACAAEWHDANBgkqhkiG9w0BAQsFADBNMRUwEwYK
CZImiZPyLGQBGRYFbG9jYWwxFDASBgoJkiaJk/IsZAEZFgR0ZXN0MR4wHAYDVQQDExVEMjYgSXNz
dWluZyBDQSBJbnRlcm4wHhcNMTgxMDA0MTE1NzM4WhcNMjAxMDA0MTIwNzM4WjB7MRUwEwYKCZIm
iZPyLGQBGRYFbG9jYWwxFDASBgoJkiaJk/IsZAEZFgR0ZXN0MRgwFgYDVQQLEw9TZXJ2aWNlQWNj
b3VudHMxFTATBgNVBAsTDEFwcGxBY2NvdW50czEbMBkGA1UEAxMSc3J2c2VjdXJpdHktdG9rZW4t
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgNJFZOIhgyDAId3EVQzyWR7qVd3LbdWi
UuFgjFuVhAtQDmQqVq9b7esMHJ04jClViTIOFvoHrqZCBZloBt095AKdSLh4eKCFlxyYC0kMWNSk
r4XZC30ymgjlAlm9b4W+bTv65W58jUTd6KnM249TtmDKu3LklTD9p2tFHXiruqCeSmd6K39GkXLo
5P/Z43RLh/+UAHslvEHqswM4j2SfXhi0S0fLUjfBKC0QUwLVWZteRh5dA9E/fXbjCOrJo0Ru2GUd
c4cZUcdW1/hwb4HZEG0KqMGPH7BNc6UiquOV8KpnfozVH6OwRSODZdX0dhoafIZf02n203hYjIMK
prCKswIDAQABo4IDVTCCA1EwHQYDVR0OBBYEFFueBi2X1pS/b4XM48l3LD9vh3RVMB8GA1UdIwQY
MBaAFP2TzQOCJYfDHa+D9DnNVEgGPLq1MIIBGwYDVR0fBIIBEjCCAQ4wggEKoIIBBqCCAQKGgcRs
ZGFwOi8vL2NuPUQyNiUyMElzc3VpbmclMjBDQSUyMEludGVybixDTj1EMjZEUlZXMDUxLENOPUNE
UCxDTj1QdWJsaWMlMjBrZXklMjBTZXJ2aWNlcyxDTj1TZXJ2aWNlcyxDTj1Db25maWd1cmF0aW9u
LERDPXRlc3QsREM9bG9jYWw/Y2VydGlmaWNhdGVSZXZvY2F0aW9uTGlzdD9iYXNlP29iamVjdENs
YXNzPWNSTERpc3RyaWJ1dGlvblBvaW50hjlodHRwOi8vY3JsLnRlc3QubG9jYWwvY3JsL0QyNiUy
MElzc3VpbmclMjBDQSUyMEludGVybi5jcmwwggFXBggrBgEFBQcBAQSCAUkwggFFMIG5BggrBgEF
BQcwAoaBrGxkYXA6Ly8vY249RDI2JTIwSXNzdWluZyUyMENBJTIwSW50ZXJuLENOPUFJQSxDTj1Q
dWJsaWMlMjBrZXklMjBTZXJ2aWNlcyxDTj1TZXJ2aWNlcyxDTj1Db25maWd1cmF0aW9uLERDPXRl
c3QsREM9bG9jYWw/Y0FDZXJ0aWZpY2F0ZT9iYXNlP29iamVjdENsYXNzPWNlcnRpZmljYXRpb25B
dXRob3JpdHkwJwYIKwYBBQUHMAGGG2h0dHA6Ly9vY3NwLnRlc3QubG9jYWwvb2NzcDBeBggrBgEF
BQcwAoZSaHR0cDovL2NybC50ZXN0LmxvY2FsL2NybC9EMjZEUlZXMDUxLnRlc3QubG9jYWxfRDI2
JTIwSXNzdWluZyUyMENBJTIwSW50ZXJuKDIpLmNydDAOBgNVHQ8BAf8EBAMCBaAwPQYJKwYBBAGC
NxUHBDAwLgYmKwYBBAGCNxUIgvz6UobS2kiD2ZU8hPqUcoKd1lISh/PfK4X3uWwCAWQCAQIwHQYD
VR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMCcGCSsGAQQBgjcVCgQaMBgwCgYIKwYBBQUHAwEw
CgYIKwYBBQUHAwIwDQYJKoZIhvcNAQELBQADggEBAOV843NuGXSphLD+2pWc0mOHsiXISar1zTZP
JEkiGw/jErvVEhPhB4MK6PMMKreYgJCbAiG6ElBJNWaORfTHsldTgwA5mqYok3Ul06rl2nt0KuJg
OJ5Es/7G6p+yTxzmZJ6fs7cErmr0OlOG+hVYNm/hmiqcBql7Zdn5BmooNE8/Xs97a3/62Z9IDD36
uJWv1INbjIPNxv5sfG9RwdKTBKdJddHLUlQwKHQWu2jmy+zWsimny5qy5ENz5Gz7GhgD+zXz083/
SIduaJbZyi/guigSFPLxzVi5r/JXVa3X25vbnyg29nMozy7WQfejaefjfcrtwBZjPhi61R6Etiy9
UwY=</X509Certificate><X509IssuerSerial><X509IssuerName>CN=D26 Issuing CA Intern, DC=test, DC=local</X509IssuerName><X509SerialNumber>579819744832699429546922184522327667301750300</X509SerialNumber></X509IssuerSerial></X509Data></KeyInfo></Signature><saml2:Subject><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">srvsecurity-token-</saml2:NameID><saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:bearer"><saml2:SubjectConfirmationData NotBefore="2019-05-14T07:47:06.255Z" NotOnOrAfter="2019-05-14T08:47:06.255Z"/></saml2:SubjectConfirmation></saml2:Subject><saml2:Conditions NotBefore="2019-05-14T07:47:06.255Z" NotOnOrAfter="2019-05-14T08:47:06.255Z"/><saml2:AttributeStatement><saml2:Attribute Name="identType" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>Systemressurs</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="authenticationLevel" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>0</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="consumerId" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>srvsecurity-token-</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion>
    """

internal fun getAlteredSamlToken() =
    """<saml2:Assertion xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" ID="SAML-4161a46a-ebc3-403f-9d3d-4eff65a070ae" IssueInstant="2019-05-14T07:47:06.255Z" Version="2.0"><saml2:Issuer>IS02</saml2:Issuer><Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/><SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/><Reference URI="#SAML-4161a46a-ebc3-403f-9d3d-4eff65a070ae"><Transforms><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/><Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/></Transforms><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><DigestValue>+JCjyWxegtHXJKHTkHPlEegluxc=</DigestValue></Reference></SignedInfo><SignatureValue>CnpPln7EcTsBR7nIT8EYLkWIwoT5GlRWcZCO5d2mwBqEmGNKd3lF2luLBjHJEhgCufgQCUivcwmP
u4tFDA5Sa8rqoCuyrFNi7UVEYrWWrUwK72vTCX25tnGUgplsukxH/YO2V9NVAKTDQg+sgQ7IpvTS
9OeQ+dtI0ezIOq+KkjC5auqQw49dyHrjlDhmYU9QjdGtvq0hegp/ksIms3ehNamzDoq+NxEA6BKC
tDnK84naoirZJjgqLIJjUa2QjetwKdr5jPt7JNKI2zKWQW7XOc8fNRLuPQ3A9J36of8Ror5LrPpW
Bny7DWyWBO/cXpdwEwDDADvKteoeKBgfvcCRTQ==</SignatureValue><KeyInfo><X509Data><X509Certificate>MIIGrDCCBZSgAwIBAgITGgABFhwGtarSCQSJDQACAAEWHDANBgkqhkiG9w0BAQsFADBNMRUwEwYK
CZImiZPyLGQBGRYFbG9jYWwxFDASBgoJkiaJk/IsZAEZFgR0ZXN0MR4wHAYDVQQDExVEMjYgSXNz
dWluZyBDQSBJbnRlcm4wHhcNMTgxMDA0MTE1NzM4WhcNMjAxMDA0MTIwNzM4WjB7MRUwEwYKCZIm
iZPyLGQBGRYFbG9jYWwxFDASBgoJkiaJk/IsZAEZFgR0ZXN0MRgwFgYDVQQLEw9TZXJ2aWNlQWNj
b3VudHMxFTATBgNVBAsTDEFwcGxBY2NvdW50czEbMBkGA1UEAxMSc3J2c2VjdXJpdHktdG9rZW4t
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgNJFZOIhgyDAId3EVQzyWR7qVd3LbdWi
UuFgjFuVhAtQDmQqVq9b7esMHJ04jClViTIOFvoHrqZCBZloBt095AKdSLh4eKCFlxyYC0kMWNSk
r4XZC30ymgjlAlm9b4W+bTv65W58jUTd6KnM249TtmDKu3LklTD9p2tFHXiruqCeSmd6K39GkXLo
5P/Z43RLh/+UAHslvEHqswM4j2SfXhi0S0fLUjfBKC0QUwLVWZteRh5dA9E/fXbjCOrJo0Ru2GUd
c4cZUcdW1/hwb4HZEG0KqMGPH7BNc6UiquOV8KpnfozVH6OwRSODZdX0dhoafIZf02n203hYjIMK
prCKswIDAQABo4IDVTCCA1EwHQYDVR0OBBYEFFueBi2X1pS/b4XM48l3LD9vh3RVMB8GA1UdIwQY
MBaAFP2TzQOCJYfDHa+D9DnNVEgGPLq1MIIBGwYDVR0fBIIBEjCCAQ4wggEKoIIBBqCCAQKGgcRs
ZGFwOi8vL2NuPUQyNiUyMElzc3VpbmclMjBDQSUyMEludGVybixDTj1EMjZEUlZXMDUxLENOPUNE
UCxDTj1QdWJsaWMlMjBrZXklMjBTZXJ2aWNlcyxDTj1TZXJ2aWNlcyxDTj1Db25maWd1cmF0aW9u
LERDPXRlc3QsREM9bG9jYWw/Y2VydGlmaWNhdGVSZXZvY2F0aW9uTGlzdD9iYXNlP29iamVjdENs
YXNzPWNSTERpc3RyaWJ1dGlvblBvaW50hjlodHRwOi8vY3JsLnRlc3QubG9jYWwvY3JsL0QyNiUy
MElzc3VpbmclMjBDQSUyMEludGVybi5jcmwwggFXBggrBgEFBQcBAQSCAUkwggFFMIG5BggrBgEF
BQcwAoaBrGxkYXA6Ly8vY249RDI2JTIwSXNzdWluZyUyMENBJTIwSW50ZXJuLENOPUFJQSxDTj1Q
dWJsaWMlMjBrZXklMjBTZXJ2aWNlcyxDTj1TZXJ2aWNlcyxDTj1Db25maWd1cmF0aW9uLERDPXRl
c3QsREM9bG9jYWw/Y0FDZXJ0aWZpY2F0ZT9iYXNlP29iamVjdENsYXNzPWNlcnRpZmljYXRpb25B
dXRob3JpdHkwJwYIKwYBBQUHMAGGG2h0dHA6Ly9vY3NwLnRlc3QubG9jYWwvb2NzcDBeBggrBgEF
BQcwAoZSaHR0cDovL2NybC50ZXN0LmxvY2FsL2NybC9EMjZEUlZXMDUxLnRlc3QubG9jYWxfRDI2
JTIwSXNzdWluZyUyMENBJTIwSW50ZXJuKDIpLmNydDAOBgNVHQ8BAf8EBAMCBaAwPQYJKwYBBAGC
NxUHBDAwLgYmKwYBBAGCNxUIgvz6UobS2kiD2ZU8hPqUcoKd1lISh/PfK4X3uWwCAWQCAQIwHQYD
VR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMCcGCSsGAQQBgjcVCgQaMBgwCgYIKwYBBQUHAwEw
CgYIKwYBBQUHAwIwDQYJKoZIhvcNAQELBQADggEBAOV843NuGXSphLD+2pWc0mOHsiXISar1zTZP
JEkiGw/jErvVEhPhB4MK6PMMKreYgJCbAiG6ElBJNWaORfTHsldTgwA5mqYok3Ul06rl2nt0KuJg
OJ5Es/7G6p+yTxzmZJ6fs7cErmr0OlOG+hVYNm/hmiqcBql7Zdn5BmooNE8/Xs97a3/62Z9IDD36
uJWv1INbjIPNxv5sfG9RwdKTBKdJddHLUlQwKHQWu2jmy+zWsimny5qy5ENz5Gz7GhgD+zXz083/
SIduaJbZyi/guigSFPLxzVi5r/JXVa3X25vbnyg29nMozy7WQfejaefjfcrtwBZjPhi61R6Etiy9
UwY=</X509Certificate><X509IssuerSerial><X509IssuerName>CN=D26 Issuing CA Intern, DC=test, DC=local</X509IssuerName><X509SerialNumber>579819744832699429546922184522327667301750300</X509SerialNumber></X509IssuerSerial></X509Data></KeyInfo></Signature><saml2:Subject><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">srvsecurity-token-tull</saml2:NameID><saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:bearer"><saml2:SubjectConfirmationData NotBefore="2019-05-14T07:47:06.255Z" NotOnOrAfter="2019-05-14T08:47:06.255Z"/></saml2:SubjectConfirmation></saml2:Subject><saml2:Conditions NotBefore="2019-05-14T07:47:06.255Z" NotOnOrAfter="2019-05-14T08:47:06.255Z"/><saml2:AttributeStatement><saml2:Attribute Name="identType" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>Systemressurs</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="authenticationLevel" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>0</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="consumerId" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>srvsecurity-token-</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion>
    """

// hentet fra IDA.adeo.no med user Z991643
internal fun getIDASelvutstedtSaml() =
    """<saml2:Assertion xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" xmlns:xs="http://www.w3.org/2001/XMLSchema" ID="1231231231231231321" IssueInstant="2018-05-30T09:58:18.472Z" Version="2.0"><saml2:Issuer>IDA</saml2:Issuer><ds:Signature xmlns:ds="http://www.w3.org/2000/09/xmldsig#"><ds:SignedInfo><ds:CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/><ds:SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/><ds:Reference URI="#1231231231231231321"><ds:Transforms><ds:Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/><ds:Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"><ec:InclusiveNamespaces xmlns:ec="http://www.w3.org/2001/10/xml-exc-c14n#" PrefixList="xs"/></ds:Transform></ds:Transforms><ds:DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><ds:DigestValue>qb5F+u4X3Sb1O4LANJanzr7f6HE=</ds:DigestValue></ds:Reference></ds:SignedInfo><ds:SignatureValue>dSL8ydbFrzJ0z29DEjqBlVL+Jkj1PTwN/DF6Yeg7QhpLiP6C2aN++81WBTg/AlYNjgOJJAK8ucc5YXp/a3WF/ZtEyUv42xPX25Bpif4sMcOaI9KpvLtYz7wqTSZf7zyaCDSPDknFUnKaWXWnzSkupLOA1Ihw09bmTa28CkZZ9qA0+kHf05GmuP/xK8D2Y72zVEsTGrfAmz7Qm1T3VxXH4nuvSImLTsmyapcPHsCdZlterAAqd5o8JIMu4CXyxp31PY7dqdFaKPGRm7BhDZYJMpBm7BH9AtcUPkREnx9TlzDDc4BHWVdUJ8rREiTeIz9e399QHCVx20UoWEG+Uw8vtw==</ds:SignatureValue><ds:KeyInfo><ds:X509Data><ds:X509Certificate>MIIGgDCCBWigAwIBAgIKFpJcigAAAAAU2DANBgkqhkiG9w0BAQUFADBNMRUwEwYKCZImiZPyLGQB
GRYFbG9jYWwxFDASBgoJkiaJk/IsZAEZFgR0ZXN0MR4wHAYDVQQDExVEMjYgSXNzdWluZyBDQSBJ
bnRlcm4wHhcNMTUwNjMwMTA0MDIwWhcNMTkwNjI5MTA0MDIwWjBbMQswCQYDVQQGEwJOTzENMAsG
A1UECBMET3NsbzENMAsGA1UEBxMET3NsbzEMMAoGA1UEChMDTkFWMSAwHgYDVQQDExdkMjZqYnNs
MDA3MjEudGVzdC5sb2NhbDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALOacZuG0TP5
pnN+vs1iPjzKUNxEVZErkF85hiBpmkLPaPEjf0DQKA24I6c8Zwm6DYehrKu7kdIqnLDVo+8svI8u
aDlVfDCHHvwS/rq35CmdR4gtQAqpAojNvlhNtz7rrbiCrw4s3WwZ3ydSEoxF4/YpD01c7DrCXcBo
Lw1HNPhV7vareEtxDc6XTFWnenJO6iDo3xpBmHQEhwNJlglMk/fsYFFL8A67Ns4f8RyD4/5SQwf6
6vMmz9ThkvvAi4NVXNT3LCA527GCo7AWA//DIMNRBfQKKmRMkjf779GtXshl4xAtOeAJnpr1D9nG
iY5cbMdiKJwsT1fInaH2HxEUCrECAwEAAaOCA1IwggNOMB0GA1UdDgQWBBQ5lK5HAV0iC4dnCOLW
ub+L9UhqFDAfBgNVHSMEGDAWgBT9k80DgiWHwx2vg/Q5zVRIBjy6tTCCARsGA1UdHwSCARIwggEO
MIIBCqCCAQagggEChoHEbGRhcDovLy9jbj1EMjYlMjBJc3N1aW5nJTIwQ0ElMjBJbnRlcm4sQ049
RDI2RFJWVzAwNCxDTj1DRFAsQ049UHVibGljJTIwa2V5JTIwU2VydmljZXMsQ049U2VydmljZXMs
Q049Q29uZmlndXJhdGlvbixEQz10ZXN0LERDPWxvY2FsP2NlcnRpZmljYXRlUmV2b2NhdGlvbkxp
c3Q/YmFzZT9vYmplY3RDbGFzcz1jUkxEaXN0cmlidXRpb25Qb2ludIY5aHR0cDovL2NybC50ZXN0
LmxvY2FsL0NybC9EMjYlMjBJc3N1aW5nJTIwQ0ElMjBJbnRlcm4uY3JsMIIBVAYIKwYBBQUHAQEE
ggFGMIIBQjCBuQYIKwYBBQUHMAKGgaxsZGFwOi8vL2NuPUQyNiUyMElzc3VpbmclMjBDQSUyMElu
dGVybixDTj1BSUEsQ049UHVibGljJTIwa2V5JTIwU2VydmljZXMsQ049U2VydmljZXMsQ049Q29u
ZmlndXJhdGlvbixEQz10ZXN0LERDPWxvY2FsP2NBQ2VydGlmaWNhdGU/YmFzZT9vYmplY3RDbGFz
cz1jZXJ0aWZpY2F0aW9uQXV0aG9yaXR5MCcGCCsGAQUFBzABhhtodHRwOi8vb2NzcC50ZXN0Lmxv
Y2FsL29jc3AwWwYIKwYBBQUHMAKGT2h0dHA6Ly9jcmwudGVzdC5sb2NhbC9DcmwvRDI2RFJWVzAw
NC50ZXN0LmxvY2FsX0QyNiUyMElzc3VpbmclMjBDQSUyMEludGVybi5jcnQwDgYDVR0PAQH/BAQD
AgWgMD0GCSsGAQQBgjcVBwQwMC4GJisGAQQBgjcVCIL8+lKG0tpIg9mVPIT6lHKCndZSEofz3yuF
97lsAgFkAgECMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjAnBgkrBgEEAYI3FQoEGjAY
MAoGCCsGAQUFBwMBMAoGCCsGAQUFBwMCMA0GCSqGSIb3DQEBBQUAA4IBAQDgaobrUxPRc5z862sR
WEw/IBCXAohrSIiJSSEs7hPoefa4tvNTq5mqwyFnIdgBcTOdxB5dyQELvGQ61GqOxSy58WMSOOVS
Sr9GC7NzRAJmZU4CK9DgsgPYVYR1Nj/3QjXTTAuYj+GTGAg6lIBwWRV5nU7U0ya9ZNZdc1VBPzou
TPGTz8hbZsbzYd1Lz1cGGITPyF8t9Wy6Vp9Nhd6HcXJoH/BULbknuiJz6Vov7i67EkB9dx5FNM8Z
Fo4tglR0sqL/Vc7Cwk1M33D+HVHqZvT87mq1IEE+WMELyrUYL4fcnOI2vWKaHbP59CZcUd5v9xBO
dBFRIuZnkBIF4/GgR30D</ds:X509Certificate></ds:X509Data></ds:KeyInfo></ds:Signature><saml2:Subject><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified" NameQualifier="www.nav.no">Z991643</saml2:NameID><saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:bearer"/></saml2:Subject><saml2:Conditions NotBefore="2018-05-30T09:58:18.472Z" NotOnOrAfter="2018-06-06T09:58:18.472Z"/><saml2:AttributeStatement><saml2:Attribute Name="authenticationLevel" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">4</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="identType" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">InternBruker</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="consumerId" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">srvPDP</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion>
    """

internal fun getAlteredSamlTokenWithInternBrukerOgAuthLevel() =
    """<saml2:Assertion xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" ID="SAML-37f786ad-d917-4363-bd85-5be58db389f6" IssueInstant="2018-05-07T09:21:56Z" Version="2.0"><saml2:Issuer>IS02</saml2:Issuer><Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
<SignedInfo>
  <CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
  <SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/>
  <Reference URI="#SAML-37f786ad-d917-4363-bd85-5be58db389f6">
    <Transforms>
      <Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/>
      <Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
    </Transforms>
    <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
    <DigestValue>G7qp1Nlst6vtkNLBXsATSVJOvjI=</DigestValue>
  </Reference>
</SignedInfo>
    <SignatureValue>kIIqG95RuphFMUa7y9BMt9Uu84/k62TjWJguy/lS15c7y1kCsaBkCQWAfqFoUVndNfQzkFii5DXfjblEL8EJ6PnkyDZ3CYgf+jTqiQDMe6clKmpc09FiZqgZAY2zU2JyV41IjxdWDdba81DwMgFG7IUwtbC3IlQ1ALz+PmLEXKf/QePDCNA6M29Im/12AXcx9+THIs7vNTC/1Vv107j4OYH0OjvK159pbBsLwoiSudjBlef5p5CcHJvzNYzH+HlL/og7bJJfpWnD1xeImH9AtVLttdUzMuitXXxDkzL4iDhRDIaoqdcid+B7yfxyolveT2rm7vY8BXAC1GmzLrQd5g==</SignatureValue><KeyInfo><X509Data><X509Certificate>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</X509Certificate><X509IssuerSerial><X509IssuerName>CN=D26 Issuing CA Intern, DC=test, DC=local</X509IssuerName><X509SerialNumber>579819418258013541424141683728872668728926316</X509SerialNumber></X509IssuerSerial></X509Data></KeyInfo></Signature><saml2:Subject><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">srvPDPtull</saml2:NameID><saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:bearer"><saml2:SubjectConfirmationData NotBefore="2018-05-07T09:21:53Z" NotOnOrAfter="2018-05-07T10:21:59Z"/></saml2:SubjectConfirmation></saml2:Subject><saml2:Conditions NotBefore="2018-05-07T09:21:53Z" NotOnOrAfter="2018-05-07T10:21:59Z"/><saml2:AttributeStatement><saml2:Attribute Name="identType" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>${IdentType.INTERNBRUKER.value}</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="authenticationLevel" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>3</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="consumerId" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>srvPDP</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion>
    """

internal fun getAlteredSamlTokenWithEksternBrukerOgAuthLevel() =
    """<saml2:Assertion xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" ID="SAML-37f786ad-d917-4363-bd85-5be58db389f6" IssueInstant="2018-05-07T09:21:56Z" Version="2.0"><saml2:Issuer>IS02</saml2:Issuer><Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
<SignedInfo>
  <CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
  <SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/>
  <Reference URI="#SAML-37f786ad-d917-4363-bd85-5be58db389f6">
    <Transforms>
      <Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/>
      <Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
    </Transforms>
    <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
    <DigestValue>G7qp1Nlst6vtkNLBXsATSVJOvjI=</DigestValue>
  </Reference>
</SignedInfo>
    <SignatureValue>kIIqG95RuphFMUa7y9BMt9Uu84/k62TjWJguy/lS15c7y1kCsaBkCQWAfqFoUVndNfQzkFii5DXfjblEL8EJ6PnkyDZ3CYgf+jTqiQDMe6clKmpc09FiZqgZAY2zU2JyV41IjxdWDdba81DwMgFG7IUwtbC3IlQ1ALz+PmLEXKf/QePDCNA6M29Im/12AXcx9+THIs7vNTC/1Vv107j4OYH0OjvK159pbBsLwoiSudjBlef5p5CcHJvzNYzH+HlL/og7bJJfpWnD1xeImH9AtVLttdUzMuitXXxDkzL4iDhRDIaoqdcid+B7yfxyolveT2rm7vY8BXAC1GmzLrQd5g==</SignatureValue><KeyInfo><X509Data><X509Certificate>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</X509Certificate><X509IssuerSerial><X509IssuerName>CN=D26 Issuing CA Intern, DC=test, DC=local</X509IssuerName><X509SerialNumber>579819418258013541424141683728872668728926316</X509SerialNumber></X509IssuerSerial></X509Data></KeyInfo></Signature><saml2:Subject><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">srvPDPtull</saml2:NameID><saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:bearer"><saml2:SubjectConfirmationData NotBefore="2018-05-07T09:21:53Z" NotOnOrAfter="2018-05-07T10:21:59Z"/></saml2:SubjectConfirmation></saml2:Subject><saml2:Conditions NotBefore="2018-05-07T09:21:53Z" NotOnOrAfter="2018-05-07T10:21:59Z"/><saml2:AttributeStatement><saml2:Attribute Name="identType" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>${IdentType.EKSTERNBRUKER.value}</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="authenticationLevel" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>3</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="consumerId" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>srvPDP</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion>
    """

internal fun getDpSamlToken() =
    """<saml2:Assertion xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" ID="SAML-24ea1481-affc-45aa-955c-44e0b233909c" IssueInstant="2018-10-24T08:58:36Z" Version="2.0"><saml2:Issuer>IS02</saml2:Issuer><Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
<SignedInfo>
  <CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
  <SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/>
  <Reference URI="#SAML-24ea1481-affc-45aa-955c-44e0b233909c">
    <Transforms>
      <Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/>
      <Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
    </Transforms>
    <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
    <DigestValue>40s+hZjwPxm3x13JDwYrRev6vXw=</DigestValue>
  </Reference>
</SignedInfo>
    <SignatureValue>V8KPgIBUUvuFzcHXRmIwzArOKl/LqA6qsFdLLBgHH4azfXEKLPFf1jRXXtY4GpmUz68cvIerR9eXgrfFNHzfpAUSirczFcjskP+MldNuKNPp1Kl8+x1TAcehfsTL91F7AX3OvPYwL5P7kcdhTK6ytvmm86jLvUYJU0/gkrgsftnGBuOAnOeGycblo7Xj2mKA2PKnbNehwOvO4LvZVkaKSlyk5BLigUK33K+igskUOUG8ysJqwg8LE9z45kOfh2li6fyr0T+s+rjZ8qKnDrtvnk4ncqk+EW/gi6FM/+AJsYg9iptmzwTeqVWGHZIdw3e4JFT+54SbL88oq/1urEIqOA==</SignatureValue><KeyInfo><X509Data><X509Certificate>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</X509Certificate><X509IssuerSerial><X509IssuerName>CN=D26 Issuing CA Intern, DC=test, DC=local</X509IssuerName><X509SerialNumber>579819418258013541424141683728872668728926316</X509SerialNumber></X509IssuerSerial></X509Data></KeyInfo></Signature><saml2:Subject><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">srvsecurity-token-</saml2:NameID><saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:bearer"><saml2:SubjectConfirmationData NotBefore="2018-10-24T08:58:33Z" NotOnOrAfter="2018-10-24T09:58:39Z"/></saml2:SubjectConfirmation></saml2:Subject><saml2:Conditions NotBefore="2018-10-24T08:58:33Z" NotOnOrAfter="2018-10-24T09:58:39Z"/><saml2:AttributeStatement><saml2:Attribute Name="identType" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>Systemressurs</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="authenticationLevel" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>0</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="consumerId" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>srvsecurity-token-</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion>
    """

internal fun getOpenAmAndDPSamlExchangePair(): List<String> {
    // hentet med getNewOpenAmAndDPSamlExchangePair
    val oidcToken = "eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICI0MkJmb1JzWjNlaU9fa1pka19yS013IiwgInN1YiI6ICJhZ2VudGFkbWluIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICI0NWVkYmYwZS05NmIxLTRkODUtYWFlOC0xMzNmZDVlNmYzOGMtNzU3MDE3IiwgImlzcyI6ICJodHRwczovL2lzc28tdC5hZGVvLm5vOjQ0My9pc3NvL29hdXRoMiIsICJ0b2tlbk5hbWUiOiAiaWRfdG9rZW4iLCAiYXVkIjogImZyZWctdG9rZW4tcHJvdmlkZXItdDAiLCAiY19oYXNoIjogIkRCZmN5SVdTbW8teDFVRGhzZWctZXciLCAib3JnLmZvcmdlcm9jay5vcGVuaWRjb25uZWN0Lm9wcyI6ICJhZGI1NGE3Ni02Njk2LTQxMzAtYWI2ZC00NjJkYjA2NTBjZjYiLCAiYXpwIjogImZyZWctdG9rZW4tcHJvdmlkZXItdDAiLCAiYXV0aF90aW1lIjogMTUzOTg0NzM1OCwgInJlYWxtIjogIi8iLCAiZXhwIjogMTUzOTg1MDk1OCwgInRva2VuVHlwZSI6ICJKV1RUb2tlbiIsICJpYXQiOiAxNTM5ODQ3MzU4IH0.BnNaq7_NlgWrTX7q4YKQmGMPyyt2aPfB8frMeZY9tgGyyAX7bORW57PgBpa5-Aou6uQ7mzuF05NW8RucbzsuSn3UC0YqMV-0a8uhm_KNPAIJc_dzYHcDO6fbnNvaHMYf1DKQhK6Ri6kP2twVrjcB1q1Xrj2rIMVaTrUcckj9YWKH2nGKBvL_E9QGGnkdOJubUG_mFn7eSEcWO0GX0_1YmDEZmpt1n3ykQNlPg9R-D9vH2OUammNAz3rqtsSM2gwM-uT8fZiTj0x4gswp5rXLFc_-bIOU2H5Ocmt2DGr7nzLqZCmoIaRdIJ1jxfGYv7BnyqNEFG8AyEDvIufia-BYig"
    val samlToken =
        """<saml2:Assertion xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" ID="SAML-f2585705-8f3f-43ef-8725-17bfa50cfae9" IssueInstant="2018-10-18T07:27:32Z" Version="2.0"><saml2:Issuer>IS02</saml2:Issuer><Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
<SignedInfo>
  <CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
  <SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/>
  <Reference URI="#SAML-f2585705-8f3f-43ef-8725-17bfa50cfae9">
    <Transforms>
      <Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/>
      <Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
    </Transforms>
    <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
    <DigestValue>Jnjpj3nFYKczF+IEZCveIzCRoC0=</DigestValue>
  </Reference>
</SignedInfo>
    <SignatureValue>DozyiwgtFSWo89DrIw90y5+8oEZOGfgMYitQBQHldxBYmvFQBOdFkCCgUAsdwT5B3d2dFpFKjZupN72eyfqBGF2PGwKfynGC7678KGX4/+Bw8UBLXlXZSzAXpaBPDRqWSa4ugdZUSSGW0Eqk+ctaEzFe8Pt8kFtBqQf23O5+ojJ/r65HmwgkjrD/OnCcQ2vq/lxbaBOh+djq33G3Hb09sK1SBxeh0q2GZLjybYUqVKM5E0QR2JXatx/HyOOhjWqMO078PPsjmzuBs559Y5XyccluxhsBIqLIUb03GYznnrxhXQWNc2UKOWpat3LWuCXYP4IEIc1K2ldtGtkLbPY0Eg==</SignatureValue><KeyInfo><X509Data><X509Certificate>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</X509Certificate><X509IssuerSerial><X509IssuerName>CN=D26 Issuing CA Intern, DC=test, DC=local</X509IssuerName><X509SerialNumber>579819418258013541424141683728872668728926316</X509SerialNumber></X509IssuerSerial></X509Data></KeyInfo></Signature><saml2:Subject><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">agentadmin</saml2:NameID><saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:bearer"><saml2:SubjectConfirmationData NotBefore="2018-10-18T07:27:29Z" NotOnOrAfter="2018-10-18T08:22:38Z"/></saml2:SubjectConfirmation></saml2:Subject><saml2:Conditions NotBefore="2018-10-18T07:27:29Z" NotOnOrAfter="2018-10-18T08:22:38Z"/><saml2:AuthnStatement AuthnInstant="2018-10-18T07:27:32Z" SessionNotOnOrAfter="2018-10-18T08:22:38Z"><saml2:SubjectLocality Address="10.33.46.97"/><saml2:AuthnContext><saml2:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified</saml2:AuthnContextClassRef></saml2:AuthnContext></saml2:AuthnStatement><saml2:AttributeStatement><saml2:Attribute Name="identType" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified"><saml2:AttributeValue>InternBruker</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="authenticationLevel" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified"><saml2:AttributeValue>4</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="consumerId" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified"><saml2:AttributeValue>srvsecurity-token-</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="identType" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified"><saml2:AttributeValue>InternBruker</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="authenticationLevel" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified"><saml2:AttributeValue>4</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="consumerId" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified"><saml2:AttributeValue>srvsecurity-token-</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="auditTrackingId" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified"><saml2:AttributeValue>45edbf0e-96b1-4d85-aae8-133fd5e6f38c-757017</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion>
        """
    val l: MutableList<String> = ArrayList()
    l.add(oidcToken)
    l.add(samlToken)
    return l
}
