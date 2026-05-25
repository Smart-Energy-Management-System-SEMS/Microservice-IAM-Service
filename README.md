# Microservice-IAM

## Supabase Postgres configuration

This service expects database settings via environment variables. No secrets should be committed.

### Local development (.env)

Create a `.env` file at the project root (same folder as `pom.xml`) with properties format:

```
SUPABASE_DB_HOST=db.<your-project-ref>.supabase.co
SUPABASE_DB_PORT=5432
SUPABASE_DB_NAME=postgres
SUPABASE_DB_USER=postgres
SUPABASE_DB_PASSWORD=your-password
SUPABASE_DB_SSLMODE=require
```

The app loads `.env` automatically via `spring.config.import` in [src/main/resources/application.properties](src/main/resources/application.properties).

### Production environment

Set the same variables in your deployment environment (CI/CD, container, or app service). Example names:

- `SUPABASE_DB_HOST`
- `SUPABASE_DB_PORT`
- `SUPABASE_DB_NAME`
- `SUPABASE_DB_USER`
- `SUPABASE_DB_PASSWORD`
- `SUPABASE_DB_SSLMODE`

You can also use the fallback names `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_SSLMODE` if preferred.