# SEMS IAM Service

## Environment variables
Use `.env.example` as base:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MINUTES`
- `KAFKA_BOOTSTRAP_SERVERS`
- `KAFKA_CONSUMER_GROUP_ID`
- `SERVER_PORT`
- `GATEWAY_PORT`
- `IAM_PORT`

## Database schema
Run:
- `src/main/resources/db/schema.sql`

## Run locally
1. Start Kafka and Zookeeper:
   - `docker compose up -d zookeeper kafka`
2. Configure Supabase credentials in env vars.
3. Build and run:
   - `./mvnw spring-boot:run`

When running the application from IntelliJ, requests go directly to:
- `http://localhost:8080`

Kafka is available locally at:
- `localhost:9092`

## Docker run
- `docker compose up --build`

With Docker Compose, the API Gateway is the public entry point:
- `http://localhost:8081`

The IAM service is still exposed for direct debugging:
- `http://localhost:8080`

## API endpoints
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/users/me`
- `GET /api/v1/users` (ADMIN)
- `POST /api/v1/users/{userId}/roles` (ADMIN)

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
