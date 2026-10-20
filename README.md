# Innoshop Backend

Backend of **Innoshop**, an online shop built as a training project at Innowise Group.

## Current scope

- Spring Boot REST API
- One endpoint: `GET /api/products`
- Product data is hard-coded in memory (no database yet)

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

The application starts on <http://localhost:8080>.

## API

### `GET /api/products`

Returns the list of products as JSON.

```json
[
  {
    "id": 1,
    "name": "Laptop Pro 14",
    "price": 1299.99,
    "description": "Lightweight laptop for study and work"
  }
]
```

## Project structure

```
src/main/java/com/innowise/academy/innoshop/
├── InnoshopApplication.java
├── controller/   REST controllers
└── model/        domain classes
```

## Not included yet

Database persistence, order handling and payments.