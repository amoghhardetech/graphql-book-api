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

## GraphQL examples

These requests were run against `POST http://localhost:8080/graphql` on the local server. Send each body as JSON with `Content-Type: application/json`.

### Create

```json
{
  "query": "mutation CreateBook($input: BookInput!) { createBook(input: $input) { id title author price } }",
  "variables": {
    "input": {
      "title": "The Pragmatic Programmer",
      "author": "Andrew Hunt",
      "price": 39.99
    }
  }
}
```

```json
{
  "data": {
    "createBook": {
      "id": "2",
      "title": "The Pragmatic Programmer",
      "author": "Andrew Hunt",
      "price": 39.99
    }
  }
}
```

### Read one

```json
{
  "query": "query Book($id: ID!) { book(id: $id) { id title author price } }",
  "variables": { "id": "2" }
}
```

```json
{
  "data": {
    "book": {
      "id": "2",
      "title": "The Pragmatic Programmer",
      "author": "Andrew Hunt",
      "price": 39.99
    }
  }
}
```

### Read all

```json
{
  "query": "query Books { books { id title author price } }"
}
```

```json
{
  "data": {
    "books": [
      {
        "id": "1",
        "title": "The Pragmatic Programmer",
        "author": "Andrew Hunt",
        "price": 39.99
      },
      {
        "id": "2",
        "title": "The Pragmatic Programmer",
        "author": "Andrew Hunt",
        "price": 39.99
      }
    ]
  }
}
```

### Update

```json
{
  "query": "mutation UpdateBook($id: ID!, $input: BookInput!) { updateBook(id: $id, input: $input) { id title author price } }",
  "variables": {
    "id": "2",
    "input": {
      "title": "The Pragmatic Programmer",
      "author": "David Thomas",
      "price": 42.5
    }
  }
}
```

```json
{
  "data": {
    "updateBook": {
      "id": "2",
      "title": "The Pragmatic Programmer",
      "author": "David Thomas",
      "price": 42.5
    }
  }
}
```

Reading book `2` again returned the updated author and price:

```json
{
  "data": {
    "book": {
      "id": "2",
      "title": "The Pragmatic Programmer",
      "author": "David Thomas",
      "price": 42.5
    }
  }
}
```

### Delete

```json
{
  "query": "mutation DeleteBook($id: ID!) { deleteBook(id: $id) }",
  "variables": { "id": "2" }
}
```

```json
{
  "data": {
    "deleteBook": true
  }
}
```

Reading book `2` after deletion returned:

```json
{
  "data": {
    "book": null
  }
}
```
