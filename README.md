# SEMS IAM Service

## Environment variables
Use `.env.example` as base:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MINUTES`
- `KAFKA_ENABLED`
- `KAFKA_BOOTSTRAP_SERVERS`
- `KAFKA_BROKERS`
- `KAFKA_HOST`
- `KAFKA_PORT`
- `KAFKA_USERNAME`
- `KAFKA_PASSWORD`
- `KAFKA_SECURITY_PROTOCOL`
- `KAFKA_SASL_MECHANISM`
- `KAFKA_SSL_CA_CERT`
- `KAFKA_CONSUMER_GROUP_ID`
- `SERVER_PORT`
- `GATEWAY_PORT`
- `IAM_PORT`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_REDIRECT_URI`
- `GOOGLE_SCOPES`
- `IAM_DEPLOY_URL`

Notes:
- `.env` contains real secrets and is ignored by git.
- `.env.example` contains placeholders and should be committed.

## Database schema
Run:
- `src/main/resources/db/schema.sql`

## Run locally
1. Configure Supabase and Kafka (Aiven) credentials in env vars.
2. Build and run:
   - `./mvnw spring-boot:run`

When running the application from IntelliJ, requests go directly to:
- `http://localhost:8080`

Kafka uses the Aiven cluster configured in your env vars.

Optional local PLAINTEXT (only if you intentionally run local Kafka):
- Set `KAFKA_SECURITY_PROTOCOL=PLAINTEXT`
- Set `KAFKA_BOOTSTRAP_SERVERS=localhost:9092`

## Docker run
- `docker compose up --build`

Optional local Kafka (PLAINTEXT) with Docker Compose:
- `docker compose --profile local-kafka up --build`
- Set `KAFKA_SECURITY_PROTOCOL=PLAINTEXT`
- Set `KAFKA_BOOTSTRAP_SERVERS=kafka:29092`

With Docker Compose, the API Gateway is the public entry point:
- `http://localhost:8081`

The IAM service is still exposed for direct debugging:
- `http://localhost:8080`

## Render environment variables
Set these in Render: Service -> Environment -> Environment Variables

Database:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

JWT:
- `JWT_SECRET`
- `JWT_EXPIRATION_MINUTES`

Kafka (Aiven):
- `KAFKA_ENABLED`
- `KAFKA_BOOTSTRAP_SERVERS`
- `KAFKA_BROKERS`
- `KAFKA_HOST`
- `KAFKA_PORT`
- `KAFKA_USERNAME`
- `KAFKA_PASSWORD`
- `KAFKA_SECURITY_PROTOCOL`
- `KAFKA_SASL_MECHANISM`
- `KAFKA_SSL_CA_CERT` (paste full PEM; use \n if the UI does not allow multiline)
- `KAFKA_CONSUMER_GROUP_ID`

Google OAuth:
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_REDIRECT_URI`
- `GOOGLE_SCOPES`

Service:
- `SERVER_PORT`
- `GATEWAY_PORT`
- `IAM_PORT`
- `IAM_DEPLOY_URL`

## API endpoints
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/google`
- `GET /api/v1/auth/google/url`
- `GET /api/v1/auth/google/callback`
- `GET /api/v1/users/me`
- `GET /api/v1/users` (ADMIN)
- `POST /api/v1/users/{userId}/roles` (ADMIN)
- `GET /actuator/health`

## Kafka events
Published by IAM:
- `iam.user.registered`
- `iam.user.logged-in`
- `iam.role.assigned`

Consumed by IAM:
- `iam.role-assignment.requested`

Role assignment request event payload:

```json
{
  "userId": "00000000-0000-0000-0000-000000000000",
  "role": "ADMIN"
}
```

## Kafka testing

Connection test (Aiven metadata):
- Use `kcat` (or any Kafka client tool) with SASL_SSL + SCRAM-SHA-256 and the CA cert.

Example with `kcat` (PowerShell):
```powershell
$ca = $env:KAFKA_SSL_CA_CERT -replace '\n', "`n"
$ca | Set-Content -NoNewline aiven-ca.pem
kcat -L -b $env:KAFKA_BOOTSTRAP_SERVERS `
  -X security.protocol=$env:KAFKA_SECURITY_PROTOCOL `
  -X sasl.mechanism=$env:KAFKA_SASL_MECHANISM `
  -X sasl.username=$env:KAFKA_USERNAME `
  -X sasl.password=$env:KAFKA_PASSWORD `
  -X ssl.ca.location=aiven-ca.pem
```

Producer test (IAM publishes events):
1. Start the service.
2. Call `POST /api/v1/auth/register`.
3. Verify a message in `iam.user.registered`.

Example `kcat` consumer:
```powershell
kcat -C -b $env:KAFKA_BOOTSTRAP_SERVERS -t iam.user.registered `
  -X security.protocol=$env:KAFKA_SECURITY_PROTOCOL `
  -X sasl.mechanism=$env:KAFKA_SASL_MECHANISM `
  -X sasl.username=$env:KAFKA_USERNAME `
  -X sasl.password=$env:KAFKA_PASSWORD `
  -X ssl.ca.location=aiven-ca.pem
```

Consumer test (IAM consumes role assignment requests):
1. Start the service.
2. Produce a message to `iam.role-assignment.requested` with a valid `userId`.
3. Verify the role assignment in your DB or logs.

Example `kcat` producer:
```powershell
@'{"userId":"00000000-0000-0000-0000-000000000000","role":"ADMIN"}'@ | kcat -P -b $env:KAFKA_BOOTSTRAP_SERVERS -t iam.role-assignment.requested `
  -X security.protocol=$env:KAFKA_SECURITY_PROTOCOL `
  -X sasl.mechanism=$env:KAFKA_SASL_MECHANISM `
  -X sasl.username=$env:KAFKA_USERNAME `
  -X sasl.password=$env:KAFKA_PASSWORD `
  -X ssl.ca.location=aiven-ca.pem
```

## API Gateway routes
- `POST /api/v1/auth/register` -> `iam-service`
- `POST /api/v1/auth/login` -> `iam-service`
- `GET /api/v1/users/me` -> `iam-service`
- `GET /api/v1/users` -> `iam-service`
- `POST /api/v1/users/{userId}/roles` -> `iam-service`

## Keep-alive script (Render)
If your free Render instance goes to sleep, you can ping it periodically from another machine/service:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\keep-alive.ps1 `
  -Url "https://your-render-service.onrender.com/" `
  -IntervalSeconds 600
```

Notes:
- Use a public endpoint that returns `200` (for example `/` or a health endpoint if you add one).
- `600` seconds = ping every 10 minutes.
- Keep this script running in a separate environment (not inside the same sleeping service).
