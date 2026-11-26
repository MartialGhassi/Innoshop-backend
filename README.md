# User Service

User Service of the **Innowise Java Advanced** training course. It manages user profiles and their payment cards and is the first microservice of the course architecture (User, Auth, Order and Payment services behind an API Gateway).

## Current scope

- Runs on port `8081` (port `8080` is reserved for the API Gateway)
- PostgreSQL 17 running in Docker Compose, connection settings read from environment variables
- Database schema managed by Liquibase: tables `users` and `payment_cards`
- Spring Data JPA entities with auditing (`created_at`, `updated_at`), Specifications and pagination
- DTOs mapped with MapStruct, Bean Validation on request bodies
- Business rule: at most 5 active cards per user (checked on creation and on re-activation)
- REST API for users and payment cards
- Global exception handling with `@RestControllerAdvice` and a uniform JSON error body
- Redis cache for `GET /api/users/{id}` (user with their cards), evicted on every change
- Unit tests (JUnit 5, Mockito) and integration tests (Testcontainers, MockMvc), JaCoCo coverage rule
- Multi-stage Dockerfile, `local` and `docker` Spring profiles, whole stack runnable with Docker Compose

## Planned features

- CI pipeline with GitHub Actions and SonarCloud

## Tech stack

- Java 25
- Spring Boot 4.1.1 (Spring Web MVC, Validation, Spring Data JPA)
- MapStruct
- PostgreSQL 17 (Docker Compose)
- Redis 7 with Spring Cache (Docker Compose)
- Liquibase
- Gradle (wrapper included)
- Lombok
- Spring Boot DevTools (development only)

## Getting started

Requirements: JDK 25, Git and Docker. Gradle is not needed, the wrapper is included.

```bash
git clone https://github.com/MartialGhassi/Innoshop-backend.git
cd Innoshop-backend
cp .env.example .env
docker compose up -d postgres redis
./gradlew bootRun
```

On Windows (PowerShell): `Copy-Item .env.example .env`, then `.\gradlew.bat bootRun`.

`.env` holds the database settings (`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`), the Redis port (`REDIS_PORT`) and the port of the application container (`APP_PORT`). It is read by Docker Compose and imported by Spring Boot, and it is ignored by Git.

