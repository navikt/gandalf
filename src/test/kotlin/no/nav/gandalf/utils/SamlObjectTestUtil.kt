package no.nav.gandalf.utils

internal fun getSamlToken() = """<saml2:Assertion xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" ID="SAML-4161a46a-ebc3-403f-9d3d-4eff65a070ae" IssueInstant="2019-05-14T07:47:06.255Z" Version="2.0"><saml2:Issuer>IS02</saml2:Issuer><Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/><SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/><Reference URI="#SAML-4161a46a-ebc3-403f-9d3d-4eff65a070ae"><Transforms><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/><Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/></Transforms><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><DigestValue>+JCjyWxegtHXJKHTkHPlEegluxc=</DigestValue></Reference></SignedInfo><SignatureValue>CnpPln7EcTsBR7nIT8EYLkWIwoT5GlRWcZCO5d2mwBqEmGNKd3lF2luLBjHJEhgCufgQCUivcwmP
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
UwY=</X509Certificate><X509IssuerSerial><X509IssuerName>CN=D26 Issuing CA Intern, DC=test, DC=local</X509IssuerName><X509SerialNumber>579819744832699429546922184522327667301750300</X509SerialNumber></X509IssuerSerial></X509Data></KeyInfo></Signature><saml2:Subject><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">srvsecurity-token-</saml2:NameID><saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:bearer"><saml2:SubjectConfirmationData NotBefore="2019-05-14T07:47:06.255Z" NotOnOrAfter="2019-05-14T08:47:06.255Z"/></saml2:SubjectConfirmation></saml2:Subject><saml2:Conditions NotBefore="2019-05-14T07:47:06.255Z" NotOnOrAfter="2019-05-14T08:47:06.255Z"/><saml2:AttributeStatement><saml2:Attribute Name="identType" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>Systemressurs</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="authenticationLevel" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>0</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="consumerId" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>srvsecurity-token-</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion>"""

internal fun getAlteredSamlToken() = """<saml2:Assertion xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" ID="SAML-4161a46a-ebc3-403f-9d3d-4eff65a070ae" IssueInstant="2019-05-14T07:47:06.255Z" Version="2.0"><saml2:Issuer>IS02</saml2:Issuer><Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/><SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/><Reference URI="#SAML-4161a46a-ebc3-403f-9d3d-4eff65a070ae"><Transforms><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/><Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/></Transforms><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><DigestValue>+JCjyWxegtHXJKHTkHPlEegluxc=</DigestValue></Reference></SignedInfo><SignatureValue>CnpPln7EcTsBR7nIT8EYLkWIwoT5GlRWcZCO5d2mwBqEmGNKd3lF2luLBjHJEhgCufgQCUivcwmP
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
UwY=</X509Certificate><X509IssuerSerial><X509IssuerName>CN=D26 Issuing CA Intern, DC=test, DC=local</X509IssuerName><X509SerialNumber>579819744832699429546922184522327667301750300</X509SerialNumber></X509IssuerSerial></X509Data></KeyInfo></Signature><saml2:Subject><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">srvsecurity-token-tull</saml2:NameID><saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:bearer"><saml2:SubjectConfirmationData NotBefore="2019-05-14T07:47:06.255Z" NotOnOrAfter="2019-05-14T08:47:06.255Z"/></saml2:SubjectConfirmation></saml2:Subject><saml2:Conditions NotBefore="2019-05-14T07:47:06.255Z" NotOnOrAfter="2019-05-14T08:47:06.255Z"/><saml2:AttributeStatement><saml2:Attribute Name="identType" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>Systemressurs</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="authenticationLevel" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>0</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="consumerId" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"><saml2:AttributeValue>srvsecurity-token-</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion>"""