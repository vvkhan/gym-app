package com.epam.gym.report.integration.config;

import com.epam.gym.report.ReportApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

@CucumberContextConfiguration
@SpringBootTest(
        classes = {ReportApplication.class, IntegrationTestContextConfig.JwtTestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration-test")
public class IntegrationTestContextConfig {

    /**
     * Replaces the real NimbusJwtDecoder (which would fetch JWKS at startup).
     * Any Bearer token → valid JWT with required scopes. No-token requests bypass
     * this entirely and get 401 from Spring Security.
     */
    @Configuration
    static class JwtTestConfig {
        @Bean
        @Primary
        public JwtDecoder testJwtDecoder() {
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .subject("test-service")
                    .claim("scope", "report:write report:read")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
        }
    }

    @Autowired
    public TestRestTemplate restTemplate;
}
