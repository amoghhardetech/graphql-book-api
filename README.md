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
- OAuth 2.1 for `/mcp` and `/graphql`

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

Local sign-in uses `book` / `change-me` unless you set `BOOK_USER` and `BOOK_PASSWORD`. Both `/graphql` and `/mcp` require that login. The home page does not.

The OpenAI starter reads the API key from `SPRING_AI_OPENAI_API_KEY` or `spring.ai.openai.api-key`. That key is only needed when this application calls OpenAI. Claude and ChatGPT do not use it when they call the book tools.

## MCP tools

`BookTools` uses `BookService` and registers these tools with the Spring AI MCP server. The server uses stateless Streamable HTTP, so each request stands alone and clients can list the tools without a session id. `GET /mcp` with `Accept: text/event-stream` stays open as an event stream. Claude opens that stream before loading tools, and a 405 response makes Claude report that the tools could not be loaded.

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

`/mcp` requires OAuth. Claude's free plan can add one custom connector.

Set these on Render before connecting. `APP_PUBLIC_URL` must be the exact site origin, with no path and no trailing slash. Change `BOOK_PASSWORD` from the local default `change-me`.

```text
APP_PUBLIC_URL=https://<your-service>.onrender.com
BOOK_USER=book
BOOK_PASSWORD=<a password you choose>
```

1. Open **Customize → Connectors → Add → Custom → Web**.
2. Paste the `/mcp` URL.
3. Choose OAuth, then **Use your own OAuth client**.
4. Enter client ID `claude` and leave the client secret empty. The redirect URI registered for that client is `https://claude.ai/api/mcp/auth_callback`.
5. Sign in with `BOOK_USER` and `BOOK_PASSWORD`, then approve the `books` scope.
6. In a chat, open the **+** menu and turn the connector on.

Claude can then list, get, create, update, and delete books. It asks before each tool call unless you set that tool to always allow. A restart of the service creates a new signing key, so sign in again after a restart.

### How a call is authenticated

There is one user account and one OAuth client. You are the user. Claude is the client, with id `claude` and no client secret.

1. Claude calls `/mcp` with no token. The server answers `401` and tells Claude where to send you.
2. Your browser opens the login page. You enter `BOOK_USER` and `BOOK_PASSWORD`, then approve the `books` scope.
3. The server gives Claude an access token that lasts 8 hours and a refresh token that lasts 30 days. You do not copy the token yourself.
4. Each book operation is a new call to `/mcp` with `Authorization: Bearer <access token>`. The server checks the token's signature, issuer, and audience, then runs `listBooks`, `getBook`, `createBook`, `updateBook`, or `deleteBook`.
5. The same token works for `POST /graphql`. Opening GraphiQL in a browser redirects to the same login page and then keeps a session cookie.

The access token can be refreshed until the refresh token expires. After a restart, the signing key is new, so the old token is rejected and Claude must send you through login again.

### ChatGPT

`/mcp` and `/graphql` both reject calls that do not send a bearer token from this login. Opening GraphiQL in a browser redirects to the same sign-in page. The home page stays open.

ChatGPT cannot use **No authentication** anymore. The registered OAuth client only allows Claude's redirect URI, `https://claude.ai/api/mcp/auth_callback`.

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
