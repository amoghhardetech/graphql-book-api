package com.amoghardetech.book.web;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

	@GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String home(HttpServletRequest request) {
		String homeUrl = request.getRequestURL().toString();
		String baseUrl = homeUrl.endsWith("/") ? homeUrl.substring(0, homeUrl.length() - 1) : homeUrl;
		String graphqlUrl = baseUrl + "/graphql";
		String graphiqlUrl = baseUrl + "/graphiql";

		return """
				<!DOCTYPE html>
				<html lang="en">
				<head>
				  <meta charset="utf-8">
				  <meta name="viewport" content="width=device-width, initial-scale=1">
				  <title>GraphQL Book API</title>
				  <style>
				    body { font-family: Georgia, serif; margin: 2rem auto; max-width: 42rem; line-height: 1.5; color: #1c1917; background: #ffffff; }
				    code, pre { font-family: ui-monospace, monospace; }
				    code { background: #f5f5f4; padding: 0.1rem 0.3rem; }
				    pre { background: #f5f5f4; padding: 0.8rem; overflow: auto; }
				    a { color: #1d4ed8; }
				    footer { margin-top: 2.5rem; padding-top: 1rem; border-top: 1px solid #e7e5e4; display: flex; align-items: center; gap: 0.75rem; }
				    footer img { height: 2.5rem; }
				  </style>
				</head>
				<body>
				  <h1>GraphQL Book API</h1>
				  <p>The application is running.</p>
				  <p>Home URL: <a href="%s">%s</a></p>
				  <h2>Endpoints</h2>
				  <ul>
				    <li>GraphQL HTTP endpoint: <code>POST %s</code></li>
				    <li>GraphiQL: <a href="%s">%s</a></li>
				  </ul>
				  <h2>Queries</h2>
				  <ul>
				    <li><code>books: [Book!]!</code> lists every book.</li>
				    <li><code>book(id: ID!): Book</code> returns one book, or null when the id is missing.</li>
				  </ul>
				  <h2>Mutations</h2>
				  <ul>
				    <li><code>createBook(input: BookInput!): Book!</code> creates a book.</li>
				    <li><code>updateBook(id: ID!, input: BookInput!): Book!</code> updates a book.</li>
				    <li><code>deleteBook(id: ID!): Boolean!</code> deletes a book and returns false when the id is missing.</li>
				  </ul>
				  <p><code>BookInput</code> requires <code>title</code>, <code>author</code>, and <code>price</code>.</p>
				  <h2>Example</h2>
				  <pre>POST %s
				Content-Type: application/json

				{
				  "query": "query { books { id title author price } }"
				}</pre>
				  <footer>
				    <span>This project is created by</span>
				    <img src="%s/amogh.gif" alt="Amogh">
				  </footer>
				</body>
				</html>
				""".formatted(
				escape(homeUrl), escape(homeUrl),
				escape(graphqlUrl),
				escape(graphiqlUrl), escape(graphiqlUrl),
				escape(graphqlUrl),
				escape(baseUrl));
	}

	private static String escape(String value) {
		return value.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;");
	}

}
