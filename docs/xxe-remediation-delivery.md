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
3. Ingen ekstern resolver kalles under test.
4. Eksisterende SOAP/SAML-utstedelse, veksling, signering og validering passerer.
5. Ikke-fragmenterte XMLDSig-referanser avvises.

## Verifikasjon etter deploy

- Følg `400`-rate og latenstid for WS-SAML-endepunktet.
- Sjekk at vellykkede WS-SAML-utstedelser og valideringer holder normalt nivå.
- Varsle sikkerhetsteamet om rettet versjon og testbevis; ikke inkluder payload eller miljødata i saken.

## Release handoff

- Før deploy: kjør `./gradlew ktlintCheck test` og lagre CI-lenken som testbevis.
- Etter deploy: følg WS-SAML `400`-rate, latenstid og vellykkede utstedelser/valideringer i den vanlige observasjonsperioden.
- Meld rettet versjon, CI-lenke og observasjonsresultat til sikkerhetsteamet uten å inkludere payload, callback-adresser eller miljødata.
