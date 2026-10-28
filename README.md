# User Service

User Service of the **Innowise Java Advanced** training course. It manages user profiles and their payment cards and is the first microservice of the course architecture (User, Auth, Order and Payment services behind an API Gateway).

## Current scope

- Spring Boot project skeleton (`com.innowise.academy.userservice`)
- Runs on port `8081` (port `8080` is reserved for the API Gateway)
- PostgreSQL 17 running in Docker Compose, connection settings read from environment variables
- Database schema managed by Liquibase: tables `users` and `payment_cards`
- No business endpoints yet

## Planned features

- Spring Data JPA with auditing, Specifications and pagination
- DTOs mapped with MapStruct, Bean Validation
- Business rule: at most 5 active cards per user
- REST API for users (`/api/users`) and cards (`/api/users/{userId}/cards`, `/api/cards`)
- Global exception handling with `@ControllerAdvice`
- Redis caching of `getUserById`
- Unit tests (JUnit 5, Mockito) and integration tests (Testcontainers)
- Dockerfile, `local` and `docker` Spring profiles
- CI pipeline with GitHub Actions and SonarCloud

## Tech stack

- Java 25
- Spring Boot 4.1.1 (Spring Web MVC, Validation, Spring Data JPA)
- PostgreSQL 17 (Docker Compose)
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
docker compose up -d
./gradlew bootRun
```

On Windows (PowerShell): `Copy-Item .env.example .env`, then `.\gradlew.bat bootRun`.

`.env` holds the database settings (`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`). It is read by Docker Compose and imported by Spring Boot, and it is ignored by Git.

On startup Liquibase creates the tables. The application starts on <http://localhost:8081>.

Check the database:

```bash
docker compose ps
docker exec -it user-service-db psql -U user_service -d user_service -c "\dt"
```

## Project structure

```
compose.yaml                      PostgreSQL container
.env.example                      template for the local .env file
src/main/java/com/innowise/academy/userservice/
└── UserServiceApplication.java   Spring Boot entry point
src/main/resources/
├── application.properties
└── db/changelog/
    ├── db.changelog-master.yaml  Liquibase changeSets
    └── changes/                  SQL scripts (one folder per changeSet)
```

Packages (`controller`, `service`, `repository`, `model`, `config`, `exception`) are added with the features that need them.
