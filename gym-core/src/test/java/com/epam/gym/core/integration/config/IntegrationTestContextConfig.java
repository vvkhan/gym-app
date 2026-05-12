package com.epam.gym.core.integration.config;

import com.epam.gym.core.GymApplication;
import com.epam.gym.core.messaging.TrainingEventPublisher;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@CucumberContextConfiguration
@SpringBootTest(
        classes = GymApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration-test")
public class IntegrationTestContextConfig {

    // Separate container from the component-test suite — full isolation
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void overrideDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    /**
    * Isolates gym-core from trainer-report-service: assertions on publish() calls
    * use Mockito.verify() in step definitions
    **/
    @MockBean
    TrainingEventPublisher trainingEventPublisher;

    // Injected here so step definitions can @Autowire it from the shared context
    @Autowired
    public TestRestTemplate restTemplate;
}
