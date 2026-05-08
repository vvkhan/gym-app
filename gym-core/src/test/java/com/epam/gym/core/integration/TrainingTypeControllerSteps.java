package com.epam.gym.core.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

@ScenarioScope
public class TrainingTypeControllerSteps {

    @Autowired private IntegrationScenarioContext ctx;
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private SharedIntegrationSteps shared;
    @Autowired private ObjectMapper objectMapper;

    @When("the training types are requested")
    public void theTrainingTypesAreRequested() {
        ctx.lastResponse = restTemplate.exchange(
                "/api/training-types", HttpMethod.GET,
                shared.authRequest(), String.class);
    }

    @When("the training types are requested without a token")
    public void theTrainingTypesAreRequestedWithoutAToken() {
        ctx.lastResponse = restTemplate.exchange(
                "/api/training-types", HttpMethod.GET,
                new HttpEntity<>(shared.noAuthHeaders()), String.class);
    }

    @Then("the response contains a non-empty list of training types")
    public void theResponseContainsANonEmptyListOfTrainingTypes() {
        try {
            JsonNode array = objectMapper.readTree(ctx.lastResponse.getBody());
            assertThat(array.isArray()).isTrue();
            assertThat(array.size()).isGreaterThan(0);
        } catch (Exception e) {
            throw new AssertionError("Could not parse training types response: " + ctx.lastResponse.getBody(), e);
        }
    }
}
