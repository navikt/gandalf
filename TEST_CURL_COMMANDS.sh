#!/bin/bash

# Gandalf STS - Test cURL Commands
# Bruk disse kommandoene for å teste token exchange

# ============================================
# ENVIRONMENT SETUP
# ============================================
export BASE_URL="https://security-token-service.dev.adeo.no"
export USERNAME="YOUR_SERVICE_USER"  # f.eks. srvMyApp
export PASSWORD="YOUR_PASSWORD"

# ============================================
# 1. UTSTEDE OIDC TOKEN (client_credentials)
# ============================================
echo "=== 1. Utsteder OIDC token ==="
curl -X POST "${BASE_URL}/rest/v1/sts/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "${USERNAME}:${PASSWORD}" \
  -d "grant_type=client_credentials&scope=openid"

echo -e "\n\n"

# ============================================
# 2. KONVERTER OIDC TIL SAML
# ============================================
echo "=== 2. Konverterer OIDC til SAML ==="
# Du må først få et OIDC token fra steg 1
export OIDC_TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."  # Sett inn faktisk token

curl -X POST "${BASE_URL}/rest/v1/sts/token/exchange" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "${USERNAME}:${PASSWORD}" \
  -d "grant_type=urn:ietf:params:oauth:grant-type:token-exchange" \
  -d "subject_token=${OIDC_TOKEN}" \
  -d "subject_token_type=urn:ietf:params:oauth:token-type:access_token" \
  -d "requested_token_type=urn:ietf:params:oauth:token-type:saml2"

echo -e "\n\n"

# ============================================
# 3. KONVERTER SAML TIL OIDC
# ============================================
echo "=== 3. Konverterer SAML til OIDC ==="
# SAML token må være Base64-encoded
export SAML_TOKEN_BASE64="PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4..."  # Base64-encoded SAML

curl -X POST "${BASE_URL}/rest/v1/sts/token/exchange" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "${USERNAME}:${PASSWORD}" \
  -d "grant_type=urn:ietf:params:oauth:grant-type:token-exchange" \
  -d "subject_token=${SAML_TOKEN_BASE64}" \
  -d "subject_token_type=urn:ietf:params:oauth:token-type:saml2"

echo -e "\n\n"

# ============================================
# 4. HENT JWKS (PUBLIC KEYS)
# ============================================
echo "=== 4. Henter JWKS public keys ==="
curl -X GET "${BASE_URL}/jwks"

echo -e "\n\n"

# ============================================
# 5. HENT OIDC CONFIGURATION
# ============================================
echo "=== 5. Henter OIDC configuration ==="
curl -X GET "${BASE_URL}/.well-known/openid-configuration"

echo -e "\n\n"

# ============================================
# 6. VALIDERE OIDC TOKEN
# ============================================
echo "=== 6. Validerer OIDC token ==="
curl -X POST "${BASE_URL}/rest/v1/sts/token/validate" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "${USERNAME}:${PASSWORD}" \
  -d "token=${OIDC_TOKEN}"

echo -e "\n\n"

# ============================================
# 7. HEALTH CHECK
# ============================================
echo "=== 7. Health check ==="
curl -X GET "${BASE_URL}/isAlive"
curl -X GET "${BASE_URL}/isReady"

echo -e "\n\n"

# ============================================
# KOMPLETT EKSEMPEL MED PIPE
# ============================================
echo "=== Komplett eksempel: Få token og konverter ==="

# Få OIDC token og ekstraher access_token
OIDC_RESPONSE=$(curl -s -X POST "${BASE_URL}/rest/v1/sts/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "${USERNAME}:${PASSWORD}" \
  -d "grant_type=client_credentials&scope=openid")

echo "OIDC Response: ${OIDC_RESPONSE}"

# Ekstraher access_token (krever jq)
if command -v jq &> /dev/null; then
    ACCESS_TOKEN=$(echo "${OIDC_RESPONSE}" | jq -r '.access_token')
    echo "Access Token: ${ACCESS_TOKEN}"

    # Konverter til SAML
    SAML_RESPONSE=$(curl -s -X POST "${BASE_URL}/rest/v1/sts/token/exchange" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -u "${USERNAME}:${PASSWORD}" \
      -d "grant_type=urn:ietf:params:oauth:grant-type:token-exchange" \
      -d "subject_token=${ACCESS_TOKEN}" \
      -d "subject_token_type=urn:ietf:params:oauth:token-type:access_token" \
      -d "requested_token_type=urn:ietf:params:oauth:token-type:saml2")

    echo "SAML Response: ${SAML_RESPONSE}"
else
    echo "Installer jq for å parse JSON: brew install jq"
fi

