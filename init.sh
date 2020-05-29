echo "Leser secrets fra disk til environment"

if test -f "/secrets/serviceuser/username"; then
  export APPLICATION_SERVICE_USERNAME=$(cat /secrets/serviceuser/username)
  echo "Eksporterer variabel APPLICATION_SERVICE_USERNAME"
fi

if test -f "/secrets/serviceuser/password"; then
  export APPLICATION_SERVICE_PASSWORD=$(cat /secrets/serviceuser/password)
  echo "Eksporterer variabel APPLICATION_SERVICE_USERNAME"
fi

if test -f "/secrets/database/config/jdbc_url"; then
  export SPRING_DATASOURCE_URL=$(cat /secrets/database/config/jdbc_url)
  echo "Eksporterer variabel SPRING_DATASOURCE_URL"
fi

if test -f "/secrets/database/credentials/username"; then
  export SPRING_DATASOURCE_USERNAME=$(cat /secrets/database/credentials/username)
  echo "Eksporterer variabel SPRING_DATASOURCE_USERNAME"
fi

if test -f "/secrets/database/credentials/password"; then
  export SPRING_DATASOURCE_PASSWORD=$(cat /secrets/database/credentials/password)
  echo "Eksporterer variabel SPRING_DATASOURCE_PASSWORD"
fi

if test -f "/var/run/secrets/nais.io/vault/nav_keystore_file"; then
  export NAV_KEYSTORE_FILE=$(cat /var/run/secrets/nais.io/vault/nav_keystore_file)
  echo "Eksporterer variabel NAV_KEYSTORE_FILE"
fi

if test -f "/var/run/secrets/nais.io/vault/nav_keystore_password"; then
  export NAV_KEYSTORE_PASSWORD=$(cat /var/run/secrets/nais.io/vault/nav_keystore_password)
  echo "Eksporterer variabel NAV_KEYSTORE_PASSWORD"
fi

