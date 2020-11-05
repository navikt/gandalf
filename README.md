![PROD- Build, push, and deploy](https://github.com/navikt/gandalf/workflows/PROD-%20Build,%20push,%20and%20deploy/badge.svg)

# About
REST-STS is available in FSS, users are authenticated to onprem-AD.  
REST-STS won't do any additional access control or role check.  
The service definitions based on specifications in these references:  
[The OAuth 2.0 Authorization Framework](https://tools.ietf.org/html/rfc6749)  
[Starting point for .well-known endpoint](https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata)  

### Ingress
**Test:** `https://security-token-service-t4.nais.preprod.local`  
**Development:** `https://security-token-service.nais.preprod.local`  
**Prod:** `https://security-token-service.nais.adeo.no`  

### Developers
For local Development `https://security-token-service.dev.adeo.no` is exposed in [naisdevice](https://doc.nais.io/device)

### Openapi
`/api`  

### Identity Provider Metadata
| Type              | Endpoint              |
|-----------------------|-----------------------|
| Retrieve public keys for validating the oidc token issued by STS    | `/jwks`                  |
| Configuration info    | `/.well-known/openid-configuration`                  |

### Overview of token issuance and token conversions on REST interface
| From              | To                  | Endpoint              | Extra                                            |
|-----------------------|-----------------------|-----------------------|--------------------------------------------------|
| client_credentials               | OIDC                | `/rest/v1/sts/token`                  |                             |
| client_credentials        | OIDC                | `/rest/v1/sts/token2`                   | For Stormaskin   |
| client_credentials          | SAML               |  `/rest/v1/sts/samltoken`             | 
| OIDC (Issued by OpenAm, `This` STS, AzureAD)      |  SAML     | `/rest/v1/sts/token/exchange`                   |                          |
| SAML token (Issued by STS(Datapower) or `This` STS)                   | OIDC     | `/rest/v1/sts/token/exchange`                   |           |

### Example Request. For more info check out: `../api`
`../rest/v1/sts/token`  
### Issue System OIDC
**You send:**  Your srvUser credentials i Authorization header  
**You get:** An `OIDC-Token` with which you can make further actions.  

**Request:**
```json
POST /rest/v1/sts/token 
HTTP/1.1
Accept: application/json
Content-Type: application/x-www-form-urlencoded
Authorization: Basic aGVsbG86eW91

grant_type=client_credentials&
scope=openid
```
**Successful Response:**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
   "access_token": "eY........",
   "token_type": "Bearer",
   "expires_in": 3600
}
```

The validity period of the token is specified in seconds. The OIDC token is a B64 URL-encoded JWT.

**Failed Responses:**
```json
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
    "error": "invalid_client",
    "error_description": "Unauthorised: Full authentication is required to access this resource"
}
```

**Failed Response:**
```json
HTTP/1.1 400 BadRequest
Content-Type: application/json

{
    "error": "invalid_request",
    "error_description": "Some message"
}
```

### Issue OIDC token based on SAML token
`...rest/v1/sts/token/exchange`

The service validates the received SAML token, generates a new OIDC token with content retrieved from the SAML token.

**Request:**
```json
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
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
   "access_token": <oidc-token>,
   "issued_token_type": "urn:ietf:params:oauth:token-type:access_token",
   "token_type": "Bearer",
   "expires_in": <30 sek more then expiry for SAML-tokenet>
}
```

```json
HTTP/1.1 400 BadRequest
Content-Type: application/json

{
    "error": "invalid_request",
    "error_description": "Some message"
}
```

The validity period of the token is specified in seconds. The OIDC token is a B64 URL-encoded JWT.

### Issue SAML token based on OIDC token
`...rest/v1/sts/token/exchange`

**Request:**
```json
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
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
   "access_token": <saml-token>,
   "issued_token_type": "urn:ietf:params:oauth:token-type:saml2",
   "token_type": "Bearer",
   "expires_in": <expiry for SAML-token>
}
```

```json
HTTP/1.1 400 BadRequest
Content-Type: application/json

{
    "error": "invalid_request",
    "error_description": "Some message"
}
```

## To Run
Run _GandalfApplicationLocal_ in `test/kotlin/no/nav/gandalf`  
Runnable endpoints:  
`/rest/v1/sts/token`  
`/rest/v1/sts/token2`  
`/rest/v1/sts/token/exchange`  
`/rest/v1/sts/samltoken`  
`/.well-known/openid-configuration`  
`/jwks`  

## Tools n stuff
* Kotlin  
* Nimbus  
* Snyk
* Spring Boot

## Contact
Plattformsikkerhet: `youssef.bel.mekki@nav.no` ++  
Slack: `#pig_sikkerhet`

## TODO
- [x] Add more endpoints to be run local testing  
- [x] Expose `dev.adeo.no` for local development  
- [ ] Describe the Swagger Objects and values
- [ ] Refactoring of code for better readability

