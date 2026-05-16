# MiCultura Backend

REST API for **MiCultura**, a platform that lists cultural events across Tenerife. Consumed by the [frontend repo](https://github.com/SomosDeWeb/pi-25-26-frontend-opal).

---

## Tech stack

- **Java 17** / **Spring Boot 3.3.6**
- **Spring Web**, **Spring Security**, **Spring Data JPA**, **Spring Validation**
- **PostgreSQL** 14+ (via Hibernate)
- **JJWT 0.12.6** for access-token signing
- **Lombok**, **Maven**

---

## Project layout

```
src/main/java/com/micultura/backend/
├── BackendApplication.java       # Spring Boot entry point
├── config/                       # Security, CORS, optional data seeding
├── controller/                   # REST controllers under /api
├── dto/                          # Request/response payloads
├── entity/                       # JPA entities
├── exception/                    # Global handler + ResourceNotFoundException
├── repository/                   # Spring Data repositories
├── security/                     # JWT service + auth filter
├── service/                      # Business logic
└── spec/                         # JPA Specifications for filtering
```

---

## Prerequisites

- Java 17+
- PostgreSQL 14+ running locally with a database named `micultura`
- Maven (the wrapper `mvnw` is included, so a local install isn't strictly required)

```sql
CREATE DATABASE micultura;
```

---

## Configuration

Configuration lives in `src/main/resources/application.properties`. Sensitive values (DB password, JWT secret) are **required** and have no defaults — the app will fail to start without them.

### Local setup

Copy `.env.example` to `.env` at the repo root and fill in your values:

```bash
cp .env.example .env
```

The `spring-dotenv` library loads `.env` at startup and exposes its entries as Spring properties, so you don't need to export anything in your shell. `.env` is gitignored.

In production, set the same variables through your platform's environment configuration (Docker env, systemd, Kubernetes secrets, etc.) — `.env` is not used outside local dev.

### All properties

| Property                                   | Env var                          | Default                          | Notes |
|--------------------------------------------|----------------------------------|----------------------------------|-------|
| `spring.datasource.url`                    | `SPRING_DATASOURCE_URL`          | `jdbc:postgresql://localhost:5432/micultura` | |
| `spring.datasource.username`               | `SPRING_DATASOURCE_USERNAME`     | `postgres`                       | |
| `spring.datasource.password`               | `SPRING_DATASOURCE_PASSWORD`     | *(set per environment)*          | Required |
| `spring.jpa.hibernate.ddl-auto`            | `APP_DB_DDL_AUTO`                | `update`                         | Use `validate` in prod |
| `app.jwt.secret`                           | `APP_JWT_SECRET`                 | *(set per environment)*          | UTF-8 string, ≥ 32 chars |
| `app.jwt.access-expiration-ms`             | `APP_JWT_ACCESS_EXPIRATION_MS`   | `900000` (15 min)                | |
| `app.jwt.refresh-expiration-ms`            | `APP_JWT_REFRESH_EXPIRATION_MS`  | `604800000` (7 days)             | |
| `app.jwt.refresh-remember-expiration-ms`   | `APP_JWT_REFRESH_REMEMBER_EXPIRATION_MS` | `2592000000` (30 days)  | |
| `app.cors.allowed-origins`                 | `APP_CORS_ALLOWED_ORIGINS`       | `http://localhost:3000`          | Comma-separated, supports `https://*.example.com` patterns |
| `app.security.cookie-secure`               | `APP_SECURITY_COOKIE_SECURE`     | `false`                          | Must be `true` over HTTPS |
| `app.security.cookie-same-site`            | `APP_SECURITY_COOKIE_SAME_SITE`  | `Lax`                            | `None` for cross-site frontends |
| `app.seed.enabled`                         | `APP_SEED_ENABLED`               | unset                            | `true` to run `DataSeeder` (local only) |

---

## Running locally

```bash
./mvnw spring-boot:run
```

Server listens on `http://localhost:8080`. With `app.seed.enabled=true` on first boot, `DataSeeder` inserts six categories and ~25 Tenerife events.

Build a jar:

```bash
./mvnw clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

---

## Authentication

- `POST /api/auth/register` — create account, returns access token + sets `mc_refresh` HttpOnly cookie
- `POST /api/auth/login` — sign in (body: `{ email, password, rememberMe }`)
- `POST /api/auth/refresh` — rotate refresh token, returns new access token (reads `mc_refresh` cookie)
- `POST /api/auth/logout` — revoke refresh token, clears cookie

Access tokens are short-lived (15 min) and sent as `Authorization: Bearer <token>`. Refresh tokens are rotated on every use, hashed at rest (SHA-256), and revoked individually.

To promote a user to admin:

```sql
UPDATE users SET rol = 'ADMIN' WHERE email = 'you@example.com';
```

The user must log in again to receive a JWT containing the new role.

---

## API endpoints

### Events (`/api/events`)

| Method | Path                | Auth   | Description |
|--------|---------------------|--------|-------------|
| GET    | `/api/events`       | public | Paginated list with filters |
| GET    | `/api/events/{id}`  | public | Single event by id |
| POST   | `/api/events`       | ADMIN  | Create event |
| PUT    | `/api/events/{id}`  | ADMIN  | Update event |
| DELETE | `/api/events/{id}`  | ADMIN  | Soft-delete event |

Supported query parameters on `GET /api/events`:

- `category` — categoria id
- `search` — case-insensitive match on title / description
- `fechaDesde`, `fechaHasta` — ISO dates (`2026-06-01`)
- `precioMin`, `precioMax` — decimal
- `page` (default `0`), `size` (default `20`, max `100`)
- `sort` — `field,direction`. Allowed fields: `fecha`, `precio`, `titulo`. Default `fecha,asc`.

### Categories (`/api/categories`)

| Method | Path                    | Auth   | Description |
|--------|-------------------------|--------|-------------|
| GET    | `/api/categories`       | public | List all categories |
| GET    | `/api/categories/{id}`  | public | Single category by id |

### Users (`/api/users`)

| Method | Path             | Auth   | Description |
|--------|------------------|--------|-------------|
| GET    | `/api/users/me`  | USER+  | Profile of the authenticated user |

---

## Error format

All errors return JSON:

```json
{ "message": "Evento no encontrado: 42" }
```

Validation errors add a `errors` map keyed by field name:

```json
{
  "message": "Error de validación",
  "errors": { "email": "El email es obligatorio" }
}
```

Status codes follow the usual conventions: `400` for validation, `401` for missing/invalid auth, `403` for forbidden, `404` for missing resources, `500` for everything else.
