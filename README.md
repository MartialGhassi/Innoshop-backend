# Innoshop Backend

Backend of **Innoshop**, an online shop built as a training project at Innowise Group.

## Current scope

- Spring Boot REST API
- Endpoints: `GET /api/products` and `GET /api/products/{id}`
- Layered architecture: controller → service → repository
- Product data is hard-coded in memory (no database yet)
- Errors are returned as a uniform JSON body

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

### `GET /api/products/{id}`

Returns a single product.

`GET /api/products/2` → `200 OK`

```json
{
  "id": 2,
  "name": "Wireless Mouse",
  "price": 24.90,
  "description": "Ergonomic Bluetooth mouse"
}
```

`GET /api/products/99` → `404 Not Found`

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Product with id 99 not found"
}
```

## Project structure

```
src/main/java/com/innowise/academy/innoshop/
├── InnoshopApplication.java
├── controller/   REST controllers (HTTP layer)
├── service/      business logic (interfaces + impl/)
├── repository/   data access (interfaces + impl/, in-memory for now)
├── exception/    custom exceptions and global exception handler
└── model/        domain classes and response records
```

## Not included yet

Database persistence, order handling and payments.