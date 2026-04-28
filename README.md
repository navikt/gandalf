![PROD- Build, push, and deploy](https://github.com/navikt/gandalf/workflows/PROD-%20Build,%20push,%20and%20deploy/badge.svg)

# Gandalf — Security Token Service (STS)

A Security Token Service (STS) is a standard component in security architectures for authentication, identity mapping, token validation and conversion.
The concept comes from the OASIS WS-Trust specification which describes a secure model for establishing and evaluating trust relationships between applications.
The security model is based on 3 players: consumer, provider, and a Security Token Service — the STS is the central player that issues tokens all providers can trust.

## About

This STS runs in FSS and authenticates users against on-prem Active Directory.
It does not perform additional access control or role checks.

Service definitions are based on:
- [The OAuth 2.0 Authorization Framework (RFC 6749)](https://tools.ietf.org/html/rfc6749)
- [OpenID Connect Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata)

### Ingresses

| Environment | URL |
|---|---|
| Test (T4) | `https://security-token-service-t4.nais.preprod.local` |
| Development | `https://security-token-service.nais.preprod.local` |
| Production | `https://security-token-service.nais.adeo.no` |

### Local development

`https://security-token-service.dev.adeo.no` is exposed via [naisdevice](https://doc.nais.io/device)

### Openapi
`/api`

### Identity Provider Metadata
| Type                                                             | Endpoint                            |
|------------------------------------------------------------------|-------------------------------------|
| Retrieve public keys for validating the oidc token issued by STS | `/jwks`                             |
| Configuration info                                               | `/.well-known/openid-configuration` |

### Overview of token issuance and token conversions on REST interface
| From                                                | To   | Endpoint                      | Extra          |
|-----------------------------------------------------|------|-------------------------------|----------------|
| client_credentials                                  | OIDC | `/rest/v1/sts/token`          |                |
| client_credentials                                  | OIDC | `/rest/v1/sts/token2`         | For Stormaskin |
| client_credentials                                  | SAML | `/rest/v1/sts/samltoken`      |
| OIDC (Issued by TokenX, `This` STS, AzureAD)        | SAML | `/rest/v1/sts/token/exchange` |                |
| SAML token (Issued by STS(Datapower) or `This` STS) | OIDC | `/rest/v1/sts/token/exchange` |                |

### Example Request. For more info check out: `../api`
`../rest/v1/sts/token`
### Issue System OIDC
**You send:**  Your srvUser credentials i Authorization header  
**You get:** An `OIDC-Token` with which you can make further actions.

**Request:**
```http
POST /rest/v1/sts/token 
HTTP/1.1
Accept: application/json
Content-Type: application/x-www-form-urlencoded
Authorization: Basic aGVsbG86eW91

grant_type=client_credentials&
scope=openid
```
**Successful Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
   "access_token": "eY........",
   "token_type": "Bearer",
   "expires_in": 3600
}
```

The validity period of the token is specified in seconds. The OIDC token is a B64 URL-encoded JWT.

**Failed Response:**
```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json
```

```json
{
    "error": "invalid_client",
    "error_description": "Unauthorised: Full authentication is required to access this resource"
}
```

```http
HTTP/1.1 400 BadRequest
Content-Type: application/json
```

```json
{
    "error": "invalid_request",
    "error_description": "Some message"
}
```

### Issue OIDC token based on SAML token
`...rest/v1/sts/token/exchange`

The service validates the received SAML token, generates a new OIDC token with content retrieved from the SAML token.

**Request:**
```http
POST /rest/v1/sts/token/exchange 
HTTP/1.1
Accept: application/x-www-form-urlencoded
Content-Type: application/x-www-form-urlencoded
Authorization: Basic aGVsbG86eW91

grant_type=urn:ietf:params:oauth:grant-type:token-exchange&
requested_token_type=urn:ietf:params:oauth:token-type:access_token&
subject_token_type=urn:ietf:params:oauth:token-type:saml2&
subject_token=BASE64URL encoded SAML token
```

**Successful Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
   "access_token": "eY........",
   "issued_token_type": "urn:ietf:params:oauth:token-type:access_token",
   "token_type": "Bearer",
   "expires_in": "30 sek more then expiry for SAML-tokenet"
}
```

**Failed Response:**
```http
HTTP/1.1 400 BadRequest
Content-Type: application/json
```

```json
{
    "error": "invalid_request",
    "error_description": "Some message"
}
```

The validity period of the token is specified in seconds. The OIDC token is a B64 URL-encoded JWT.

### Issue SAML token based on OIDC token
`...rest/v1/sts/token/exchange`

**Request:**
```http
POST /rest/v1/sts/token/exchange 
HTTP/1.1
Accept: application/x-www-form-urlencoded
Content-Type: application/x-www-form-urlencoded
Authorization: Basic aGVsbG86eW91

grant_type=urn:ietf:params:oauth:grant-type:token-exchange&
requested_token_type=urn:ietf:params:oauth:token-type:saml2&
subject_token_type=urn:ietf:params:oauth:token-type:access_token&
subject_token=BASE64URL encoded OIDC token
```

**Successful Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
   "access_token": "eY........",
   "issued_token_type": "urn:ietf:params:oauth:token-type:saml2",
   "token_type": "Bearer",
   "expires_in": "expiry for SAML-token"
}
```

**Failed Response:**
```http
HTTP/1.1 400 BadRequest
Content-Type: application/json
```

```json
{
    "error": "invalid_request",
    "error_description": "Some message"
}
```

## Running locally

```bash
./gradlew bootRun -Dspring.profiles.active=local
```

This starts the app with an embedded H2 database and an in-memory LDAP server on port `11389`.

Available endpoints when running locally:

| Endpoint | Description |
|---|---|
| `/rest/v1/sts/token` | Issue system OIDC token |
| `/rest/v1/sts/token2` | Issue system OIDC token (Stormaskin) |
| `/rest/v1/sts/token/exchange` | Exchange SAML ↔ OIDC |
| `/rest/v1/sts/samltoken` | Issue SAML token |
| `/.well-known/openid-configuration` | OpenID Connect discovery |
| `/jwks` | Public keys |
| `/actuator/prometheus` | Prometheus metrics |
| `/api` | OpenAPI / Swagger UI |

## Metrics

Metrics are exposed at `/actuator/prometheus` using [Micrometer](https://micrometer.io/) with the Prometheus registry.

Key metrics:

| Metric | Type | Description |
|---|---|---|
| `securitytokenservice_saml_token_ok_total` | Counter | Successful SAML token issuances |
| `securitytokenservice_oidc_token_ok_total` | Counter | Successful OIDC token issuances |
| `securitytokenservice_exchange_token_ok_total` | Counter | Successful token exchanges |
| `requests_latency_ldap_seconds_*` | Timer | LDAP request latency |
| `requests_latency_oidc_seconds_*` | Timer | OIDC request latency |
| `keystore_cert_days_remaining` | Gauge | Days until keystore certificate expires |

The Grafana dashboard is defined in `.nais/grafana-dashboard.json`.

## Tech stack

- Kotlin
- Spring Boot
- Micrometer (Prometheus)
- Nimbus JOSE+JWT
- H2 (local), Oracle (remote)
- Embedded LDAP (local), Active Directory (remote)

## Alerts

Alerts are defined in `.nais/alerts.yml` and deployed to dev and prod environments.
An alert fires when a keystore certificate has fewer than 30 days remaining.
