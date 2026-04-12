package com.epam.gym.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

    // Filter chain 1 — handles all OAuth2 protocol endpoints:
    // - POST /oauth2/token       — issue access tokens
    // - GET  /oauth2/jwks        — expose public key for resource servers
    // - GET  /.well-known/...    — OpenID Connect discovery metadata
    // Client authentication is done via HTTP Basic (client_id:client_secret) at token request step
    // Unauthenticated request returns 401
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) ->
                        res.sendError(401, "Unauthorized")));
        return http.build();
    }

    // Filter chain 2 — covers everything not matched by chain 1
    // Only /actuator/health is permitted (needed for Eureka status)
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().denyAll())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    // Registers gym-core as an OAuth2 client with client_credentials grant
    // Scopes:
    // - report:write — POST /api/report (send training events)
    // - report:read  — GET  /api/report/... (query monthly hours)
    // Token TTL is short (5 min), gym-core caches it and refreshes automatically via OAuth2AuthorizedClientManager
    @Bean
    public RegisteredClientRepository registeredClientRepository(
            PasswordEncoder passwordEncoder,
            @Value("${app.security.gym-core-client-secret}") String clientSecret) {

        RegisteredClient gymCore = RegisteredClient.withId("gym-core-client-id")
                .clientId("gym-core")
                .clientSecret(passwordEncoder.encode(clientSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("report:write")
                .scope("report:read")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(5))
                        .build())
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(gymCore);
    }

    // RSA-2048 key pair generated fresh on every startup
    // Regenerating on restart invalidates all previously issued tokens
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyID(UUID.randomUUID().toString())
                    .build();

            return new ImmutableJWKSet<>(new JWKSet(rsaKey));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to generate RSA key pair for JWT signing", e);
        }
    }

    // JwtDecoder is required by the Authorization Server infrastructure to validate tokens it issues
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    // Configures the issuer URI embedded in every issued JWT ('iss' claim)
    // The resource server uses this to validate that the token came from the expected authorization server
    @Bean
    public AuthorizationServerSettings authorizationServerSettings(
            @Value("${app.auth.issuer-uri}") String issuerUri) {
        return AuthorizationServerSettings.builder()
                .issuer(issuerUri)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
