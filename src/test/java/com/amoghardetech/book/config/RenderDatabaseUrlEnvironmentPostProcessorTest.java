package com.amoghardetech.book.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

class RenderDatabaseUrlEnvironmentPostProcessorTest {

	private final RenderDatabaseUrlEnvironmentPostProcessor processor = new RenderDatabaseUrlEnvironmentPostProcessor();

	@Test
	void convertsARenderDatabaseUrlIntoJdbcProperties() {
		Map<String, Object> properties = RenderDatabaseUrlEnvironmentPostProcessor.jdbcProperties(
				"postgres://book_user:s3cret%2Fpw@dpg-book-a.oregon-postgres.render.com:5432/bookdb?sslmode=require");

		assertThat(properties).containsEntry("spring.datasource.url",
				"jdbc:postgresql://dpg-book-a.oregon-postgres.render.com:5432/bookdb?sslmode=require");
		assertThat(properties).containsEntry("spring.datasource.username", "book_user");
		assertThat(properties).containsEntry("spring.datasource.password", "s3cret/pw");
	}

	@Test
	void prefersDatabaseUrlOverTheLocalJdbcUrl() {
		MockEnvironment environment = new MockEnvironment()
				.withProperty("DATABASE_URL", "postgresql://render_user:render_pw@localhost/bookdb")
				.withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/bookdb");

		processor.postProcessEnvironment(environment, new SpringApplication());

		assertThat(environment.getProperty("spring.datasource.url"))
				.isEqualTo("jdbc:postgresql://localhost:5432/bookdb");
		assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("render_user");
	}

	@Test
	void leavesAJdbcUrlUnchangedWhenDatabaseUrlIsAbsent() {
		MockEnvironment environment = new MockEnvironment()
				.withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/bookdb")
				.withProperty("spring.datasource.username", "postgres");

		processor.postProcessEnvironment(environment, new SpringApplication());

		assertThat(environment.getProperty("spring.datasource.url"))
				.isEqualTo("jdbc:postgresql://localhost:5432/bookdb");
		assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("postgres");
	}

}
