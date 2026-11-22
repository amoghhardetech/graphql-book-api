package com.amoghardetech.book.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.util.StringUtils;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(AppProperties.class)
public class SecurityConfig {

	static final String CLAUDE_CLIENT_ID = "claude";

	static final String BOOKS_SCOPE = "books";

	static final String CLAUDE_REDIRECT_URI = "https://claude.ai/api/mcp/auth_callback";

	@Bean
	@Order(1)
	SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		OAuth2AuthorizationServerConfigurer authorizationServer = new OAuth2AuthorizationServerConfigurer();
		http.securityMatcher(authorizationServer.getEndpointsMatcher())
				.with(authorizationServer, (server) -> server.oidc(Customizer.withDefaults()))
				.authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
				.exceptionHandling((exceptions) -> exceptions.defaultAuthenticationEntryPointFor(
						new LoginUrlAuthenticationEntryPoint("/login"),
						new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));
		return http.build();
	}

	@Bean
	@Order(2)
	SecurityFilterChain mcpSecurityFilterChain(HttpSecurity http, AppProperties app) throws Exception {
		http.securityMatcher("/mcp", "/graphql", "/graphiql", "/graphiql/**")
				.csrf((csrf) -> csrf.ignoringRequestMatchers("/graphql", "/mcp"))
				.authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
				.oauth2ResourceServer((resourceServer) -> resourceServer.jwt(Customizer.withDefaults())
						.authenticationEntryPoint((request, response, exception) -> {
							String accept = request.getHeader("Accept");
							if (accept != null && accept.contains(MediaType.TEXT_HTML_VALUE)) {
								new LoginUrlAuthenticationEntryPoint("/login").commence(request, response, exception);
								return;
							}
							String metadata = request.getRequestURI().contains("/graphql")
									? app.graphqlProtectedResourceMetadataUrl()
									: app.protectedResourceMetadataUrl();
							response.setHeader("WWW-Authenticate",
									"Bearer resource_metadata=\"" + metadata + "\", scope=\"" + BOOKS_SCOPE + "\"");
							response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
						}));
		return http.build();
	}

	@Bean
	@Order(3)
	SecurityFilterChain applicationSecurityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests((authorize) -> authorize.anyRequest().permitAll())
				.formLogin(Customizer.withDefaults());
		return http.build();
	}

	@Bean
	RegisteredClientRepository registeredClientRepository() {
		RegisteredClient claude = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId(CLAUDE_CLIENT_ID)
				.clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.redirectUri(CLAUDE_REDIRECT_URI)
				.scope(BOOKS_SCOPE)
				.scope(OidcScopes.OPENID)
				.clientSettings(ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(true).build())
				.tokenSettings(TokenSettings.builder()
						.accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
						.accessTokenTimeToLive(Duration.ofHours(8))
						.refreshTokenTimeToLive(Duration.ofDays(30))
						.reuseRefreshTokens(false)
						.build())
				.build();
		return new InMemoryRegisteredClientRepository(claude);
	}

	@Bean
	AuthorizationServerSettings authorizationServerSettings(AppProperties app) {
		return AuthorizationServerSettings.builder().issuer(app.issuer()).build();
	}

	@Bean
	JWKSource<SecurityContext> jwkSource() {
		try {
			var keys = java.security.KeyPairGenerator.getInstance("RSA");
			keys.initialize(2048);
			var pair = keys.generateKeyPair();
			RSAKey rsa = new RSAKey.Builder((RSAPublicKey) pair.getPublic())
					.privateKey((RSAPrivateKey) pair.getPrivate())
					.keyID(UUID.randomUUID().toString())
					.build();
			return new ImmutableJWKSet<>(new JWKSet(rsa));
		}
		catch (java.security.NoSuchAlgorithmException exception) {
			throw new IllegalStateException("RSA is not available", exception);
		}
	}

	@Bean
	JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource, AppProperties app) {
		NimbusJwtDecoder decoder = (NimbusJwtDecoder) OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
		OAuth2TokenValidator<Jwt> issuer = JwtValidators.createDefaultWithIssuer(app.issuer());
		OAuth2TokenValidator<Jwt> audience = token -> token.getAudience().contains(app.mcpResource())
				|| token.getAudience().contains(app.graphqlResource())
						? org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success()
						: org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
								new org.springframework.security.oauth2.core.OAuth2Error("invalid_token",
										"The token audience is not this application", null));
		decoder.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(issuer, audience));
		return decoder;
	}

	@Bean
	OAuth2TokenCustomizer<JwtEncodingContext> accessTokenAudience(AppProperties app) {
		return context -> {
			if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
				context.getClaims().audience(List.of(app.mcpResource(), app.graphqlResource()));
			}
		};
	}

	@Bean
	UserDetailsService userDetailsService(AppProperties app, PasswordEncoder passwordEncoder) {
		if (app.oauth() == null || !StringUtils.hasText(app.oauth().username())
				|| !StringUtils.hasText(app.oauth().password())) {
			throw new IllegalStateException("Set BOOK_USER and BOOK_PASSWORD before starting the application");
		}
		return new org.springframework.security.provisioning.InMemoryUserDetailsManager(
				User.withUsername(app.oauth().username())
						.password(passwordEncoder.encode(app.oauth().password()))
						.roles("USER")
						.build());
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

}
