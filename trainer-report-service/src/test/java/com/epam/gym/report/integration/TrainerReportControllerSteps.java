package com.epam.gym.report.integration;

import com.epam.gym.report.dto.ActionType;
import com.epam.gym.report.dto.WorkloadRequest;
import com.epam.gym.report.repository.TrainerSummaryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ScenarioScope
public class TrainerReportControllerSteps {

    private static final String TEST_TOKEN = "test-bearer-token";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 6, 1);

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private TrainerSummaryRepository repository;
    @Autowired private ObjectMapper objectMapper;

    private String uid;
    private String trainerUsername;
    private int submittedDuration;
    private ResponseEntity<String> lastResponse;

    @Before
    public void setUp() {
        uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    @After
    public void tearDown() {
        if (trainerUsername != null) {
            repository.findByUsername(trainerUsername).ifPresent(repository::delete);
        }
    }

    @Given("a valid JWT token is available")
    public void aValidJwtTokenIsAvailable() {
        // JWT is decoded by the always-valid testJwtDecoder bean in JwtTestConfig.
    }

    @When("a workload ADD request is submitted for a trainer")
    public void aWorkloadAddRequestIsSubmittedForATrainer() {
        trainerUsername = "trainer-" + uid;
        submittedDuration = 60;
        lastResponse = restTemplate.exchange(
                "/api/report", HttpMethod.POST,
                new HttpEntity<>(buildRequest(trainerUsername, submittedDuration, ActionType.ADD), authHeaders()),
                String.class);
    }

    @When("a workload ADD request is submitted without a token")
    public void aWorkloadAddRequestIsSubmittedWithoutAToken() {
        lastResponse = restTemplate.exchange(
                "/api/report", HttpMethod.POST,
                new HttpEntity<>(buildRequest("trainer-" + uid, 60, ActionType.ADD), noAuthHeaders()),
                String.class);
    }

    @When("a workload request is submitted with missing required fields")
    public void aWorkloadRequestIsSubmittedWithMissingRequiredFields() {
        lastResponse = restTemplate.exchange(
                "/api/report", HttpMethod.POST,
                new HttpEntity<>(new WorkloadRequest(), authHeaders()),
                String.class);
    }

    @And("a workload ADD event has been processed for a trainer")
    public void aWorkloadAddEventHasBeenProcessedForATrainer() {
        trainerUsername = "trainer-" + uid;
        submittedDuration = 90;
        restTemplate.exchange(
                "/api/report", HttpMethod.POST,
                new HttpEntity<>(buildRequest(trainerUsername, submittedDuration, ActionType.ADD), authHeaders()),
                String.class);
    }

    @When("the summary is requested for that trainer")
    public void theSummaryIsRequestedForThatTrainer() {
        lastResponse = restTemplate.exchange(
                "/api/report/" + trainerUsername, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), String.class);
    }

    @When("the summary is requested for a trainer username that does not exist")
    public void theSummaryIsRequestedForATrainerUsernameThatDoesNotExist() {
        lastResponse = restTemplate.exchange(
                "/api/report/nonexistent-" + uid, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), String.class);
    }

    @When("the summary is requested without a token")
    public void theSummaryIsRequestedWithoutAToken() {
        lastResponse = restTemplate.exchange(
                "/api/report/some-trainer", HttpMethod.GET,
                new HttpEntity<>(noAuthHeaders()), String.class);
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int expected) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(expected);
    }

    @Then("the response contains the workload data for the submitted training month")
    public void theResponseContainsTheWorkloadDataForTheSubmittedTrainingMonth() {
        try {
            JsonNode root = objectMapper.readTree(lastResponse.getBody());
            JsonNode years = root.get("years");
            assertThat(years).isNotNull();
            assertThat(years.isArray()).isTrue();
            boolean found = false;
            for (JsonNode year : years) {
                if (year.get("year").asInt() == TRAINING_DATE.getYear()) {
                    for (JsonNode month : year.get("months")) {
                        if (month.get("month").asInt() == TRAINING_DATE.getMonthValue()) {
                            assertThat(month.get("totalDurationMinutes").asInt()).isEqualTo(submittedDuration);
                            found = true;
                        }
                    }
                }
            }
            assertThat(found).isTrue();
        } catch (Exception e) {
            throw new AssertionError("Could not parse workload response: " + lastResponse.getBody(), e);
        }
    }

    // ----- Helpers -----

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(TEST_TOKEN);
        return headers;
    }

    private HttpHeaders noAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private WorkloadRequest buildRequest(String username, int durationMinutes, ActionType action) {
        WorkloadRequest req = new WorkloadRequest();
        req.setTrainerUsername(username);
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setActive(true);
        req.setTrainingDate(TRAINING_DATE);
        req.setTrainingDurationMinutes(durationMinutes);
        req.setActionType(action);
        return req;
    }
}
