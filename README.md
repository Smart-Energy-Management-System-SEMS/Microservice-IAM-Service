# SEMS IAM Service

## Environment variables
Use `.env.example` as base:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MINUTES`
- `KAFKA_BOOTSTRAP_SERVERS`
- `SERVER_PORT`

## Database schema
Run:
- `src/main/resources/db/schema.sql`

## Run locally
1. Start Kafka and Zookeeper:
   - `docker compose up -d zookeeper kafka`
2. Configure Supabase credentials in env vars.
3. Build and run:
   - `./mvnw spring-boot:run`

## Docker run
- `docker compose up --build`

## API endpoints
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/users/me`
- `GET /api/v1/users` (ADMIN)
- `POST /api/v1/users/{userId}/roles` (ADMIN)

## Kafka events
- `iam.user.registered`
- `iam.user.logged-in`
- `iam.role.assigned`
