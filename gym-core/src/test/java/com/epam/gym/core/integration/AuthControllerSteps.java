package com.epam.gym.core.integration;

import com.epam.gym.core.dto.request.ChangePasswordRequest;
import com.epam.gym.core.dto.request.LoginRequest;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

@ScenarioScope
public class AuthControllerSteps {

    @Autowired private IntegrationScenarioContext ctx;
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private SharedIntegrationSteps shared;

    @When("the trainee logs in with valid credentials")
    public void theTraineeLogsInWithValidCredentials() {
        LoginRequest req = new LoginRequest();
        req.setUsername(ctx.traineeUsername);
        req.setPassword(ctx.traineePassword);
        ctx.lastResponse = restTemplate.postForEntity("/api/auth/login", req, String.class);
        if (ctx.lastResponse.getStatusCode().is2xxSuccessful()) {
            ctx.traineeToken = shared.extract(ctx.lastResponse.getBody(), "accessToken");
            ctx.activeToken = ctx.traineeToken;
        }
    }

    @Then("the response contains a non-blank token")
    public void theResponseContainsANonBlankToken() {
        String token = shared.extract(ctx.lastResponse.getBody(), "accessToken");
        assertThat(token).isNotBlank();
    }

    @When("the trainee logs in with an incorrect password")
    public void theTraineeLogsInWithAnIncorrectPassword() {
        LoginRequest req = new LoginRequest();
        req.setUsername(ctx.traineeUsername);
        req.setPassword("wrong-password");
        ctx.lastResponse = restTemplate.postForEntity("/api/auth/login", req, String.class);
    }

    @When("the trainee fails to log in {int} times in a row")
    public void theTraineeFailsToLogIn(int times) {
        LoginRequest req = new LoginRequest();
        req.setUsername(ctx.traineeUsername);
        req.setPassword("wrong-password");
        for (int i = 0; i < times; i++) {
            restTemplate.postForEntity("/api/auth/login", req, String.class);
        }
    }

    @Then("a subsequent login attempt returns 401")
    public void aSubsequentLoginAttemptReturns401() {
        LoginRequest req = new LoginRequest();
        req.setUsername(ctx.traineeUsername);
        req.setPassword(ctx.traineePassword);
        ctx.lastResponse = restTemplate.postForEntity("/api/auth/login", req, String.class);
        assertThat(ctx.lastResponse.getStatusCode().value()).isEqualTo(401);
    }

    @When("a GET request is sent to a protected endpoint without a token")
    public void aGetRequestIsSentToProtectedEndpointWithoutToken() {
        ctx.lastResponse = restTemplate.exchange(
                "/api/training-types", HttpMethod.GET,
                new HttpEntity<>(shared.noAuthHeaders()), String.class);
    }

    @When("the trainee logs out")
    public void theTraineeLogsOut() throws InterruptedException {
        // JWT issuedAt has second precision; sleep ensures lastLogout > issuedAt
        // (to make sure token is invalid after logout)
        Thread.sleep(1100);
        ctx.lastResponse = restTemplate.exchange(
                "/api/auth/logout", HttpMethod.POST,
                shared.authRequest(), String.class);
    }

    @And("a subsequent request with the same token returns 401")
    public void aSubsequentRequestWithTheSameTokenReturns401() {
        var response = restTemplate.exchange(
                "/api/training-types", HttpMethod.GET,
                new HttpEntity<>(shared.authHeaders()), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @When("the trainee changes their password")
    public void theTraineeChangesTheirPassword() {
        String newPassword = "NewPass" + ctx.uid;
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setNewPassword(newPassword);
        ctx.lastResponse = restTemplate.exchange(
                "/api/auth/password", HttpMethod.PUT,
                shared.authRequest(req), String.class);
        ctx.traineePassword = newPassword;
    }

    @Then("the trainee can log in with the new password")
    public void theTraineeCanLogInWithTheNewPassword() {
        LoginRequest req = new LoginRequest();
        req.setUsername(ctx.traineeUsername);
        req.setPassword(ctx.traineePassword);
        var resp = restTemplate.postForEntity("/api/auth/login", req, String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }
}
