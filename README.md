![Build, push, and deploy](https://github.com/navikt/gandalf/workflows/Build,%20push,%20and%20deploy/badge.svg?branch=master)

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
#### Get System OIDC
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
**Failed Response:**
```json
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
    "error": "invalid_client",
    "error_description": "Unauthorised: Full authentication is required to access this resource"
}
```

## To Run
Run _GandalfApplicationLocal_ in `test/kotlin/no/nav/gandalf`  
Runnable endpoints:  
`/rest/v1/sts/token`  
`/rest/v1/sts/token2`  
`/.well-known/openid-configuration`  
`/jwks`  

## Tools n stuff
* Kotlin  
* Nimbus  
* Snyk
* Spring Boot and all its dependencies.

## Contact
Plattformsikkerhet: `youssef.bel.mekki@nav.no` ++  
Slack: `#pig_sikkerhet`

## TODO
- [ ] Add more endpoints to be run local testing  
- [ ] Describe the Swagger Objects and values
- [ ] Refactoring of code for better readability
