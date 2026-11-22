package com.amoghardetech.book.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String publicUrl, OAuth oauth) {

	public String issuer() {
		String url = publicUrl == null ? "http://localhost:8080" : publicUrl.trim();
		while (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}

	public String mcpResource() {
		return issuer() + "/mcp";
	}

	public String graphqlResource() {
		return issuer() + "/graphql";
	}

	public String protectedResourceMetadataUrl() {
		return issuer() + "/.well-known/oauth-protected-resource/mcp";
	}

	public String graphqlProtectedResourceMetadataUrl() {
		return issuer() + "/.well-known/oauth-protected-resource/graphql";
	}

	public record OAuth(String username, String password) {
	}

}
