package com.epam.gym.core.integration;

import com.epam.gym.core.dto.request.ActivateDeactivateRequest;
import com.epam.gym.core.dto.request.AddTrainingRequest;
import com.epam.gym.core.dto.request.TrainerRegistrationRequest;
import com.epam.gym.core.dto.request.UpdateTrainerRequest;
import com.epam.gym.core.repository.TrainingTypeRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ScenarioScope
public class TrainerControllerSteps {

    @Autowired private IntegrationScenarioContext ctx;
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private SharedIntegrationSteps shared;
    @Autowired private TrainingTypeRepository trainingTypeRepository;

    @When("a trainer registration request is submitted")
    public void aTrainerRegistrationRequestIsSubmitted() {
        TrainerRegistrationRequest req = new TrainerRegistrationRequest();
        req.setFirstName("TReg" + ctx.uid);
        req.setLastName("TUser" + ctx.uid);
        req.setSpecializationId(trainingTypeRepository.findAll().get(0).getId());
        ctx.lastResponse = restTemplate.postForEntity("/api/trainers", req, String.class);
        if (ctx.lastResponse.getStatusCode().is2xxSuccessful()) {
            ctx.trainerUsername = shared.extract(ctx.lastResponse.getBody(), "username");
            ctx.trainerPassword = shared.extract(ctx.lastResponse.getBody(), "password");
        }
    }

    @When("a GET request is sent to the trainer profile endpoint without a token")
    public void aGetRequestIsSentToTrainerProfileWithoutToken() {
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainers/someuser", HttpMethod.GET,
                new HttpEntity<>(shared.noAuthHeaders()), String.class);
    }

    @When("the trainer requests their own profile")
    public void theTrainerRequestsTheirOwnProfile() {
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainers/" + ctx.trainerUsername, HttpMethod.GET,
                shared.authRequest(), String.class);
    }

    @When("the trainer requests a profile for a username that does not exist")
    public void theTrainerRequestsAProfileForAUsernameThatDoesNotExist() {
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainers/nonexistent-" + ctx.uid, HttpMethod.GET,
                shared.authRequest(), String.class);
    }

    @When("the trainer submits a profile update")
    public void theTrainerSubmitsAProfileUpdate() {
        UpdateTrainerRequest req = new UpdateTrainerRequest();
        req.setFirstName("Updated" + ctx.uid);
        req.setLastName("Trainer" + ctx.uid);
        req.setIsActive(true);
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainers/" + ctx.trainerUsername, HttpMethod.PUT,
                shared.authRequest(req), String.class);
    }

    @Then("the response reflects the updated first and last name")
    public void theResponseReflectsTheUpdatedFirstAndLastName() {
        assertThat(shared.extract(ctx.lastResponse.getBody(), "firstName")).contains("Updated");
    }

    @And("the trainer is deactivated")
    public void theTrainerIsDeactivated() {
        ActivateDeactivateRequest req = new ActivateDeactivateRequest();
        req.setIsActive(false);
        restTemplate.exchange(
                "/api/trainers/" + ctx.trainerUsername + "/activate", HttpMethod.PATCH,
                shared.authRequest(req), String.class);
    }

    @When("the trainer sends an activate request")
    public void theTrainerSendsAnActivateRequest() {
        ActivateDeactivateRequest req = new ActivateDeactivateRequest();
        req.setIsActive(true);
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainers/" + ctx.trainerUsername + "/activate", HttpMethod.PATCH,
                shared.authRequest(req), String.class);
    }

    @When("the trainer sends a deactivate request")
    public void theTrainerSendsADeactivateRequest() {
        ActivateDeactivateRequest req = new ActivateDeactivateRequest();
        req.setIsActive(false);
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainers/" + ctx.trainerUsername + "/activate", HttpMethod.PATCH,
                shared.authRequest(req), String.class);
    }

    @When("the trainer sends an activate request without prior deactivation")
    public void theTrainerSendsAnActivateRequestWithoutPriorDeactivation() {
        theTrainerSendsAnActivateRequest();
    }

    @And("a training session exists for the trainer")
    public void aTrainingSessionExistsForTheTrainer() {
        AddTrainingRequest req = new AddTrainingRequest();
        req.setTraineeUsername(ctx.traineeUsername);
        req.setTrainerUsername(ctx.trainerUsername);
        req.setTrainingName("TSession-" + ctx.uid);
        req.setTrainingDate(LocalDate.now());
        req.setDuration(45);
        restTemplate.exchange("/api/trainings", HttpMethod.POST, shared.authRequest(req), String.class);
    }

    @When("the trainer requests their training list")
    public void theTrainerRequestsTheirTrainingList() {
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainers/" + ctx.trainerUsername + "/trainings", HttpMethod.GET,
                shared.authRequest(), String.class);
    }
}
