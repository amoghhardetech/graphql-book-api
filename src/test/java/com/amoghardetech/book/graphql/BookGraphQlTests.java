package com.amoghardetech.book.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

@SpringBootTest
@AutoConfigureHttpGraphQlTester
class BookGraphQlTests {

	private static final String CREATE = """
			mutation($input: BookInput!) {
			  createBook(input: $input) { id title author price }
			}
			""";

	@Autowired
	private HttpGraphQlTester graphQlTester;

	@Test
	void createsThenReadsUpdatesAndDeletesABook() {
		Map<String, Object> input = Map.of(
				"title", "Dune",
				"author", "Frank Herbert",
				"price", 18.99);

		String id = graphQlTester.document(CREATE)
				.variable("input", input)
				.execute()
				.path("createBook")
				.entity(Map.class)
				.get()
				.get("id")
				.toString();

		graphQlTester.document("""
				query($id: ID!) {
				  book(id: $id) { id title author price }
				}
				""")
				.variable("id", id)
				.execute()
				.path("book.title")
				.entity(String.class)
				.isEqualTo("Dune");

		graphQlTester.document("""
				query { books { id title } }
				""")
				.execute()
				.path("books[*].title")
				.entityList(String.class)
				.contains("Dune");

		graphQlTester.document("""
				mutation($id: ID!, $input: BookInput!) {
				  updateBook(id: $id, input: $input) { title price }
				}
				""")
				.variable("id", id)
				.variable("input", Map.of("title", "Dune Messiah", "author", "Frank Herbert", "price", 16.5))
				.execute()
				.path("updateBook.title")
				.entity(String.class)
				.isEqualTo("Dune Messiah");

		graphQlTester.document("""
				mutation($id: ID!) { deleteBook(id: $id) }
				""")
				.variable("id", id)
				.execute()
				.path("deleteBook")
				.entity(Boolean.class)
				.isEqualTo(true);

		graphQlTester.document("""
				query($id: ID!) { book(id: $id) { id } }
				""")
				.variable("id", id)
				.execute()
				.path("book")
				.valueIsNull();
	}

	@Test
	void missingBookQueriesReturnNullAndDeleteReturnsFalse() {
		graphQlTester.document("""
				query { book(id: "999") { id } }
				""")
				.execute()
				.path("book")
				.valueIsNull();

		Boolean deleted = graphQlTester.document("""
				mutation { deleteBook(id: "999") }
				""")
				.execute()
				.path("deleteBook")
				.entity(Boolean.class)
				.get();

		assertThat(deleted).isFalse();
	}

	@Test
	void updateRejectsAMissingBook() {
		graphQlTester.document("""
				mutation($input: BookInput!) {
				  updateBook(id: "999", input: $input) { id }
				}
				""")
				.variable("input", Map.of("title", "Dune", "author", "Frank Herbert", "price", 18.99))
				.execute()
				.errors()
				.satisfy(errors -> assertThat(errors).isNotEmpty());
	}

}
