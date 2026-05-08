package com.epam.gym.core.integration;

import com.epam.gym.core.dto.request.AddTrainingRequest;
import io.cucumber.java.en.When;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.time.LocalDate;

@ScenarioScope
public class TrainingControllerSteps {

    @Autowired private IntegrationScenarioContext ctx;
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private SharedIntegrationSteps shared;

    @When("a training session is submitted")
    public void aTrainingSessionIsSubmitted() {
        AddTrainingRequest req = buildRequest();
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainings", HttpMethod.POST,
                shared.authRequest(req), String.class);
    }

    @When("a training session is submitted without a token")
    public void aTrainingSessionIsSubmittedWithoutAToken() {
        AddTrainingRequest req = buildRequest();
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainings", HttpMethod.POST,
                new HttpEntity<>(req, shared.noAuthHeaders()), String.class);
    }

    @When("a training session is submitted with missing required fields")
    public void aTrainingSessionIsSubmittedWithMissingRequiredFields() {
        ctx.lastResponse = restTemplate.exchange(
                "/api/trainings", HttpMethod.POST,
                shared.authRequest(new AddTrainingRequest()), String.class);
    }

    private AddTrainingRequest buildRequest() {
        AddTrainingRequest req = new AddTrainingRequest();
        req.setTraineeUsername(ctx.traineeUsername);
        req.setTrainerUsername(ctx.trainerUsername);
        req.setTrainingName("Session-" + ctx.uid);
        req.setTrainingDate(LocalDate.now());
        req.setDuration(60);
        return req;
    }
}
