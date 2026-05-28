# SEMS IAM Service

## Config model (Config Service ready)
This service now supports centralized configuration through `CONFIG_SERVICE_URL`.

At startup, the service queries:
- `GET /api/v1/config/{service-name}`
- `GET /api/v1/config/kafka`
- `GET /api/v1/config/services`

If values are returned, they override local properties before beans are initialized.
If Config Service is unavailable, the service continues using local defaults and `.env` values.

## Environment variables kept in this service
Required (sensitive or deployment-specific):
- `SERVER_PORT`
- `CONFIG_SERVICE_URL`
- `CONFIG_SERVICE_TIMEOUT_MS` (optional, default `3000`)
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`

Optional local fallback overrides (only if Config Service is not available):
- `KAFKA_BOOTSTRAP_SERVERS`
- `KAFKA_SECURITY_PROTOCOL`
- `KAFKA_SASL_MECHANISM`
- `KAFKA_USERNAME`
- `KAFKA_PASSWORD`
- `KAFKA_SSL_CA_CERT`
- `KAFKA_CONSUMER_GROUP_ID`
- `IAM_DEPLOY_URL`
- `GOOGLE_REDIRECT_URI`
- `GOOGLE_SCOPES`

## Configuration now expected from Config Service
Shared/non-sensitive config should live in Config Service, for example:
- Kafka topics (`topics.*`)
- Kafka bootstrap and protocol defaults
- Consumer group defaults
- OAuth redirect/scopes defaults
- `service.public-base-url`
- Common service URLs and cross-service routing config

## API endpoints (unchanged)
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/google`
- `GET /api/v1/auth/google/url`
- `GET /api/v1/auth/google/callback`
- `GET /api/v1/users/me`
- `GET /api/v1/users` (ADMIN)
- `POST /api/v1/users/{userId}/roles` (ADMIN)
- `GET /actuator/health`

## Run locally
1. Copy `.env.example` to `.env` and set required values.
2. Ensure Config Service is running and reachable at `CONFIG_SERVICE_URL`.
3. Run:
   - `./mvnw spring-boot:run`

## Docker run
- `docker compose up --build`

Gateway remains public entrypoint on `http://localhost:8081`.
IAM direct debugging stays on `http://localhost:8080`.

## Azure Container Apps deployment guidance
1. Set secrets in ACA secrets store:
   - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
2. Set env vars:
   - `SERVER_PORT=8080`
   - `CONFIG_SERVICE_URL=https://<config-service-domain>`
   - `CONFIG_SERVICE_TIMEOUT_MS=3000`
3. Keep shared config in Config Service, not in per-service env vars.
4. Restrict IAM outbound access to trusted services (DB, Kafka, Config Service, Google OAuth).
5. Rotate secrets and avoid committing real `.env` values.
