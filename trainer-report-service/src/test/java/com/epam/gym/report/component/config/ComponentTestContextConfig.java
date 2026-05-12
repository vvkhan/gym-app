package com.epam.gym.report.component.config;

import com.epam.gym.report.ReportApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(
        classes = ReportApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("component-test")
public class ComponentTestContextConfig {

    /**
    * Prevents OAuth2 auto-configuration from fetching the JWKS endpoint at startup.
    * @ConditionalOnMissingBean(JwtDecoder.class) in Spring Boot auto-config sees this
    * mock and skips creating the real NimbusJwtDecoder entirely — no HTTP call is made.
    **/
    @MockBean
    JwtDecoder jwtDecoder;
}
