package com.amoghardetech.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.amoghardetech.book.entity.Book;
import com.amoghardetech.book.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

	@Mock
	private BookRepository bookRepository;

	@InjectMocks
	private BookService bookService;

	@Test
	void findAllReturnsBooksFromTheRepository() {
		Book book = new Book(1L, "Dune", "Frank Herbert", 18.99);
		when(bookRepository.findAll()).thenReturn(List.of(book));

		assertThat(bookService.findAll()).containsExactly(book);
	}

	@Test
	void findByIdReturnsEmptyWhenTheBookIsMissing() {
		when(bookRepository.findById(9L)).thenReturn(Optional.empty());

		assertThat(bookService.findById(9L)).isEmpty();
	}

	@Test
	void createSavesANewBook() {
		when(bookRepository.save(any(Book.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		Book created = bookService.create("Dune", "Frank Herbert", 18.99);

		ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
		verify(bookRepository).save(captor.capture());
		assertThat(captor.getValue().getId()).isNull();
		assertThat(created.getTitle()).isEqualTo("Dune");
		assertThat(created.getAuthor()).isEqualTo("Frank Herbert");
		assertThat(created.getPrice()).isEqualTo(18.99);
	}

	@Test
	void updateChangesAnExistingBook() {
		Book existing = new Book(1L, "Dune", "Frank Herbert", 18.99);
		when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(bookRepository.save(existing)).thenReturn(existing);

		Book updated = bookService.update(1L, "Dune Messiah", "Frank Herbert", 16.5);

		assertThat(updated.getTitle()).isEqualTo("Dune Messiah");
		assertThat(updated.getPrice()).isEqualTo(16.5);
	}

	@Test
	void updateRejectsAMissingBook() {
		when(bookRepository.findById(9L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookService.update(9L, "Dune", "Frank Herbert", 18.99))
				.isInstanceOf(ResponseStatusException.class)
				.extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void deleteRemovesAnExistingBook() {
		when(bookRepository.existsById(1L)).thenReturn(true);

		assertThat(bookService.delete(1L)).isTrue();

		verify(bookRepository).deleteById(1L);
	}

	@Test
	void deleteReturnsFalseWhenTheBookIsMissing() {
		when(bookRepository.existsById(9L)).thenReturn(false);

		assertThat(bookService.delete(9L)).isFalse();
	}

}