Without an active profile the application uses the `local` profile (see [Docker](#docker)).

On startup Liquibase creates the tables. The application starts on <http://localhost:8081>.

Check the database and the cache:

```bash
docker compose ps
docker exec -it user-service-db psql -U user_service -d user_service -c "\dt"
docker exec -it user-service-cache redis-cli ping
```

## REST API

Base URL: `http://localhost:8081`

### Users

| Method | Path | Description | Success |
|---|---|---|---|
| `POST` | `/api/users` | Create a user | `201 Created` + `Location` header |
| `GET` | `/api/users?name=&surname=&page=&size=&sort=` | Paginated list, optional filters on name/surname (contains, case-insensitive) | `200 OK` |
| `GET` | `/api/users/{id}` | Get a user with their payment cards | `200 OK` |
| `PUT` | `/api/users/{id}` | Update a user | `200 OK` |
| `DELETE` | `/api/users/{id}` | Delete a user and all their cards | `204 No Content` |
| `PATCH` | `/api/users/{id}/status` | Activate or deactivate a user | `204 No Content` |

### Payment cards

| Method | Path | Description | Success |
|---|---|---|---|
| `POST` | `/api/users/{userId}/cards` | Add a card to a user | `201 Created` + `Location: /api/cards/{id}` |
| `GET` | `/api/users/{userId}/cards` | List the cards of a user | `200 OK` |
| `GET` | `/api/cards/{id}` | Get a card | `200 OK` |
| `PUT` | `/api/cards/{id}` | Update a card | `200 OK` |
| `DELETE` | `/api/cards/{id}` | Delete a card | `204 No Content` |
| `PATCH` | `/api/cards/{id}/status` | Activate or deactivate a card | `204 No Content` |

### Examples

```bash
curl -i -X POST http://localhost:8081/api/users   -H "Content-Type: application/json"   -d '{"name":"Anna","surname":"Smith","birthDate":"1995-05-10","email":"anna.smith@example.com"}'

curl -i -X POST http://localhost:8081/api/users/1/cards   -H "Content-Type: application/json"   -d '{"number":"4000000000000001","holder":"ANNA SMITH","expirationDate":"2030-01-01"}'

curl -i -X PATCH http://localhost:8081/api/cards/1/status   -H "Content-Type: application/json"   -d '{"active":false}'
```

Only synthetic test data must be used, never real card numbers.

## Error handling

Every error is returned with the same JSON body:

```json
{
  "timestamp": "2026-11-21T02:01:09.776",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "path": "/api/users",
  "fieldErrors": {
    "name": "must not be blank",
    "email": "must be a well-formed email address"
  }
}
```

| Status | When |
|---|---|
| `400 Bad Request` | Invalid request body (`fieldErrors` lists the invalid fields), malformed JSON, path variable of the wrong type (e.g. `/api/users/abc`) |
| `404 Not Found` | Unknown user or card, unknown endpoint |
| `405 Method Not Allowed` | HTTP method not supported by the endpoint (e.g. `PUT /api/users/{id}/status`) |
| `415 Unsupported Media Type` | Request body that is not JSON |
| `409 Conflict` | Email already used, card number already used, user already has 5 active cards |
| `500 Internal Server Error` | Unexpected error (logged, no internal details in the response) |

All messages are defined in the `ErrorCode` enum.

## Caching

`GET /api/users/{id}` is cached in Redis (cache `users`, key = user id, e.g. `users::1`). The cached value is the `UserDetailsResponse` DTO, the user with their payment cards, stored as JSON.

| Operation | Effect on the cache |
|---|---|
| `GET /api/users/{id}` | Read from Redis if present, otherwise loaded from PostgreSQL and stored |
| `PUT`, `DELETE`, `PATCH .../status` on a user | Entry of this user evicted |
| `POST`, `PUT`, `DELETE`, `PATCH .../status` on a card | Entry of the card's owner evicted |

- Entries expire after 10 minutes (`spring.cache.redis.time-to-live`).
- Evictions are applied only after the database transaction commits.
- Unknown users (`404`) are never cached.

Inspect the cache:

```bash
docker exec -it user-service-cache redis-cli KEYS "users::*"
docker exec -it user-service-cache redis-cli GET users::1
docker exec -it user-service-cache redis-cli TTL users::1
```

## Tests

Requirements: Docker must be running (the integration tests start their own containers).

```bash
./gradlew check
```

- **Unit tests** (`service/impl`): JUnit 5 and Mockito, repositories and mappers are mocked.
- **Integration tests** (`controller`): the full application is started with MockMvc against real PostgreSQL and Redis containers provided by Testcontainers (`@ServiceConnection`), the local `docker compose` stack is not needed.
- **Coverage**: JaCoCo report in `build/reports/jacoco/test/html/index.html`. `check` fails if a class in `service.impl` has less than 80 % line coverage.

## Docker

### Spring profiles

| Profile | Activated | PostgreSQL | Redis |
|---|---|---|---|
| `local` | by default (`spring.profiles.default`), e.g. `bootRun` or IntelliJ | `localhost:${POSTGRES_PORT}` | `localhost:${REDIS_PORT}` |
| `docker` | by the image (`SPRING_PROFILES_ACTIVE=docker`) | `postgres:5432` | `redis:6379` |

Inside a container `localhost` is the container itself, so the `docker` profile reaches PostgreSQL and Redis by their Compose service names on the Docker network.

### Image

The `Dockerfile` is multi-stage:

1. **build**: `eclipse-temurin:25-jdk-alpine`, downloads the dependencies (cached layer), then builds the jar with `./gradlew bootJar`. Tests are not run here, they need Docker themselves (run `./gradlew check` locally).
2. **runtime**: `eclipse-temurin:25-jre-alpine`, contains only the jar and runs it as the non-root user `app`.

`.dockerignore` keeps `.env`, `.git`, build output and IDE files out of the build context, so no secret ends up in the image.

### Run the whole stack

```bash
docker compose up -d --build
```

Starts PostgreSQL, Redis and the application (`user-service-app`). The application waits until both are healthy and is available on <http://localhost:8081> (`APP_PORT`). Stop the application or the `bootRun` instance first if port `8081` is already in use, or set another `APP_PORT` in `.env`.

```bash
docker compose logs -f app     # follow the application logs
docker compose down            # stop and remove the containers (data volume is kept)
```

### Run the image without Compose for the application

```bash
docker compose up -d postgres redis
docker build -t user-service .
docker run --rm --network user-service_default --env-file .env -p 127.0.0.1:8081:8081 user-service
```

`user-service_default` is the network created by Compose. Its name comes from the project name set in `compose.yaml` (`name: user-service`), so it does not depend on the name of the cloned folder.

## Project structure

```
Dockerfile                        multi-stage build of the application image
.dockerignore                     files excluded from the Docker build context
compose.yaml                      PostgreSQL, Redis and application containers
.env.example                      template for the local .env file
src/main/java/com/innowise/academy/userservice/
├── UserServiceApplication.java   Spring Boot entry point
├── config/                       JPA auditing, Page serialization, Redis cache
├── controller/                   UserController, PaymentCardController
├── service/                      service interfaces
│   └── impl/                     service implementations (business rules, transactions)
├── repository/                   Spring Data JPA repositories
│   └── specification/            filters for the paginated queries
├── model/
│   ├── entity/                   JPA entities (BaseEntity, UserEntity, PaymentCardEntity)
│   ├── dto/                      request/response records, ErrorResponse
│   └── mapper/                   MapStruct mappers
└── exception/                    custom exceptions, ErrorCode, GlobalExceptionHandler
src/main/resources/
├── application.properties        common settings
├── application-local.properties  local profile (localhost)
├── application-docker.properties docker profile (Compose service names)
└── db/changelog/
    ├── db.changelog-master.yaml  includes one changelog per task
    └── changes/<n>/              changelog.yml + SQL scripts of one task (e.g. 1 = USR-1)
```
