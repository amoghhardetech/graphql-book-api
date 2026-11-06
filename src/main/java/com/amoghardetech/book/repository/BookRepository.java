package com.amoghardetech.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amoghardetech.book.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
}
