package com.epam.gym.core.integration;

import com.epam.gym.core.dto.request.ActivateDeactivateRequest;
import com.epam.gym.core.dto.request.TraineeRegistrationRequest;
import com.epam.gym.core.dto.request.TraineeTrainersUpdateRequest;
import com.epam.gym.core.dto.request.UpdateTraineeRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ScenarioScope
public class TraineeControllerSteps {

    @Autowired private IntegrationScenarioContext ctx;
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private SharedIntegrationSteps shared;
    @Autowired private ObjectMapper objectMapper;

    @When("a trainee registration request is submitted")
    public void aTraineeRegistrationRequestIsSubmitted() {
        TraineeRegistrationRequest req = new TraineeRegistrationRequest();
        req.setFirstName("Reg" + ctx.uid);
        req.setLastName("User" + ctx.uid);
        req.setDateOfBirth(LocalDate.of(1990, 1, 1));
        ctx.lastResponse = restTemplate.postForEntity("/api/trainees", req, String.class);
        if (ctx.lastResponse.getStatusCode().is2xxSuccessful()) {
            ctx.traineeUsername = shared.extract(ctx.lastResponse.getBody(), "username");
            ctx.traineePassword = shared.extract(ctx.lastResponse.getBody(), "password");
        }
    }

    @When("a GET request is sent to the trainee profile endpoint without a token")
    public void aGetRequestIsSentToTraineeProfileWithoutToken() {
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainees/someuser", HttpMethod.GET,
                new HttpEntity<>(shared.noAuthHeaders()), String.class);
    }

    @When("the trainee requests their own profile")
    public void theTraineeRequestsTheirOwnProfile() {
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainees/" + ctx.traineeUsername, HttpMethod.GET,
                shared.authRequest(), String.class);
    }

    @When("the trainee requests a profile for a username that does not exist")
    public void theTraineeRequestsAProfileForAUsernameThatDoesNotExist() {
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainees/nonexistent-" + ctx.uid, HttpMethod.GET,
                shared.authRequest(), String.class);
    }

    @When("the trainee submits a profile update")
    public void theTraineeSubmitsAProfileUpdate() {
        UpdateTraineeRequest req = new UpdateTraineeRequest();
        req.setFirstName("Updated" + ctx.uid);
        req.setLastName("Name" + ctx.uid);
        req.setAddress("New Address " + ctx.uid);
        req.setDateOfBirth(LocalDate.of(1992, 3, 15));
        req.setIsActive(true);
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainees/" + ctx.traineeUsername, HttpMethod.PUT,
                shared.authRequest(req), String.class);
    }

    @Then("the response reflects the updated address")
    public void theResponseReflectsTheUpdatedAddress() {
        assertThat(shared.extract(ctx.lastResponse.getBody(), "address")).contains("New Address");
    }

    @When("the trainee deletes their own profile")
    public void theTraineeDeletesTheirOwnProfile() {
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainees/" + ctx.traineeUsername, HttpMethod.DELETE,
                shared.authRequest(), String.class);
    }

    @Then("getting the profile afterward returns 404")
    public void gettingTheProfileAfterwardReturns404() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(ctx.trainerToken);
        var resp = restTemplate.exchange(
                "/api/trainees/" + ctx.traineeUsername, HttpMethod.GET,
                new HttpEntity<>(headers), String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(404);
    }

    @And("the trainee is deactivated")
    public void theTraineeIsDeactivated() {
        ActivateDeactivateRequest req = new ActivateDeactivateRequest();
        req.setIsActive(false);
        restTemplate.exchange(
                "/api/trainees/" + ctx.traineeUsername + "/activate", HttpMethod.PATCH,
                shared.authRequest(req), String.class);
    }

    @When("the trainee sends an activate request")
    public void theTraineeSendsAnActivateRequest() {
        ActivateDeactivateRequest req = new ActivateDeactivateRequest();
        req.setIsActive(true);
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainees/" + ctx.traineeUsername + "/activate", HttpMethod.PATCH,
                shared.authRequest(req), String.class);
    }

    @When("the trainee sends a deactivate request")
    public void theTraineeSendsADeactivateRequest() {
        ActivateDeactivateRequest req = new ActivateDeactivateRequest();
        req.setIsActive(false);
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainees/" + ctx.traineeUsername + "/activate", HttpMethod.PATCH,
                shared.authRequest(req), String.class);
    }

    @When("the trainee sends an activate request without prior deactivation")
    public void theTraineeSendsAnActivateRequestWithoutPriorDeactivation() {
        theTraineeSendsAnActivateRequest();
    }

    @When("the trainee requests the list of not-assigned trainers")
    public void theTraineeRequestsTheListOfNotAssignedTrainers() {
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainees/" + ctx.traineeUsername + "/not-assigned-trainers", HttpMethod.GET,
                shared.authRequest(), String.class);
    }

    @Then("the response is a list")
    public void theResponseIsAList() {
        try {
            JsonNode node = objectMapper.readTree(ctx.lastResponse.getBody());
            assertThat(node.isArray()).isTrue();
        } catch (Exception e) {
            throw new AssertionError("Response is not a JSON array: " + ctx.lastResponse.getBody(), e);
        }
    }

    @When("the trainee updates their trainer list with the registered trainer")
    public void theTraineeUpdatesTheirTrainerListWithTheRegisteredTrainer() {
        TraineeTrainersUpdateRequest req = new TraineeTrainersUpdateRequest();
        req.setTrainerUsernames(List.of(ctx.trainerUsername));
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainees/" + ctx.traineeUsername + "/trainers", HttpMethod.PUT,
                shared.authRequest(req), String.class);
    }

    @Then("the response includes the assigned trainer")
    public void theResponseIncludesTheAssignedTrainer() {
        try {
            JsonNode array = objectMapper.readTree(ctx.lastResponse.getBody());
            boolean found = false;
            for (JsonNode node : array) {
                if (ctx.trainerUsername.equals(node.get("username").asText())) {
                    found = true;
                    break;
                }
            }
            assertThat(found).isTrue();
        } catch (Exception e) {
            throw new AssertionError("Could not find trainer in response: " + ctx.lastResponse.getBody(), e);
        }
    }

    @And("a training session exists for the trainee")
    public void aTrainingSessionExistsForTheTrainee() {
        var req = new com.epam.gym.core.dto.request.AddTrainingRequest();
        req.setTraineeUsername(ctx.traineeUsername);
        req.setTrainerUsername(ctx.trainerUsername);
        req.setTrainingName("Session-" + ctx.uid);
        req.setTrainingDate(LocalDate.now());
        req.setDuration(45);
        restTemplate.exchange("/api/trainings", HttpMethod.POST, shared.authRequest(req), String.class);
    }

    @When("the trainee requests their training list")
    public void theTraineeRequestsTheirTrainingList() {
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainees/" + ctx.traineeUsername + "/trainings", HttpMethod.GET,
                shared.authRequest(), String.class);
    }
}
