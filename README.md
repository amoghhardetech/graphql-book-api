# GraphQL Book API

A Spring Boot GraphQL API for creating, viewing, updating, and deleting books.

## Tech Stack

- Java 21
- Spring Boot
- GraphQL
- PostgreSQL
- JPA/Hibernate
- Docker

## Features

- Create books
- View books
- Update books
- Delete books

## Architecture

```
Client
  ↓
GraphQL
  ↓
Spring Boot
  ↓
JPA/Hibernate
  ↓
PostgreSQL
```

## Getting Started

Start PostgreSQL:

```bash
docker run --name book-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=bookdb \
  -p 5432:5432 \
  -d postgres
```

Start the API:

```bash
./gradlew bootRun
```

GraphQL is available at `http://localhost:8080/graphql`. GraphiQL is available at `http://localhost:8080/graphiql`.
