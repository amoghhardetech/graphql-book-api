package com.amoghardetech.book.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void homePageListsTheGraphqlEndpoints() throws Exception {
		mockMvc.perform(get("/").header("Host", "localhost:8080"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("http://localhost:8080/")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("POST http://localhost:8080/graphql")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("http://localhost:8080/graphiql")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("books: [Book!]!")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("book(id: ID!): Book")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("createBook(input: BookInput!): Book!")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("updateBook(id: ID!, input: BookInput!): Book!")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("deleteBook(id: ID!): Boolean!")));
	}

}
