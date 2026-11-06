package com.amoghardetech.book.graphql;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.amoghardetech.book.entity.Book;
import com.amoghardetech.book.service.BookService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BookController {

	private final BookService bookService;

	@QueryMapping
	public List<Book> books() {
		return bookService.findAll();
	}

	@QueryMapping
	public Book book(@Argument Long id) {
		return bookService.findById(id).orElse(null);
	}

	@MutationMapping
	public Book createBook(@Argument BookInput input) {
		return bookService.create(input.title(), input.author(), input.price());
	}

	@MutationMapping
	public Book updateBook(@Argument Long id, @Argument BookInput input) {
		return bookService.update(id, input.title(), input.author(), input.price());
	}

	@MutationMapping
	public boolean deleteBook(@Argument Long id) {
		return bookService.delete(id);
	}

}
