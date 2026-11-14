# GraphQL Book API

A Spring Boot API for creating, viewing, updating, and deleting books over GraphQL and MCP.

## Tech Stack

- Java 21
- Spring Boot
- GraphQL
- Spring AI 2.0 (MCP server and OpenAI)
- PostgreSQL
- JPA/Hibernate
- Docker

## Features

- Create books
- View books
- Update books
- Delete books
- Expose the same operations as MCP tools

## Architecture

```
Client
  ↓
GraphQL or MCP
  ↓
Spring Boot
  ↓
BookService
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

GraphQL is available at `http://localhost:8080/graphql`. GraphiQL is available at `http://localhost:8080/graphiql`. The MCP server is available at `http://localhost:8080/mcp`.

The OpenAI starter reads the API key from `SPRING_AI_OPENAI_API_KEY` or `spring.ai.openai.api-key`. That key is only needed when this application calls OpenAI. Claude and ChatGPT do not use it when they call the book tools.

## MCP tools

`BookTools` uses `BookService` and registers these tools with the Spring AI MCP server. The server uses the Streamable HTTP transport.

| Tool | Arguments | Result |
|---|---|---|
| `listBooks` | none | All books |
| `getBook` | `id` | One book. Missing ids are reported as not found |
| `createBook` | `title`, `author`, `price` | The created book |
| `updateBook` | `id`, `title`, `author`, `price` | The updated book. Missing ids are reported as not found |
| `deleteBook` | `id` | `true` when a book was deleted, `false` when the id is missing |

## Connect Claude or ChatGPT

Deploy the application, then use the public HTTPS endpoint:

`https://<your-service>.onrender.com/mcp`

Wake a free Render service in a browser before connecting. The first request after sleep can time out while the JVM starts.

### Claude

Claude's free plan can add one custom connector.

1. Open **Customize → Connectors → Add → Custom → Web**.
2. Paste the `/mcp` URL.
3. Choose **No sign in**.
4. In a chat, open the **+** menu and turn the connector on.

Claude can then list, get, create, update, and delete books. It asks before each tool call unless you set that tool to always allow.

### ChatGPT

ChatGPT web on Plus, Pro, Business, Enterprise, or Education can add the same URL as a custom MCP server. Turn on Developer mode, then open **Plugins → Add custom MCP server**, paste the `/mcp` URL, and choose **No authentication**.

The `/mcp` endpoint has no login of its own. Anyone who knows the URL can call these tools, including `deleteBook`.

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
