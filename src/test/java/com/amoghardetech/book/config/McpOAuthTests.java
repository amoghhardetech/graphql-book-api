package com.amoghardetech.book.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class McpOAuthTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void mcpWithoutATokenAsksForOauth() throws Exception {
		mockMvc.perform(post("/mcp")
						.contentType("application/json")
						.accept("application/json, text/event-stream")
						.content("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("WWW-Authenticate",
						org.hamcrest.Matchers.containsString(
								"resource_metadata=\"http://localhost:8080/.well-known/oauth-protected-resource/mcp\"")))
				.andExpect(header().string("WWW-Authenticate", org.hamcrest.Matchers.containsString("scope=\"books\"")));
	}

	@Test
	void authorizationServerMetadataIsPublic() throws Exception {
		mockMvc.perform(get("/.well-known/oauth-authorization-server"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.issuer").value("http://localhost:8080"))
				.andExpect(jsonPath("$.code_challenge_methods_supported[0]").value("S256"));
	}

	@Test
	void graphqlWithoutATokenAsksForOauth() throws Exception {
		mockMvc.perform(post("/graphql")
						.contentType("application/json")
						.content("{\"query\":\"query { books { id } }\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("WWW-Authenticate",
						org.hamcrest.Matchers.containsString(
								"resource_metadata=\"http://localhost:8080/.well-known/oauth-protected-resource/graphql\"")));
	}

	@Test
	void protectedResourceMetadataNamesTheMcpUrl() throws Exception {
		mockMvc.perform(get("/.well-known/oauth-protected-resource/mcp"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resource").value("http://localhost:8080/mcp"))
				.andExpect(jsonPath("$.authorization_servers[0]").value("http://localhost:8080"))
				.andExpect(jsonPath("$.scopes_supported[0]").value("books"));
	}

	@Test
	void graphqlMetadataNamesTheGraphqlUrl() throws Exception {
		mockMvc.perform(get("/.well-known/oauth-protected-resource/graphql"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resource").value("http://localhost:8080/graphql"))
				.andExpect(jsonPath("$.authorization_servers[0]").value("http://localhost:8080"));
	}

}
