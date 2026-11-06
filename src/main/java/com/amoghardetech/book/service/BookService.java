package com.amoghardetech.book.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.amoghardetech.book.entity.Book;
import com.amoghardetech.book.repository.BookRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {

	private final BookRepository bookRepository;

	public List<Book> findAll() {
		return bookRepository.findAll();
	}

	public Optional<Book> findById(Long id) {
		return bookRepository.findById(id);
	}

	public Book create(String title, String author, Double price) {
		return bookRepository.save(new Book(null, title, author, price));
	}

	public Book update(Long id, String title, String author, Double price) {
		Book book = bookRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + id));
		book.setTitle(title);
		book.setAuthor(author);
		book.setPrice(price);
		return bookRepository.save(book);
	}

	public boolean delete(Long id) {
		if (!bookRepository.existsById(id)) {
			return false;
		}
		bookRepository.deleteById(id);
		return true;
	}

}
