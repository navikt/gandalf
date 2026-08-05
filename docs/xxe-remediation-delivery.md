# XXE-remediation: leveransegrunnlag

Den detaljerte beslutningshistorikken ligger i [XXE-remediation-kartet](https://github.com/navikt/gandalf/issues/574).

## Rød sone — implementeres og gjennomgås manuelt

- `SecureXml`: JAXP-funksjoner, attributter og fail-closed-feilbehandling.
- XMLDSig `URIDereferencer`: kun lokale fragmentreferanser skal tillates.
- Migrering av XML-kall i `WSTrustRequest` og `SamlObject`.
- Fjerning av parserdetaljer fra HTTP-feilresponsen.

## Testkrav etter implementasjon

1. `DOCTYPE` avvises i `WSTrustRequest.read()` og `SamlObject.read()`.
2. `/rest/v1/sts/ws/samltoken` svarer `400` uten interne parserdetaljer.
3. `DOCTYPE`, ekstern generell entitet og ekstern parameterentitet avvises uten at resolveren kalles under test.
4. `TransformerFactory` avviser ekstern XSLT-import.
5. Eksisterende SOAP/SAML-utstedelse, veksling, signering og validering passerer, inkludert same-document XMLDSig-referanse.
6. Ikke-fragmenterte XMLDSig-referanser avvises.

## Verifikasjon etter deploy

- Merge til `master`; bare master-workflowen deployer til T4 og standard dev.
- Følg `400`-rate og latenstid for WS-SAML-endepunktet.
- Sjekk at vellykkede WS-SAML-utstedelser og valideringer holder normalt nivå.
- Test en gyldig WS-Trust-request med forventet `200`, og en ellers gyldig request med ufarlig `DOCTYPE` med forventet `400 Invalid XML request`.
- Godkjenn `production`-environmentet i GitHub Actions først etter verifikasjon i dev; prod-deployen er blokkert av denne gaten.
- Varsle sikkerhetsteamet om rettet versjon og testbevis; ikke inkluder payload eller miljødata i saken.

## Release handoff

- Før deploy: kjør `./gradlew ktlintCheck test` og lagre CI-lenken som testbevis. CI kjører `ktlintCheck` før testene.
- Lokal verifikasjon: `./gradlew bootJar && docker compose up --build`; Compose mount-er embedded LDAP-fixtures som lokalprofilen trenger.
- Etter deploy: følg WS-SAML `400`-rate, latenstid og vellykkede utstedelser/valideringer i den vanlige observasjonsperioden.
- Meld rettet versjon, CI-lenke og observasjonsresultat til sikkerhetsteamet uten å inkludere payload, callback-adresser eller miljødata.
