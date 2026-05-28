# SEMS IAM Service

## Local integration with API Gateway + Config-Service
- Config-Service: `http://localhost:8090`
- API Gateway: `http://localhost:8081`
- IAM local base URL: `http://localhost:8082`
- Route prefix: `/api/v1`

This service loads centralized config from:
- `GET /api/v1/config/services/{serviceName}`
- `GET /api/v1/config/kafka`
- `GET /api/v1/config/services`

using `CONFIG_SERVICE_URL` at startup.

## Required local environment variables
Non-sensitive:
- `SERVER_PORT=8082`
- `CONFIG_SERVICE_URL=http://localhost:8090`
- `CONFIG_SERVICE_TIMEOUT_MS=3000`
- `API_GATEWAY_AUTH_REQUIRED=false` (for local testing without JWT)
- `CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173`

Sensitive:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`

Optional local Kafka fallback (if Config Service does not return Kafka config):
- `KAFKA_BOOTSTRAP_SERVERS=localhost:9092`
- `KAFKA_SECURITY_PROTOCOL=PLAINTEXT`
- `KAFKA_CONSUMER_GROUP_ID=iam-service`

## Health checks
Public (no auth):
- `GET /health`
- `GET /actuator/health`

## API endpoints used by Gateway
- `POST /api/v1/auth/register` (public)
- `POST /api/v1/auth/login` (public)
- `POST /api/v1/auth/google` (public)
- `GET /api/v1/auth/google/url` (public)
- `GET /api/v1/auth/google/callback` (public)
- `GET /api/v1/users/me` (protected when `API_GATEWAY_AUTH_REQUIRED=true`)
- `GET /api/v1/users` (ADMIN)
- `POST /api/v1/users/{userId}/roles` (ADMIN)

## Run local
1. Ensure DB is reachable.
2. Ensure Kafka is running on `localhost:9092` (or served by Config Service).
3. Configure `.env` from `.env.example`.
4. Run service.

## Azure Container Apps
- Keep secrets in ACA secret store.
- Set only non-sensitive config as env vars.
- Keep shared routing/Kafka/topics config in Config-Service.
