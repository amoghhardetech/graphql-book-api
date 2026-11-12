package com.amoghardetech.book.mcp;

import java.util.List;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.amoghardetech.book.entity.Book;
import com.amoghardetech.book.service.BookService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BookTools {

	private final BookService bookService;

	@McpTool(name = "listBooks", description = "List all books", generateOutputSchema = true, annotations = @McpTool.McpAnnotations(title = "List books", readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
	public List<Book> listBooks() {
		return bookService.findAll();
	}

	@McpTool(name = "getBook", description = "Get a book by id", generateOutputSchema = true, annotations = @McpTool.McpAnnotations(title = "Get book", readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
	public Book getBook(@McpToolParam(description = "Book id", required = true) Long id) {
		return bookService.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + id));
	}

	@McpTool(name = "createBook", description = "Create a book", generateOutputSchema = true, annotations = @McpTool.McpAnnotations(title = "Create book", readOnlyHint = false, destructiveHint = false, idempotentHint = false, openWorldHint = false))
	public Book createBook(@McpToolParam(description = "Book title", required = true) String title,
			@McpToolParam(description = "Book author", required = true) String author,
			@McpToolParam(description = "Book price", required = true) Double price) {
		return bookService.create(title, author, price);
	}

	@McpTool(name = "updateBook", description = "Update a book", generateOutputSchema = true, annotations = @McpTool.McpAnnotations(title = "Update book", readOnlyHint = false, destructiveHint = false, idempotentHint = true, openWorldHint = false))
	public Book updateBook(@McpToolParam(description = "Book id", required = true) Long id,
			@McpToolParam(description = "Book title", required = true) String title,
			@McpToolParam(description = "Book author", required = true) String author,
			@McpToolParam(description = "Book price", required = true) Double price) {
		return bookService.update(id, title, author, price);
	}

	@McpTool(name = "deleteBook", description = "Delete a book by id", annotations = @McpTool.McpAnnotations(title = "Delete book", readOnlyHint = false, destructiveHint = true, idempotentHint = true, openWorldHint = false))
	public boolean deleteBook(@McpToolParam(description = "Book id", required = true) Long id) {
		return bookService.delete(id);
	}

}
