#!/bin/sh

# This script is intended for Render deployments to parse database URLs and set JWT secrets.
# For local Docker Compose, the docker-compose.yml handles these directly.

# Ensure RENDER_DB_URL_RAW is set (this is Render's internalConnectionString)
if [ -z "$RENDER_DB_URL_RAW" ]; then
  echo "Error: RENDER_DB_URL_RAW (internalConnectionString) is not set by Render. Exiting."
  exit 1
fi

# Ensure RENDER_DB_USER_RAW and RENDER_DB_PASSWORD_RAW are set
if [ -z "$RENDER_DB_USER_RAW" ] || [ -z "$RENDER_DB_PASSWORD_RAW" ]; then
  echo "Error: RENDER_DB_USER_RAW or RENDER_DB_PASSWORD_RAW are not set by Render. Exiting."
  exit 1
fi

# --- Multi-step parsing of RENDER_DB_URL_RAW ---
# Expected format from Render's internalConnectionString: postgresql://user:password@host/database_name
# We need to extract user, password, host, and database name

# Remove "postgresql://" prefix
URL_NO_PREFIX=$(echo "$RENDER_DB_URL_RAW" | sed 's/^postgresql:\/\///')

# Extract user:password@host and database name
USER_PASS_HOST_PART=$(echo "$URL_NO_PREFIX" | cut -d'/' -f1)
DB_NAME_PARSED=$(echo "$URL_NO_PREFIX" | cut -d'/' -f2)

# Extract user:password and host from USER_PASS_HOST_PART
DB_USER_PASS_PART=$(echo "$USER_PASS_HOST_PART" | cut -d'@' -f1)
DB_HOST_PARSED=$(echo "$USER_PASS_HOST_PART" | cut -d'@' -f2)

# Extract user and password from DB_USER_PASS_PART
DB_USER_PARSED=$(echo "$DB_USER_PASS_PART" | cut -d':' -f1)
DB_PASSWORD_PARSED=$(echo "$DB_USER_PASS_PART" | cut -d':' -f2)

# PostgreSQL default port
DB_PORT_FINAL="5432"

# Construct the final DATABASE_URL (which application.properties expects)
# Ensure the format is postgresql://host:port/database (without jdbc: prefix, application.properties adds it)
export DATABASE_URL="postgresql://${DB_HOST_PARSED}:${DB_PORT_FINAL}/${DB_NAME_PARSED}"
export SPRING_DATASOURCE_USERNAME="${DB_USER_PARSED}"
export SPRING_DATASOURCE_PASSWORD="${DB_PASSWORD_PARSED}"

# Generate a random JWT secret if APP_JWT_SECRET is not already set
if [ -z "$APP_JWT_SECRET" ]; then
  APP_JWT_SECRET=$(head /dev/urandom | tr -dc A-Za-z0-9_ | head -c 64)
fi

# Execute the main application JAR, explicitly passing the constructed environment variables as Java System Properties
exec java \
  -Dspring.datasource.url="${DATABASE_URL}" \
  -Dspring.datasource.username="${SPRING_DATASOURCE_USERNAME}" \
  -Dspring.datasource.password="${SPRING_DATASOURCE_PASSWORD}" \
  -Dapp.jwt.secret="${APP_JWT_SECRET}" \
  -jar app.jar \
  --spring.profiles.active=prod