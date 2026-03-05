# Gandalf - Security Token Service (STS)
## Komplett Teknisk Dokumentasjon

---

## 📋 Innholdsfortegnelse
1. [Hva er Gandalf?](#hva-er-gandalf)
2. [Overordnet Arkitektur](#overordnet-arkitektur)
3. [Hovedkomponenter](#hovedkomponenter)
4. [API Endepunkter](#api-endepunkter)
5. [Token-flyt og Prosesser](#token-flyt-og-prosesser)
6. [Sikkerhet og Autentisering](#sikkerhet-og-autentisering)
7. [Teknisk Stack](#teknisk-stack)
8. [Deployering](#deployering)

---

## 🎯 Hva er Gandalf?

**Gandalf** er NAVs Security Token Service (STS) - en sentral sikkerhetskomponent som fungerer som en **"tillitsmegler"** (trust broker) mellom forskjellige systemer og applikasjoner.

### Hovedformål:
- ✅ **Utstede sikkerhetstokens** til systembrukere (service accounts)
- 🔄 **Konvertere mellom token-formater** (OIDC ↔ SAML)
- 🔐 **Validere og verifisere tokens**
- 🤝 **Etablere tillit mellom systemer** som ikke direkte stoler på hverandre

### Hvorfor trengs dette?
I NAVs infrastruktur finnes det både **legacy systemer** (bruker SAML) og **moderne systemer** (bruker OIDC/JWT). Gandalf fungerer som en bro mellom disse:

```
Legacy System (SAML) → Gandalf → Moderne System (OIDC)
      eller
Moderne System (OIDC) → Gandalf → Legacy System (SAML)
```

---

## 🏗️ Overordnet Arkitektur

```
┌─────────────────────────────────────────────────────────┐
│                    GANDALF STS                          │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │         REST API Controllers                     │  │
│  │  - AccessTokenController                         │  │
│  │  - TokenExchangeController                       │  │
│  │  - IdentityProviderController                    │  │
│  │  - ValidateController                            │  │
│  └──────────────────────────────────────────────────┘  │
│                       ↓                                 │
│  ┌──────────────────────────────────────────────────┐  │
│  │      AccessTokenIssuer (Kjernelogikk)           │  │
│  │  - issueToken()                                  │  │
│  │  - exchangeSamlToOidcToken()                     │  │
│  │  - exchangeOidcToSamlToken()                     │  │
│  │  - validateOidcToken()                           │  │
│  └──────────────────────────────────────────────────┘  │
│                       ↓                                 │
│  ┌──────────────────────────────────────────────────┐  │
│  │         Sikkerhet & Autentisering                │  │
│  │  - LDAP/Active Directory                         │  │
│  │  - Basic Auth                                    │  │
│  │  - JWT Signing & Validation                      │  │
│  └──────────────────────────────────────────────────┘  │
│                       ↓                                 │
│  ┌──────────────────────────────────────────────────┐  │
│  │           Data & Konfigurasjon                   │  │
│  │  - KeyStore (RSA keys)                           │  │
│  │  - Oracle Database                               │  │
│  │  - External Issuers (Azure, TokenX, etc)         │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 🧩 Hovedkomponenter

### 1. **AccessTokenIssuer** (Kjernelogikk)
**Fil:** `accesstoken/AccessTokenIssuer.kt`

Dette er hjertet av Gandalf. Inneholder all logikk for:

#### 🔹 Token-utstedelse:
```kotlin
fun issueToken(username: String): SignedJWT
```
- Tar inn systembruker (f.eks. `srvPDP`)
- Genererer et OIDC/JWT token signert med RSA
- Token inneholder: subject, issuer, audience, expiry, claims

#### 🔹 SAML → OIDC konvertering:
```kotlin
fun exchangeSamlToOidcToken(samlToken: String): SignedJWT
```
- Mottar SAML token (XML-format, Base64-kodet)
- Validerer SAML-signatur og utløpstid
- Ekstraherer identitetsinformasjon (nameID, authlevel, etc)
- Genererer nytt OIDC token med samme informasjon

#### 🔹 OIDC → SAML konvertering:
```kotlin
fun exchangeOidcToSamlToken(token: String, username: String): String
```
- Mottar OIDC/JWT token
- Validerer JWT-signatur mot kjente issuers (TokenX, Azure AD, etc)
- Konverterer til SAML XML-format
- Returnerer Base64-kodet SAML token

#### 🔹 Token-validering:
```kotlin
fun validateOidcToken(oidcToken: String): OidcObject
```
- Sjekker signatur mot public keys (JWKS)
- Validerer expiry, issuer, audience
- Støtter flere eksterne issuers (Azure, TokenX, Maskinporten)

---

### 2. **TokenExchangeController** (REST API)
**Fil:** `api/controllers/TokenExchangeController.kt`

Dette er REST API-en som eksterne systemer kaller.

#### Endepunkt: `POST /rest/v1/sts/token/exchange`

**Brukes for:**
- SAML til OIDC konvertering
- OIDC til SAML konvertering

**Request-eksempel (SAML → OIDC):**
```http
POST /rest/v1/sts/token/exchange
Authorization: Basic <base64-encoded-credentials>
Content-Type: application/x-www-form-urlencoded

grant_type=urn:ietf:params:oauth:grant-type:token-exchange
&subject_token_type=urn:ietf:params:oauth:token-type:saml2
&subject_token=<BASE64_ENCODED_SAML_TOKEN>
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIs...",
  "token_type": "Bearer",
  "expires_in": 3630
}
```

**Request-eksempel (OIDC → SAML):**
```http
POST /rest/v1/sts/token/exchange
Authorization: Basic <base64-encoded-credentials>
Content-Type: application/x-www-form-urlencoded

grant_type=urn:ietf:params:oauth:grant-type:token-exchange
&subject_token_type=urn:ietf:params:oauth:token-type:access_token
&requested_token_type=urn:ietf:params:oauth:token-type:saml2
&subject_token=<OIDC_JWT_TOKEN>
```

---

### 3. **AccessTokenController** (REST API)
**Fil:** `api/controllers/AccessTokenController.kt`

#### Endepunkt: `POST /rest/v1/sts/token`
**Brukes for:** Direkte utstedelse av OIDC tokens til systembrukere

**Request:**
```http
POST /rest/v1/sts/token
Authorization: Basic <srvUser:password>
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials&scope=openid
```

#### Endepunkt: `POST /rest/v1/sts/samltoken`
**Brukes for:** Direkte utstedelse av SAML tokens

---

### 4. **LDAP Authentication** (Sikkerhet)
**Filer:** 
- `ldap/LDAPAuthentication.kt`
- `ldap/CustomAuthenticationProvider.kt`
- `ldap/LDAPConnectionSetup.kt`

**Hva gjør den?**
- Kobler til NAVs on-prem Active Directory via LDAPS
- Validerer systembrukere (service accounts)
- Basic Authentication: brukernavn/passord sendes i Authorization header
- Søker i OU=ServiceAccounts og OU=ApplAccounts

**Flyt:**
```
1. Client sender Basic Auth header (base64: username:password)
2. Spring Security intercepter request
3. CustomAuthenticationProvider kalles
4. LDAPAuthentication kobler til LDAP
5. Prøver å binde (authenticate) med credentials
6. Hvis OK: Returnerer authenticated token
7. Hvis FAIL: 401 Unauthorized
```

---

### 5. **KeyStore Management**
**Filer:**
- `keystore/KeyStoreReader.kt`
- `service/RsaKeysProvider.kt`

**Hva gjør den?**
- Leser RSA private/public keys fra Java KeyStore (JKS)
- Brukes til å signere OIDC tokens (JWT)
- Publiserer public keys på `/jwks` endepunkt
- Roterer keys ved utløp

**JWKS (JSON Web Key Set):**
Ekstern systemer kan hente public keys fra:
```
GET https://security-token-service.dev.adeo.no/jwks
```
For å validere tokens utstedt av Gandalf.

---

### 6. **IdentityProviderController** (Metadata)
**Fil:** `api/controllers/IdentityProviderController.kt`

Publiserer standard OIDC metadata:

#### `GET /.well-known/openid-configuration`
Returnerer konfigurasjon som:
```json
{
  "issuer": "https://security-token-service.dev.adeo.no",
  "jwks_uri": "https://security-token-service.dev.adeo.no/jwks",
  "token_endpoint": "https://security-token-service.dev.adeo.no/rest/v1/sts/token",
  "subject_types_supported": ["public"],
  "response_types_supported": ["token"],
  "grant_types_supported": ["client_credentials", "urn:ietf:params:oauth:grant-type:token-exchange"]
}
```

---

### 7. **Database Repository**
**Fil:** `repository/ConsumerRepository.kt`

- Lagrer informasjon om consumers (klienter)
- Oracle database i produksjon
- H2 in-memory database for testing
- Brukes til å spore hvilke systemer som bruker STS

---

## 🔄 Token-flyt og Prosesser

### Scenario 1: Legacy system trenger OIDC token

```
┌──────────────┐                                ┌─────────────┐
│ Legacy App   │                                │   Gandalf   │
│  (har SAML)  │                                │     STS     │
└──────┬───────┘                                └──────┬──────┘
       │                                               │
       │ 1. POST /token/exchange                       │
       │    Auth: Basic srvUser:pass                   │
       │    subject_token=<SAML_TOKEN>                 │
       ├──────────────────────────────────────────────>│
       │                                               │
       │                                   2. Validerer SAML token
       │                                      - Sjekker signatur
       │                                      - Sjekker expiry
       │                                               │
       │                                   3. Ekstraherer claims
       │                                      - nameID
       │                                      - authLevel
       │                                      - consumerId
       │                                               │
       │                                   4. Genererer OIDC token
       │                                      - Signerer med RSA
       │                                      - Setter expiry
       │                                               │
       │ 5. Returns OIDC token                         │
       │    { "access_token": "eyJ..." }               │
       │<──────────────────────────────────────────────┤
       │                                               │
       │ 6. Bruker OIDC token mot                      │
       │    moderne API                                │
       │                                               │
```

### Scenario 2: Moderne system trenger SAML token

```
┌──────────────┐                                ┌─────────────┐
│  Moderne App │                                │   Gandalf   │
│  (har OIDC)  │                                │     STS     │
└──────┬───────┘                                └──────┬──────┘
       │                                               │
       │ 1. POST /token/exchange                       │
       │    Auth: Basic srvUser:pass                   │
       │    subject_token=<OIDC_TOKEN>                 │
       │    requested_token_type=saml2                 │
       ├──────────────────────────────────────────────>│
       │                                               │
       │                                   2. Validerer OIDC token
       │                                      - Sjekker JWT signatur
       │                                      - Validerer issuer
       │                                      - Sjekker expiry
       │                                               │
       │                                   3. Konverterer til SAML
       │                                      - Bygger XML struktur
       │                                      - Signerer med X.509
       │                                               │
       │ 4. Returns SAML token (Base64)                │
       │    { "access_token": "PD94..." }              │
       │<──────────────────────────────────────────────┤
       │                                               │
       │ 5. Bruker SAML token mot                      │
       │    legacy system                              │
       │                                               │
```

### Scenario 3: System trenger nytt OIDC token

```
┌──────────────┐                                ┌─────────────┐
│  System App  │                                │   Gandalf   │
│              │                                │     STS     │
└──────┬───────┘                                └──────┬──────┘
       │                                               │
       │ 1. POST /rest/v1/sts/token                    │
       │    Auth: Basic srvMyApp:secretPass            │
       │    grant_type=client_credentials              │
       ├──────────────────────────────────────────────>│
       │                                               │
       │                                   2. Autentiserer mot LDAP
       │                                      - Kobler til AD
       │                                      - Validerer credentials
       │                                               │
       │                                   3. Genererer OIDC token
       │                                      - subject = srvMyApp
       │                                      - audience = srvMyApp
       │                                      - expires = 1 time
       │                                               │
       │ 4. Returns OIDC token                         │
       │    { "access_token": "eyJ...",                │
       │      "expires_in": 3600 }                     │
       │<──────────────────────────────────────────────┤
       │                                               │
```

---

## 🔐 Sikkerhet og Autentisering

### Autentiseringsmetoder:

1. **Basic Authentication**
   - Alle endepunkter krever Basic Auth
   - Format: `Authorization: Basic base64(username:password)`
   - Valideres mot LDAP/Active Directory

2. **Token Signing**
   - OIDC tokens signeres med RSA-256
   - SAML tokens signeres med X.509 sertifikater
   - Private keys lagres i KeyStore (JKS)

3. **Token Validation**
   - OIDC: JWT signature validation med JWKS
   - SAML: XML signature validation
   - Expiry check
   - Issuer validation

### Støttede Eksterne Issuers:
- **Azure AD** (NAVs cloud identity)
- **TokenX** (token exchange platform)
- **ID-porten** (BankID autentisering)
- **Maskinporten** (machine-to-machine)

---

## 💻 Teknisk Stack

### Backend:
- **Språk:** Kotlin 2.2.21
- **Framework:** Spring Boot 3.5.7
  - Spring Security 7.0.0
  - Spring Web
  - Spring Data JPA
- **Token Libraries:**
  - Nimbus JOSE+JWT 11.30.1 (JWT handling)
  - Unboundid LDAP SDK 7.0.4 (LDAP)
- **Database:** Oracle JDBC (prod), H2 (test)
- **Logging:** Logback + Logstash encoder
- **Metrics:** Prometheus Micrometer
- **API Docs:** SpringDoc OpenAPI 2.8.14

### Build & Deploy:
- **Build Tool:** Gradle (Kotlin DSL)
- **Container:** Docker
- **Platform:** Kubernetes (NAIS)
- **Java Version:** 21

---

## 📊 API Endepunkter - Komplett Oversikt

### Token Utstedelse:
| Endepunkt | Metode | Beskrivelse |
|-----------|--------|-------------|
| `/rest/v1/sts/token` | POST | Utsteder OIDC token til systembruker |
| `/rest/v1/sts/token2` | POST | Utsteder OIDC token (Stormaskin variant) |
| `/rest/v1/sts/samltoken` | POST | Utsteder SAML token til systembruker |

### Token Exchange:
| Endepunkt | Metode | Beskrivelse |
|-----------|--------|-------------|
| `/rest/v1/sts/token/exchange` | POST | SAML ↔ OIDC konvertering |
| `/rest/v1/sts/token/exchangedifi` | POST | DIFI Maskinporten → OIDC |

### Metadata & Keys:
| Endepunkt | Metode | Beskrivelse |
|-----------|--------|-------------|
| `/.well-known/openid-configuration` | GET | OIDC discovery metadata |
| `/jwks` | GET | Public keys for JWT validation |
| `/rest/v1/sts/jwks` | GET | Alternativ JWKS endepunkt |

### Validering:
| Endepunkt | Metode | Beskrivelse |
|-----------|--------|-------------|
| `/rest/v1/sts/token/validate` | GET | Validerer OIDC token |

### Health & Metrics:
| Endepunkt | Metode | Beskrivelse |
|-----------|--------|-------------|
| `/isAlive` | GET | Liveness probe |
| `/isReady` | GET | Readiness probe |
| `/ping` | GET | Simple health check |
| `/prometheus` | GET | Metrics for monitoring |

---

## 🚀 Deployering

### Miljøer:

| Miljø | URL | Beskrivelse |
|-------|-----|-------------|
| **Test (T4)** | `https://security-token-service-t4.nais.preprod.local` | Test-miljø |
| **Development** | `https://security-token-service.dev.adeo.no` | Dev-miljø (naisdevice) |
| **Preprod** | `https://security-token-service.nais.preprod.local` | Pre-produksjon |
| **Production** | `https://security-token-service.nais.adeo.no` | Produksjon |

### Kubernetes (NAIS):
- Kjører i FSS (Fagsystemer Sonen)
- Namespace: `atom`
- Persistent storage: Oracle database

---

## 🐛 Feilsøking - Vanlige Problemer

### 1. **SSL Certificate Error**
```
PKIX path building failed: unable to find valid certification path
```
**Løsning:**
- Sjekk at truststore inneholder riktige CA-sertifikater
- Verifiser at sertifikatkjeden er komplett
- For Kubernetes: Sjekk at secrets er montert korrekt

### 2. **LDAP Connection Timeout**
```
Error when connecting to LDAPS
```
**Løsning:**
- Sjekk nettverkstilgang til LDAP server
- Verifiser LDAP config (url, port, timeout)
- Test med `kubectl exec` og curl

### 3. **Token Validation Failed**
```
JWT signature does not match
```
**Løsning:**
- Sjekk at issuer er riktig
- Verifiser at token ikke er utløpt
- Kontroller at public key er tilgjengelig på JWKS endpoint

---

## 📝 Eksempel: Komplett Token Exchange Flyt

```bash
# 1. Få OIDC token først (som systembruker)
curl -X POST https://security-token-service.dev.adeo.no/rest/v1/sts/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "srvMyApp:MySecretPassword" \
  -d "grant_type=client_credentials&scope=openid"

# Response:
# {
#   "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "token_type": "Bearer",
#   "expires_in": 3600
# }

# 2. Konverter OIDC til SAML (for å kalle legacy system)
curl -X POST https://security-token-service.dev.adeo.no/rest/v1/sts/token/exchange \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "srvMyApp:MySecretPassword" \
  -d "grant_type=urn:ietf:params:oauth:grant-type:token-exchange" \
  -d "subject_token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d "subject_token_type=urn:ietf:params:oauth:token-type:access_token" \
  -d "requested_token_type=urn:ietf:params:oauth:token-type:saml2"

# Response:
# {
#   "access_token": "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4...",
#   "issued_token_type": "urn:ietf:params:oauth:token-type:saml2",
#   "token_type": "Bearer",
#   "expires_in": 3600
# }
```

---

## 📚 Referanser

- [OAuth 2.0 RFC 6749](https://tools.ietf.org/html/rfc6749)
- [OAuth 2.0 Token Exchange RFC 8693](https://tools.ietf.org/html/rfc8693)
- [OpenID Connect Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html)
- [SAML 2.0 Specification](http://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html)

---


**Sist oppdatert:** Februar 2026  
**Lisens:** Se LICENSE fil

