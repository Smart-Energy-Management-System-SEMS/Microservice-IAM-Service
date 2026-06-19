# SEMS IAM Service

Servicio IAM de SEMS en Java/Spring Boot, preparado para ejecucion local y despliegue en Azure Container Apps.

## Endpoints de salud
- `GET /health`
- `GET /actuator/health`

## Variables de entorno
Minimas para contenedor:
- `PORT=8080` (Azure Container Apps la inyecta automaticamente)
- `JWT_SECRET`
- `DATABASE_URL` (o `DB_URL`)

Opcionales/recomendadas:
- `CONFIG_SERVICE_URL`
- `KAFKA_BROKERS` (alias compatible: `KAFKA_BOOTSTRAP_SERVERS`)
- `TOPIC_IAM_EVENTS` (default: `iam.events`)
- `KAFKA_SECURITY_PROTOCOL`
- `KAFKA_SASL_MECHANISM`
- `KAFKA_USERNAME`
- `KAFKA_PASSWORD`
- `SPRING_PROFILES_ACTIVE=prod`

Compatibilidad local existente (no removida):
- `SERVER_PORT` (tiene prioridad sobre `PORT`)
- `DB_USERNAME`, `DB_PASSWORD`
- `API_GATEWAY_AUTH_REQUIRED`, `CORS_ALLOWED_ORIGINS`, `GOOGLE_*`, `IAM_DEPLOY_URL`

## Configuracion de puerto
La app usa:
- `server.port=${SERVER_PORT:${PORT:8080}}`

Esto mantiene local (`SERVER_PORT`) y Azure (`PORT`) sin hardcodear `localhost` para runtime cloud.

## Eventos Kafka
Contrato operativo actual:
- Topic fisico agrupado: `iam.events`
- `eventType` viaja dentro del payload
- Un mismo topic puede transportar varios `eventType`

Envelope aplicado por IAM:
```json
{
  "eventType": "iam.user.registered",
  "eventId": "uuid",
  "occurredAt": "2026-06-17T00:00:00Z",
  "data": {
    "userId": "uuid",
    "emailAddress": "user@example.com",
    "role": "ADMIN"
  }
}
```

Nota historica:
- Referencias como `TOPIC_IAM_USER_REGISTERED`, `TOPIC_IAM_USER_LOGGED_IN`, `TOPIC_IAM_ROLE_ASSIGNED` o `TOPIC_IAM_ROLE_ASSIGNMENT_REQUESTED` se consideran legado documental y no deben reintroducirse como configuracion activa de runtime.

## Ejecutar local
1. Copiar variables:
   - `copy .env.example .env` (Windows)
2. Ajustar `.env` con tu base de datos, JWT y servicios.
3. Ejecutar:
   - `./mvnw spring-boot:run`

## Docker
Build de imagen:
```bash
docker build -t sems-iam-service:latest .
```

Run local con `.env`:
```bash
docker run --rm -p 8080:8080 --env-file .env sems-iam-service:latest
```

Health check:
```bash
curl http://localhost:8080/actuator/health
```

## Azure Container Apps (ejemplo)
```bash
az containerapp create \
  --name sems-iam-service \
  --resource-group <rg> \
  --environment <aca-env> \
  --image <acr>.azurecr.io/sems-iam-service:latest \
  --target-port 8080 \
  --ingress external \
  --env-vars SPRING_PROFILES_ACTIVE=prod CONFIG_SERVICE_URL=<https://config-service-url> \
  --secrets jwt-secret=<jwt> database-url=<jdbc-url> kafka-password=<kafka-password> \
  --env-vars JWT_SECRET=secretref:jwt-secret DATABASE_URL=secretref:database-url KAFKA_PASSWORD=secretref:kafka-password
```

## Notas para API Gateway
Este repositorio es IAM. Se incluyen en `.env.example` variables de rutas de gateway para estandarizar despliegues SEMS:
- `IAM_SERVICE_URL`, `DEVICE_MANAGEMENT_SERVICE_URL`, `ALERT_SERVICE_URL`, `SUBSCRIPTIONS_SERVICE_URL`, `PAYMENTS_SERVICE_URL`, `ANALYTICS_SERVICE_URL`, `ENERGY_MONITORING_SERVICE_URL`

IAM no depende de esas variables para su logica actual.
