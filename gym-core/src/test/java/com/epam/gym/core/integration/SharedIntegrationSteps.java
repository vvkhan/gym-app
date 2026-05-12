package com.epam.gym.core.integration;

import com.epam.gym.core.dto.request.LoginRequest;
import com.epam.gym.core.dto.request.TraineeRegistrationRequest;
import com.epam.gym.core.dto.request.TrainerRegistrationRequest;
import com.epam.gym.core.repository.TrainingTypeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ScenarioScope
public class SharedIntegrationSteps {

    @Autowired private IntegrationScenarioContext ctx;
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private TrainingTypeRepository trainingTypeRepository;
    @Autowired private ObjectMapper objectMapper;

    // ----- Shared setup steps -----

    @Given("a trainee is registered")
    public void aTraineeIsRegistered() {
        TraineeRegistrationRequest req = new TraineeRegistrationRequest();
        req.setFirstName("First" + ctx.uid);
        req.setLastName("Last" + ctx.uid);
        req.setDateOfBirth(LocalDate.of(1990, 1, 1));
        ResponseEntity<String> resp = restTemplate.postForEntity("/api/trainees", req, String.class);
        ctx.traineeUsername = extract(resp.getBody(), "username");
        ctx.traineePassword = extract(resp.getBody(), "password");
    }

    @Given("a trainer is registered")
    public void aTrainerIsRegistered() {
        UUID typeId = trainingTypeRepository.findAll().get(0).getId();
        TrainerRegistrationRequest req = new TrainerRegistrationRequest();
        req.setFirstName("TFirst" + ctx.uid);
        req.setLastName("TLast" + ctx.uid);
        req.setSpecializationId(typeId);
        ResponseEntity<String> resp = restTemplate.postForEntity("/api/trainers", req, String.class);
        ctx.trainerUsername = extract(resp.getBody(), "username");
        ctx.trainerPassword = extract(resp.getBody(), "password");
    }

    @Given("a trainee is registered and logged in")
    public void aTraineeIsRegisteredAndLoggedIn() {
        aTraineeIsRegistered();
        ctx.traineeToken = login(ctx.traineeUsername, ctx.traineePassword);
        ctx.activeToken = ctx.traineeToken;
    }

    @Given("a trainer is registered and logged in")
    public void aTrainerIsRegisteredAndLoggedIn() {
        aTrainerIsRegistered();
        ctx.trainerToken = login(ctx.trainerUsername, ctx.trainerPassword);
        ctx.activeToken = ctx.trainerToken;
    }

    // ----- Common assertions -----

    @Then("the response status is {int}")
    public void theResponseStatusIs(int expected) {
        assertThat(ctx.lastResponse.getStatusCode().value()).isEqualTo(expected);
    }

    @Then("the response contains a non-blank username and password")
    public void theResponseContainsANonBlankUsernameAndPassword() {
        assertThat(extract(ctx.lastResponse.getBody(), "username")).isNotBlank();
        assertThat(extract(ctx.lastResponse.getBody(), "password")).isNotBlank();
    }

    @Then("the profile contains the same first and last name as registered")
    public void theProfileContainsSameFirstAndLastName() {
        assertThat(extract(ctx.lastResponse.getBody(), "firstName")).isNotBlank();
        assertThat(extract(ctx.lastResponse.getBody(), "lastName")).isNotBlank();
    }

    @Then("the training list is not empty")
    public void theTrainingListIsNotEmpty() {
        try {
            JsonNode node = objectMapper.readTree(ctx.lastResponse.getBody());
            assertThat(node.isArray()).isTrue();
            assertThat(node.size()).isGreaterThan(0);
        } catch (Exception e) {
            throw new AssertionError("Could not parse training list: " + ctx.lastResponse.getBody(), e);
        }
    }

    // ----- Package-private helpers used by other step classes -----

    String login(String username, String password) {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        ResponseEntity<String> resp = restTemplate.postForEntity("/api/auth/login", req, String.class);
        return extract(resp.getBody(), "accessToken");
    }

    HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(ctx.activeToken);
        return headers;
    }

    HttpHeaders noAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    HttpEntity<Void> authRequest() {
        return new HttpEntity<>(authHeaders());
    }

    <T> HttpEntity<T> authRequest(T body) {
        return new HttpEntity<>(body, authHeaders());
    }

    String extract(String json, String field) {
        try {
            return objectMapper.readTree(json).get(field).asText();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot extract field '" + field + "' from: " + json, e);
        }
    }
}
