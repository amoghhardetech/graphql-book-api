package com.amoghardetech.book.config;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProtectedResourceMetadataController {

	private final AppProperties app;

	public ProtectedResourceMetadataController(AppProperties app) {
		this.app = app;
	}

	@GetMapping({ "/.well-known/oauth-protected-resource", "/.well-known/oauth-protected-resource/mcp" })
	public Map<String, Object> mcpMetadata() {
		return metadata(app.mcpResource());
	}

	@GetMapping("/.well-known/oauth-protected-resource/graphql")
	public Map<String, Object> graphqlMetadata() {
		return metadata(app.graphqlResource());
	}

	private Map<String, Object> metadata(String resource) {
		return Map.of(
				"resource", resource,
				"authorization_servers", List.of(app.issuer()),
				"bearer_methods_supported", List.of("header"),
				"scopes_supported", List.of(SecurityConfig.BOOKS_SCOPE));
	}

}
