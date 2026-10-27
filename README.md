# User Service

User Service of the **Innowise Java Advanced** training course. It manages user profiles and their payment cards and is the first microservice of the course architecture (User, Auth, Order and Payment services behind an API Gateway).

## Current scope

- Spring Boot project skeleton (`com.innowise.academy.userservice`)
- Runs on port `8081` (port `8080` is reserved for the API Gateway)
- No business endpoints yet

## Planned features

- PostgreSQL schema managed by Liquibase: `users` and `payment_cards`
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
- Spring Boot 4.1.1 (Spring Web MVC, Validation)
- Gradle (wrapper included)
- Lombok
- Spring Boot DevTools (development only)

## Getting started

Requirements: JDK 25 and Git. Gradle is not needed, the wrapper is included.

```bash
git clone https://github.com/MartialGhassi/Innoshop-backend.git
cd Innoshop-backend
./gradlew bootRun
```

On Windows (PowerShell): `.\gradlew.bat bootRun`

The application starts on <http://localhost:8081>.

## Project structure

```
src/main/java/com/innowise/academy/userservice/
└── UserServiceApplication.java   Spring Boot entry point
```

Packages (`controller`, `service`, `repository`, `model`, `config`, `exception`) are added with the features that need them.
