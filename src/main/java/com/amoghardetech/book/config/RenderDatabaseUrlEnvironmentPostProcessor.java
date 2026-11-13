package com.amoghardetech.book.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

public class RenderDatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Map<String, Object> properties = new HashMap<>();
		String databaseUrl = environment.getProperty("DATABASE_URL");
		String datasourceUrl = environment.getProperty("spring.datasource.url");
		String candidate = postgresUrl(databaseUrl) ? databaseUrl : datasourceUrl;
		if (postgresUrl(candidate)) {
			properties.putAll(jdbcProperties(candidate));
		}
		if (!StringUtils.hasText(environment.getProperty("spring.ai.openai.api-key"))) {
			properties.put("spring.ai.model.chat", "none");
			properties.put("spring.ai.model.embedding", "none");
			properties.put("spring.ai.model.image", "none");
			properties.put("spring.ai.model.moderation", "none");
			properties.put("spring.ai.model.audio.speech", "none");
			properties.put("spring.ai.model.audio.transcription", "none");
		}
		if (!properties.isEmpty()) {
			environment.getPropertySources().addFirst(new MapPropertySource("render-runtime", properties));
		}
	}

	static boolean postgresUrl(String url) {
		return url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://"));
	}

	static Map<String, Object> jdbcProperties(String url) {
		int schemeEnd = url.indexOf("://");
		String rest = url.substring(schemeEnd + 3);
		int at = rest.lastIndexOf('@');
		String userInfo = at >= 0 ? rest.substring(0, at) : null;
		String hostAndPath = at >= 0 ? rest.substring(at + 1) : rest;
		int slash = hostAndPath.indexOf('/');
		if (slash < 0) {
			throw new IllegalArgumentException("Database URL is missing the database name");
		}
		String hostPort = hostAndPath.substring(0, slash);
		String pathAndQuery = hostAndPath.substring(slash + 1);
		int queryStart = pathAndQuery.indexOf('?');
		String database = queryStart < 0 ? pathAndQuery : pathAndQuery.substring(0, queryStart);
		String query = queryStart < 0 ? null : pathAndQuery.substring(queryStart + 1);
		int colon = hostPort.lastIndexOf(':');
		String host = colon < 0 ? hostPort : hostPort.substring(0, colon);
		String port = colon < 0 ? "5432" : hostPort.substring(colon + 1);
		String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
		if (StringUtils.hasText(query)) {
			jdbcUrl = jdbcUrl + "?" + query;
		}

		Map<String, Object> properties = new HashMap<>();
		properties.put("spring.datasource.url", jdbcUrl);
		if (userInfo != null) {
			int separator = userInfo.indexOf(':');
			String username = separator < 0 ? userInfo : userInfo.substring(0, separator);
			properties.put("spring.datasource.username", decode(username));
			if (separator >= 0) {
				properties.put("spring.datasource.password", decode(userInfo.substring(separator + 1)));
			}
		}
		return properties;
	}

	private static String decode(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

}
